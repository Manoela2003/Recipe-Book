package recipes.recipeBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.recipeBook.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
}