package recipes.recipeBook.web;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.exception.DuplicateEmailException;
import recipes.recipeBook.exception.DuplicateUsernameException;
import recipes.recipeBook.service.UserService;

@Controller
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "username", required = false) String username,
                                Model model) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        model.addAttribute("user", userDTO);
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult bindingResult, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!userDTO.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        try {
            userService.createUser(userDTO);
        } catch (DuplicateUsernameException | DuplicateEmailException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        return "redirect:/login?success";
    }
}
