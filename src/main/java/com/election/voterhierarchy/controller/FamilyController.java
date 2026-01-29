package com.election.voterhierarchy.controller;

import com.election.voterhierarchy.dto.*;
import com.election.voterhierarchy.entity.Family;
import com.election.voterhierarchy.entity.Person;
import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import com.election.voterhierarchy.service.FamilyService;
import com.election.voterhierarchy.dto.PersonUpdateDTO;
import com.election.voterhierarchy.entity.VoterDetails2002;
import com.election.voterhierarchy.entity.VoterDetailsCurrent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/families")
@RequiredArgsConstructor
@Slf4j
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping
    public String listFamilies(Model model) {
        List<Family> families = familyService.getAllFamilies();
        model.addAttribute("families", families);
        return "family/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("familyHeadDTO", new FamilyHeadDTO());
        model.addAttribute("statuses", PersonStatus.values());
        return "family/create-head";
    }

    @PostMapping("/create")
    public String createFamily(@Valid @ModelAttribute FamilyHeadDTO familyHeadDTO,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", PersonStatus.values());
            return "family/create-head";
        }

        try {
            Family family = familyService.createFamilyWithHead(familyHeadDTO);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Family created successfully with code: " + family.getFamilyCode());
            return "redirect:/families/" + family.getId() + "/members/new";
        } catch (Exception e) {
            log.error("Error creating family", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("statuses", PersonStatus.values());
            return "family/create-head";
        }
    }

    @GetMapping("/{id}/members/new")
    public String showAddMemberForm(@PathVariable Long id, Model model) {
        try {
            Family family = familyService.getFamilyById(id);
            model.addAttribute("family", family);
            model.addAttribute("familyMemberDTO", new FamilyMemberDTO());
            model.addAttribute("relationTypes", new RelationType[]{
                RelationType.SPOUSE, RelationType.SON, RelationType.DAUGHTER, RelationType.DEPENDENT
            });
            model.addAttribute("statuses", PersonStatus.values());
            return "family/add-member";
        } catch (Exception e) {
            log.error("Error loading add member form", e);
            return "redirect:/families";
        }
    }

    @PostMapping("/{id}/members/add")
    public String addMember(@PathVariable Long id,
                           @Valid @ModelAttribute FamilyMemberDTO familyMemberDTO,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Family family = familyService.getFamilyById(id);
            model.addAttribute("family", family);
            model.addAttribute("relationTypes", new RelationType[]{
                RelationType.SPOUSE, RelationType.SON, RelationType.DAUGHTER, RelationType.DEPENDENT
            });
            model.addAttribute("statuses", PersonStatus.values());
            return "family/add-member";
        }

        try {
            familyService.addFamilyMember(id, familyMemberDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Member added successfully");
            return "redirect:/families/" + id + "/members/new";
        } catch (Exception e) {
            log.error("Error adding member", e);
            Family family = familyService.getFamilyById(id);
            model.addAttribute("family", family);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("relationTypes", new RelationType[]{
                RelationType.SPOUSE, RelationType.SON, RelationType.DAUGHTER, RelationType.DEPENDENT
            });
            model.addAttribute("statuses", PersonStatus.values());
            return "family/add-member";
        }
    }

    @GetMapping("/{id}/hierarchy")
    public String viewHierarchy(@PathVariable Long id, Model model) {
        try {
            Family family = familyService.getFamilyById(id);
            HierarchyNode hierarchy = familyService.buildHierarchy(id);
            List<ValidationError> validationErrors = familyService.validateFamily(id);

            model.addAttribute("family", family);
            model.addAttribute("hierarchy", hierarchy);
            model.addAttribute("validationErrors", validationErrors);
            model.addAttribute("hasErrors", !validationErrors.isEmpty());

            return "family/hierarchy";
        } catch (Exception e) {
            log.error("Error viewing hierarchy", e);
            return "redirect:/families";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Family family = familyService.getFamilyById(id);
        Person familyHead = family.getFamilyHead();
        
        FamilyUpdateDTO dto = FamilyUpdateDTO.builder()
            .contactPerson(family.getContactPerson())
            .contactNumber(family.getContactNumber())
            .headStatus(familyHead.getStatus())
            .build();
        
        // Load 2002 details
        if (familyHead.getVoterDetails2002() != null) {
            VoterDetails2002 details2002 = familyHead.getVoterDetails2002();
            dto.setHeadName2002(details2002.getName());
            dto.setHeadParentSpouseName2002(details2002.getParentSpouseName());
            dto.setHeadEpicNo2002(details2002.getEpicNo());
            dto.setHeadAcNo2002(details2002.getAcNo());
            dto.setHeadPartNo2002(details2002.getPartNo());
            dto.setHeadSerialNo2002(details2002.getSerialNo());
        }
        
        // Load current details
        if (familyHead.getVoterDetailsCurrent() != null) {
            VoterDetailsCurrent currentDetails = familyHead.getVoterDetailsCurrent();
            dto.setHeadNameCurrent(currentDetails.getName());
            dto.setHeadParentSpouseNameCurrent(currentDetails.getParentSpouseName());
            dto.setHeadEpicNoCurrent(currentDetails.getEpicNo());
            dto.setHeadAcNoCurrent(currentDetails.getAcNo());
            dto.setHeadPartNoCurrent(currentDetails.getPartNo());
            dto.setHeadSerialNoCurrent(currentDetails.getSerialNo());
            
            if (currentDetails.getBloDetails() != null) {
                dto.setHeadBloName(currentDetails.getBloDetails().getBloName());
                dto.setHeadBloMobile(currentDetails.getBloDetails().getBloMobile());
            }
        }
        
        model.addAttribute("family", family);
        model.addAttribute("familyUpdateDTO", dto);
        model.addAttribute("statuses", PersonStatus.values());
        return "family/edit";
    }

    @PostMapping("/{id}/update")
    public String updateFamily(@PathVariable Long id,
                            @Valid @ModelAttribute FamilyUpdateDTO familyUpdateDTO,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Family family = familyService.getFamilyById(id);
            model.addAttribute("family", family);
            model.addAttribute("statuses", PersonStatus.values());
            return "family/edit";
        }
        
        try {
            familyService.updateFamily(id, familyUpdateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Family updated successfully");
            return "redirect:/families/" + id + "/hierarchy";
        } catch (Exception e) {
            log.error("Error updating family", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            Family family = familyService.getFamilyById(id);
            model.addAttribute("family", family);
            model.addAttribute("statuses", PersonStatus.values());
            return "family/edit";
        }
    }

    @GetMapping("/{familyId}/members/{memberId}/edit")
    public String showEditMemberForm(@PathVariable Long familyId, 
                                    @PathVariable Long memberId, 
                                    Model model) {
        Family family = familyService.getFamilyById(familyId);
        Person member = familyService.getMemberById(memberId);
        
        PersonUpdateDTO dto = PersonUpdateDTO.builder()
            .personId(member.getId())
            .relationType(member.getRelationType())
            .age(member.getAge())
            .status(member.getStatus())
            .build();
        
        // Load 2002 details
        if (member.getVoterDetails2002() != null) {
            VoterDetails2002 details2002 = member.getVoterDetails2002();
            dto.setName2002(details2002.getName());
            dto.setParentSpouseName2002(details2002.getParentSpouseName());
            dto.setEpicNo2002(details2002.getEpicNo());
            dto.setAcNo2002(details2002.getAcNo());
            dto.setPartNo2002(details2002.getPartNo());
            dto.setSerialNo2002(details2002.getSerialNo());
        }
        
        // Load current details
        if (member.getVoterDetailsCurrent() != null) {
            VoterDetailsCurrent currentDetails = member.getVoterDetailsCurrent();
            dto.setNameCurrent(currentDetails.getName());
            dto.setParentSpouseNameCurrent(currentDetails.getParentSpouseName());
            dto.setEpicNoCurrent(currentDetails.getEpicNo());
            dto.setAcNoCurrent(currentDetails.getAcNo());
            dto.setPartNoCurrent(currentDetails.getPartNo());
            dto.setSerialNoCurrent(currentDetails.getSerialNo());
            
            if (currentDetails.getBloDetails() != null) {
                dto.setBloName(currentDetails.getBloDetails().getBloName());
                dto.setBloMobile(currentDetails.getBloDetails().getBloMobile());
            }
        }
        
        model.addAttribute("family", family);
        model.addAttribute("member", member);
        model.addAttribute("personUpdateDTO", dto);
        model.addAttribute("relationTypes", new RelationType[]{
            RelationType.SPOUSE, RelationType.SON, RelationType.DAUGHTER, RelationType.DEPENDENT
        });
        model.addAttribute("statuses", PersonStatus.values());
        return "family/edit-member";
    }

    @PostMapping("/{familyId}/members/{memberId}/update")
    public String updateMember(@PathVariable Long familyId,
                            @PathVariable Long memberId,
                            @ModelAttribute PersonUpdateDTO personUpdateDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            familyService.updateMember(memberId, personUpdateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Member updated successfully");
            return "redirect:/families/" + familyId + "/hierarchy";
        } catch (Exception e) {
            log.error("Error updating member", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/families/" + familyId + "/members/" + memberId + "/edit";
        }
    }

    @PostMapping("/{familyId}/members/{memberId}/delete")
    public String deleteMember(@PathVariable Long familyId,
                            @PathVariable Long memberId,
                            RedirectAttributes redirectAttributes) {
        try {
            familyService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting member", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/families/" + familyId + "/hierarchy";
    }

    @GetMapping("/{id}/finish")
    public String finishFamily(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", 
            "Family data collection completed. Ready for BLO verification.");
        return "redirect:/families";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = familyService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "family/dashboard";
    }

    @GetMapping("/search")
    public String search(@ModelAttribute FamilyFilterDTO filter, Model model) {
        List<Family> families;
        
        if (filter.getFamilyCode() == null && filter.getContactNumber() == null && 
            filter.getContactPerson() == null && filter.getStatus() == null && 
            filter.getFamilyHeadName() == null) {
            families = familyService.getAllFamilies();
        } else {
            families = familyService.searchFamilies(filter);
        }
        
        model.addAttribute("families", families);
        model.addAttribute("filter", filter);
        model.addAttribute("statuses", PersonStatus.values());
        return "family/search";
    }

    @PostMapping("/{id}/delete")
    public String deleteFamily(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            familyService.deleteFamily(id);
            redirectAttributes.addFlashAttribute("successMessage", "Family deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting family", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting family: " + e.getMessage());
        }
        return "redirect:/families";
    }
}
