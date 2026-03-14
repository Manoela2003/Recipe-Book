package recipes.recipeBook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.RecipeCategory;
import recipes.recipeBook.entity.User;

import java.util.List;

public interface RecipeService {
    Recipe findRecipeById(Long id);
    Recipe save(RecipeDTO recipeDTO, List<ImageDTO> imageDTOs, User user);
    Page<Recipe> findAllRecipes(Pageable pageable);
    Recipe updateRecipe(Long id, RecipeDTO recipeDTO, List<ImageDTO> imageDTOs, User user);
    Page<Recipe> findByCategory(RecipeCategory category, Pageable pageable);
    long countRecipes();
    Page<Recipe> searchByTitle(String query, Pageable pageable);
    Page<Recipe> searchByCategoryAndTitle(RecipeCategory category, String query, Pageable pageable);
    Page<Recipe> findMyRecipes(User author, Pageable pageable);
    Page<Recipe> searchMyRecipesByTitle(User author, String query, Pageable pageable);
    Page<Recipe> findBookmarkedRecipes(User user, Pageable pageable);
    void deleteRecipe(Long id);
}
