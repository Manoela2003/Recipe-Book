package recipes.recipeBook.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
