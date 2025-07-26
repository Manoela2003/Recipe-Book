package recipes.recipeBook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import recipes.recipeBook.service.UserService;
import recipes.recipeBook.service.impl.CustomUserDetailsService;

@Configuration
public class UserDetailsServiceConfig {

    private final UserService userService;

    public UserDetailsServiceConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userService);
    }
}