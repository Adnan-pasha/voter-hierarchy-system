package com.election.voterhierarchy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blo_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_details_current_id")
    private VoterDetailsCurrent voterDetailsCurrent;

    @Column(name = "blo_name", nullable = false)
    private String bloName;

    @Column(name = "blo_mobile", nullable = false)
    private String bloMobile;
}
