package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "room")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends MasterEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "type")
    private Integer type;
}
