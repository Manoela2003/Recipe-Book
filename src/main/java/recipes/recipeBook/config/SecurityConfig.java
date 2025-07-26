package recipes.recipeBook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private static final String LOGIN_PAGE = "/login";
    private static final String LOGOUT_PAGE = "/logout";
    private static final String REGISTER_PAGE = "/register";
    private static final String CSS_RESOURCES = "/css/**";
    private static final String HOME_PAGE = "/home";
    private static final String LOGOUT_SUCCESS_URL = "/home";
    private static final String IMAGES = "/images/**";
    private static final String RECIPES = "/recipes/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", HOME_PAGE, REGISTER_PAGE, LOGIN_PAGE, CSS_RESOURCES, IMAGES, RECIPES).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage(LOGIN_PAGE)
                        .defaultSuccessUrl(HOME_PAGE, true)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl(LOGOUT_PAGE)
                        .logoutSuccessUrl(LOGOUT_SUCCESS_URL)
                        .permitAll()
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_PAGE, "GET"))
                );


        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
}
