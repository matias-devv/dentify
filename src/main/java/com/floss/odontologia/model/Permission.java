package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor @NoArgsConstructor @Getter @Setter
@Table( name = "permissions")
public class Permission {

    @Id @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id_permission;

    @Column ( unique = true, nullable = false)
    private String permissionName;
}
