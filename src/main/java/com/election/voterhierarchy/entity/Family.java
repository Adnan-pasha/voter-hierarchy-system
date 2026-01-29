package com.election.voterhierarchy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "family")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_code", unique = true)
    private String familyCode;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Person> members = new ArrayList<>();

    @Column(name = "contact_person", nullable = false)
    private String contactPerson;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addMember(Person member) {
        members.add(member);
        member.setFamily(this);
    }

    public void removeMember(Person member) {
        members.remove(member);
        member.setFamily(null);
    }
	
	@Transient
	public Person getFamilyHead() {
		if (members == null) return null;
		return members.stream()
				.filter(Person::isFamilyHead)
				.findFirst()
				.orElse(null);
	}
}
