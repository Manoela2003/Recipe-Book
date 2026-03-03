package recipes.recipeBook.dto.mapper;

import recipes.recipeBook.dto.IngredientDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.entity.*;
import recipes.recipeBook.service.TagService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeMapper {

    public static Recipe mapToRecipe(RecipeDTO dto, User user, TagService tagService) {
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
            if (stepTexts.get(i) != null && !stepTexts.get(i).trim().isEmpty()) {
                RecipeStep step = new RecipeStep();
                step.setStepNumber(steps.size() + 1);
                step.setInstruction(stepTexts.get(i));
                steps.add(step);
            }
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
        recipe.setNotes(dto.getNotes());
        recipe.setCreatedOn(new Date());

        return recipe;
    }

    public static RecipeDTO mapToRecipeDTO(Recipe recipe) {
        if (recipe == null) return null;

        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setPrimaryCategory(recipe.getPrimaryCategory());

        if (recipe.getAuthor() != null) {
            dto.setAuthorUsername(recipe.getAuthor().getUsername());
        }

        if (recipe.getTags() != null) {
            List<String> tagNames = recipe.getTags().stream()
                    .map(Tag::getName)
                    .toList();
            dto.setTagNames(tagNames);
        } else {
            dto.setTagNames(new ArrayList<>());
        }

        if (recipe.getInstructions() != null) {
            List<String> instructions = recipe.getInstructions().stream()
                    .map(RecipeStep::getInstruction)
                    .toList();
            dto.setInstructions(instructions);
        } else {
            dto.setInstructions(new ArrayList<>());
        }

        if (recipe.getIngredients() != null) {
            List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                    .map(ingredient -> {
                        IngredientDTO ingDto = new IngredientDTO();
                        ingDto.setName(ingredient.getName());
                        ingDto.setAmount(ingredient.getAmount());
                        ingDto.setUnit(ingredient.getUnit());
                        return ingDto;
                    })
                    .toList();
            dto.setIngredients(ingredientDTOs);
        } else {
            dto.setIngredients(new ArrayList<>());
        }

        dto.setMainImageIndex(recipe.getMainImageIndex());

        dto.setNotes(recipe.getNotes());

        dto.setPrepTime(recipe.getPrepTime());
        dto.setCookTime(recipe.getCookTime());
        dto.setServings(recipe.getServings());

        return dto;
    }

}
