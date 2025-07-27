package recipes.recipeBook.dto.mapper;

import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.entity.*;
import recipes.recipeBook.service.ImageService;
import recipes.recipeBook.service.TagService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeDTOToRecipeMapper {

    public static Recipe map(RecipeDTO dto, User user, TagService tagService) {
        Recipe recipe = new Recipe();
        recipe.setAuthor(user);
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
        recipe.setPrimaryCategory(dto.getPrimaryCategory());
        List<Tag> tags = new ArrayList<>();
        if (dto.getTagNames() != null && !dto.getTagNames().isEmpty()) {
            for (String tagName : dto.getTagNames()) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    Tag tag = tagService.findOrCreateTag(tagName.trim());
                    tags.add(tag);
                }
            }
        }
        recipe.setTags(tags);
        List<RecipeStep> steps = new ArrayList<>();
        List<String> stepTexts = dto.getInstructions();

        for (int i = 0; i < stepTexts.size(); i++) {
            RecipeStep step = new RecipeStep();
            step.setStepNumber(i + 1);
            step.setInstruction(stepTexts.get(i));
            steps.add(step);
        }
        recipe.setInstructions(steps);

        List<Ingredient> ingredients = dto.getIngredients().stream()
                .map(ingredientDTO -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(ingredientDTO.getName());
                    ingredient.setUnit(ingredientDTO.getUnit());
                    ingredient.setAmount(ingredientDTO.getAmount());
                    return ingredient;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        recipe.setIngredients(ingredients);

        recipe.setPrepTime(dto.getPrepTime());
        recipe.setCookTime(dto.getCookTime());
        recipe.setServings(dto.getServings());
        recipe.setCreatedOn(new Date());

        return recipe;
    }
}
