package com.dentify.security.service;

import com.dentify.security.authority.AuthorityResolver;
import com.dentify.security.model.AuthUser;
import com.dentify.security.repository.IAuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService{

    private final AuthorityResolver authorityResolver;
    private final IAuthUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //This query brings up the roles
        AuthUser authUser = userRepository.findByUsernameWithRoles( username)
                                          .orElseThrow( ()-> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorityList = authorityResolver.resolveAuthorities(authUser);

        return new User(authUser.getUsername(),
                        authUser.getPassword(),
                        authUser.isEnabled(),
                        authUser.isAccountNonExpired(),
                        authUser.isCredentialNonExpired(),
                        authUser.isAccountNonLocked(),
                        authorityList);
    }
}
