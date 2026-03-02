package recipes.recipeBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import recipes.recipeBook.entity.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByIsPredefinedTrue();

    List<Tag> findByNameContainingIgnoreCase(String query);

    List<Tag> findTop10ByOrderByNameAsc();
}
