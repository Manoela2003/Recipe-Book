package recipes.recipeBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import recipes.recipeBook.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
