package recipes.recipeBook.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.service.RecipeService;
import recipes.recipeBook.service.UserService;

@Controller
public class HomeController {

    private final RecipeService recipeService;
    private final UserService userService;

    public HomeController(RecipeService recipeService, UserService userService) {
        this.recipeService = recipeService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpServletRequest request) {
        Page<Recipe> latestRecipes = recipeService.findAllRecipes(PageRequest.of(0, 6, Sort.by("id").descending()));

        long totalRecipes = recipeService.countRecipes();
        long totalUsers = userService.countUsers();

        model.addAttribute("recipes", latestRecipes.getContent());
        model.addAttribute("totalRecipes", totalRecipes);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("requestURI", request.getRequestURI());

        return "home";
    }
}