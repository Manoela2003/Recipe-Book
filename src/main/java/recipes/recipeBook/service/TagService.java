package recipes.recipeBook.service;

import recipes.recipeBook.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    List<Tag> getPredefinedTags();
    List<Tag> searchTagsByName(String query);
    Tag findOrCreateTag(String name);
    void initializePredefinedTags();
}
