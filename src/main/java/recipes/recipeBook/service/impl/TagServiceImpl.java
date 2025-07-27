package recipes.recipeBook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.recipeBook.entity.Tag;
import recipes.recipeBook.repository.TagRepository;
import recipes.recipeBook.service.TagService;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getPredefinedTags() {
        return tagRepository.findByIsPredefinedTrue();
    }

    public List<Tag> searchTagsByName(String query) {
        return tagRepository.findByNameContainingIgnoreCase(query);
    }

    public Tag findOrCreateTag(String name) {
        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(name);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }

        Tag newTag = new Tag(name.trim(), false);
        return tagRepository.save(newTag);
    }

    public void initializePredefinedTags() {
        String[] predefinedTags = {
                "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Keto", "Low-Carb", "Healthy",
                "Italian", "Asian", "Mexican", "Mediterranean", "American", "Indian", "Chinese",
                "Baked", "Grilled", "Fried", "Slow Cooker", "One-Pot", "No-Cook", "Quick & Easy",
                "Spicy", "Sweet", "Comfort Food", "Holiday", "Party Food"
        };

        for (String tagName : predefinedTags) {
            if (!tagRepository.findByNameIgnoreCase(tagName).isPresent()) {
                tagRepository.save(new Tag(tagName, true));
            }
        }
    }
}
