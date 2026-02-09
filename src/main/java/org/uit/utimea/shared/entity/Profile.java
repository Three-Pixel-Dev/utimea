package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "profile")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends MasterEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "degree")
    private String degree; // for teacher

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private CodeValue department; // for teacher

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private CodeValue batch; // for student

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_section_id")
    private MajorSection majorSection; // student
}