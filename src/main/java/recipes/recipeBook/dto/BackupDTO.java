package recipes.recipeBook.dto;

import lombok.Data;
import java.util.List;

@Data
public class BackupDTO {
    private List<UserBackupDTO> users;
    private List<RecipeDTO> recipes;
}