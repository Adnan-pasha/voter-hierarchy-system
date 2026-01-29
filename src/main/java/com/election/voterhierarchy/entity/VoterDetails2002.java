package com.election.voterhierarchy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voter_details_2002")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoterDetails2002 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parent_spouse_name", nullable = false)
    private String parentSpouseName;

    @Column(name = "epic_no", nullable = false)
    private String epicNo;

    @Column(name = "ac_no", nullable = false)
    private String acNo;

    @Column(name = "part_no", nullable = false)
    private String partNo;

    @Column(name = "serial_no", nullable = false)
    private String serialNo;
}
