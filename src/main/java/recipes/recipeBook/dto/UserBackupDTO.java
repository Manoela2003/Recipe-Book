package recipes.recipeBook.dto;

import lombok.Data;
import recipes.recipeBook.entity.Role;

@Data
public class UserBackupDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}