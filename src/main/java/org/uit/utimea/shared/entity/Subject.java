package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.uit.utimea.shared.data.SubjectYear;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Subject extends MasterEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_subject_type",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_type_id")
    )
    private List<CodeValue> subjectTypes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id")
    private CodeValue roomType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "subject_teacher_mapping",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Profile> teachers = new ArrayList<>();

    @Column(name = "special_room_count")
    private Integer specialRoomCount;

    @Column(name="is_first_sem")
    private Boolean isFirstSem;

    @Column(name = "subject_year")
    @Enumerated(EnumType.STRING)
    private SubjectYear subjectYear;
}