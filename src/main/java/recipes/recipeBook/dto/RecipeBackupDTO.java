package recipes.recipeBook.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeBackupDTO {
    private RecipeDTO recipeDTO;
    private List<ImageBackupDTO> images;
}