package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "timetable")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Timetable extends MasterEntity {

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_info_id", nullable = false)
    private TimetableInfo timetableInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_data_id", nullable = false)
    private TimetableData timetableData;
}
