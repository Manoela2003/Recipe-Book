package recipes.recipeBook.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.dto.mapper.UserDTOToUserMapper;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.exception.DuplicateEmailException;
import recipes.recipeBook.exception.DuplicateUsernameException;
import recipes.recipeBook.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUserDuplicateUsername() {
        UserDTO user = new UserDTO();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("user@mail.com");

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> userService.createUser(user),
                "Expected DuplicateUsernameException to be thrown when a user with that username already exists.");

        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUserDuplicateEmail() {
        UserDTO user = new UserDTO();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("user@mail.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(user),
                "Expected DuplicateEmailException to be thrown when a user with that email already exists.");

        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUserSuccess() {
        UserDTO user = new UserDTO();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("user@mail.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);

        User savedUser = UserDTOToUserMapper.map(user);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        assertEquals(userService.createUser(user).getId(), savedUser.getId(),
                "Expected the saved user's id to be the same as the given user.");

        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
}