package recipes.recipeBook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
