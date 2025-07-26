package recipes.recipeBook.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import recipes.recipeBook.entity.CustomUserDetails;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.repository.UserRepository;
import recipes.recipeBook.service.UserService;

import java.util.Optional;

public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return new CustomUserDetails(userService.getUserByUsername(username));
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
