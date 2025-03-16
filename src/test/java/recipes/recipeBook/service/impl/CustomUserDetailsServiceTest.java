package recipes.recipeBook.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("securePassword");
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser");

        assertNotNull(userDetails, "Expected user to be successfully found and loaded.");
        assertEquals("testUser", userDetails.getUsername(), "Expected username to be equal to 'testUser'.");
        assertEquals("securePassword", userDetails.getPassword(), "Expected password to be equal to 'securePassword'.");

        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void testLoadUserByUsernameUserNotFound() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknownUser"),
                "Expected UsernameNotFoundException to be thrown when such user doesn't exist");

        assertEquals("User not found", exception.getMessage(),
                "Expected 'User not found' message to be shown when such user doesn't exist");

        verify(userRepository, times(1)).findByUsername("unknownUser");
    }
}