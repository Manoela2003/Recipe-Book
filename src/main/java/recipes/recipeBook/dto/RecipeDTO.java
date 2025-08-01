package recipes.recipeBook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RecipeDTO {
    @NotBlank(message = "Title of the recipe cannot be empty")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    @NotBlank(message = "Description of the recipe cannot be empty")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    @NotBlank(message = "Category for the recipe must be chosen")
    private String category;
    @NotEmpty(message = "At least one step must be provided")
    private List<String> instructions;
    private Integer mainImageIndex;
    @NotEmpty(message = "At least one ingredient must be added")
    private List<IngredientDTO> ingredients;
    private long prepTime;
    private long cookTime;
    private int servings;
}
