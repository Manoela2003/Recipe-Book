package recipes.recipeBook.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientDTO {
    @NotBlank(message = "Ingredient name cannot be blank")
    private String name;

    @Min(value = 0, message = "Amount must be non-negative")
    private Double amount;

    @NotBlank(message = "Unit is required")
    private String unit;
}
