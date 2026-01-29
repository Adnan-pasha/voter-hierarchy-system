package com.election.voterhierarchy.entity;

import com.election.voterhierarchy.enums.PersonStatus;
import com.election.voterhierarchy.enums.RelationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "is_family_head")
    private Boolean isFamilyHead;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private RelationType relationType;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PersonStatus status;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoterDetails2002 voterDetails2002;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoterDetailsCurrent voterDetailsCurrent;

    public void setVoterDetails2002(VoterDetails2002 details) {
        if (details == null) {
            if (this.voterDetails2002 != null) {
                this.voterDetails2002.setPerson(null);
            }
        } else {
            details.setPerson(this);
        }
        this.voterDetails2002 = details;
    }

    public void setVoterDetailsCurrent(VoterDetailsCurrent details) {
        if (details == null) {
            if (this.voterDetailsCurrent != null) {
                this.voterDetailsCurrent.setPerson(null);
            }
        } else {
            details.setPerson(this);
        }
        this.voterDetailsCurrent = details;
    }
	
	public boolean isFamilyHead() {
		return Boolean.TRUE.equals(isFamilyHead);
	}
	
	public void setIsFamilyHead(Boolean isFamilyHead) {
		this.isFamilyHead = isFamilyHead;
	}
}
