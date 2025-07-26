package recipes.recipeBook.service;

import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.User;

import java.util.List;

public interface RecipeService {
    Recipe findRecipeById(Long id);
    Recipe save(RecipeDTO recipeDTO, List<ImageDTO> imageDTOs, User user);
}
