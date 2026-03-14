package recipes.recipeBook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.RecipeCategory;
import recipes.recipeBook.entity.User;

import java.util.Set;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    boolean existsByTitle(String title);
    Page<Recipe> findByPrimaryCategory(RecipeCategory category, Pageable pageable);
    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Recipe> findByPrimaryCategoryAndTitleContainingIgnoreCase(RecipeCategory category, String title, Pageable pageable);
    Page<Recipe> findByAuthor(User author, Pageable pageable);
    Page<Recipe> findByAuthorAndTitleContainingIgnoreCase(User author, String title, Pageable pageable);
    @Query("SELECT r FROM User u JOIN u.bookmarkedRecipes r WHERE u.id = :userId")
    Page<Recipe> findBookmarkedRecipesByUserId(@Param("userId") Long userId, Pageable pageable);
    @Modifying
    @Query(value = "DELETE FROM user_bookmarks WHERE recipe_id = :recipeId", nativeQuery = true)
    void removeRecipeFromAllBookmarks(@Param("recipeId") Long recipeId);
}
