package com.floss.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @AllArgsConstructor
@NoArgsConstructor
@Table ( name = "Auth_users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user_security;

    @Column ( unique = true )
    private String username;
    private String password;
    private boolean enabled;
    private boolean accountNotExpired;
    private boolean accountNotLocked;
    private boolean credentialNotExpired;

    @ManyToOne( fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable( name = "user_roles", joinColumns = @JoinColumn ( name = "user_id"),
                 inverseJoinColumns = @JoinColumn ( name = "role_id") )
    private Role role;

    @OneToOne
    @JoinColumn( name = "id_user_app" )
    private AppUser appUser;
}
