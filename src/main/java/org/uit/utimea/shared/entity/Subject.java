package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "subject")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subject extends MasterEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description")
    private String description;
}
