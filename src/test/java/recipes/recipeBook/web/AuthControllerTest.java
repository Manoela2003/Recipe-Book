package recipes.recipeBook.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import recipes.recipeBook.config.SecurityConfig;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.exception.DuplicateEmailException;
import recipes.recipeBook.exception.DuplicateUsernameException;
import recipes.recipeBook.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    void testShowLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testShowRegisterPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", new UserDTO()));
    }

    @Test
    void testRegisterUserPasswordMismatch() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", "testUser")
                        .param("password", "password123")
                        .param("confirmPassword", "wrongPassword")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Passwords do not match"));
    }

    @Test
    void testRegisterUserDuplicateUsername() throws Exception {
        Mockito.doThrow(new DuplicateUsernameException("existingUser"))
                .when(userService).createUser(Mockito.any(UserDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", "existingUser")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "User with username 'existingUser' already exists!"));
    }

    @Test
    void testRegisterUserDuplicateEmail() throws Exception {
        Mockito.doThrow(new DuplicateEmailException("duplicate@example.com"))
                .when(userService).createUser(Mockito.any(UserDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", "newUser")
                        .param("email", "duplicate@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "User with email 'duplicate@example.com' already exists!"));
    }

    @Test
    void testRegisterUserInvalidUsernameAndPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .param("username", "us")
                .param("password", "pass")
                .param("confirmPassword", "pass")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeHasFieldErrors("user", "username", "password"));
    }

    @Test
    void testRegisterUserSuccess() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", "validUser")
                        .param("email", "validUser@mail.com")
                        .param("firstName", "Valid")
                        .param("lastName", "User")
                        .param("password", "validPassword123")
                        .param("confirmPassword", "validPassword123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));

        verify(userService, times(1)).createUser(any(UserDTO.class));
    }
}