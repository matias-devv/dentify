package com.dentify.security.authority;

import com.dentify.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthorityResolver {

    public List<GrantedAuthority> resolveAuthorities(AuthUser user) {

        List<GrantedAuthority> authorityList = new ArrayList<>();

        user.getRoles()
                .forEach(role -> { authorityList.add( new SimpleGrantedAuthority( "ROLE_".concat(role.getRoleName() ) ) ); });

        user.getRoles()
                .stream()
                .flatMap( role -> role.getPermissions().stream() )
                .forEach( permission -> { authorityList.add( new SimpleGrantedAuthority( permission.getPermissionName()  ) ); });

        return authorityList;
    }
}