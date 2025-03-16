package recipes.recipeBook.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import recipes.recipeBook.bdd.CucumberSpringConfiguration;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.repository.UserRepository;
import recipes.recipeBook.web.AuthController;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class UserRegistrationSteps {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private UserDTO userDTO;

    @Given("I have a valid user registration request")
    public void i_have_a_valid_user_registration_request() {
        userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setPassword("securePassword");
        userDTO.setEmail("testuser@mail.com");

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @When("I submit the registration form")
    public void i_submit_the_registration_form() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", userDTO.getUsername())
                        .param("email", userDTO.getEmail())
                        .param("password", userDTO.getPassword())
                        .param("confirmPassword", userDTO.getPassword()))
                .andExpect(redirectedUrl("/login?success"));
    }

    @Then("The user should be registered successfully")
    public void the_user_should_be_registered_successfully() {
        assertTrue(userRepository.existsByUsername(userDTO.getUsername()),
                "User was not saved in the database!");
    }
}
