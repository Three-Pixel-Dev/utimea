package org.uit.utimea.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Code extends MasterEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "constant_value")
    private String constantValue;

    @OneToMany(mappedBy = "code", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeValue> codeValues = new ArrayList<>();
}
