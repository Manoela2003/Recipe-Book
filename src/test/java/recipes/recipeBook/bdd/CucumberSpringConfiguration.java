package recipes.recipeBook.bdd;

import io.cucumber.java.After;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import recipes.recipeBook.RecipeBookApplication;
import recipes.recipeBook.repository.UserRepository;

@CucumberContextConfiguration
@SpringBootTest(classes = RecipeBookApplication.class)
@ActiveProfiles("bdd")
@ComponentScan(basePackages = "recipes.recipeBook")
public class CucumberSpringConfiguration {

    @Autowired
    private UserRepository userRepository;

    @After
    public void resetDatabase() {
        userRepository.deleteAll();
    }
}