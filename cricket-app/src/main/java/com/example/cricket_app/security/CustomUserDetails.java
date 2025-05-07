package com.example.cricket_app.security;

import com.example.cricket_app.entity.Users;
import com.example.cricket_app.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
//my  application has its own Users entity, but Spring Security doesn’t know how to use it.
//so we create CustomUserDetails and tell spring that to  get email,password from my user object.
public class CustomUserDetails implements UserDetails {
    Long id;
    String email;
    String password;
    UserRole role;


    public CustomUserDetails() {
    }

    public CustomUserDetails(Users user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }//granted authority is  an interface implemented by SimpleGrantedAuthority.

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
