package recipes.recipeBook.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.repository.UserRepository;
import recipes.recipeBook.service.BookmarkService;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public BookmarkServiceImpl(UserRepository userRepository, RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void toggleBookmark(Long recipeId, User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        if (currentUser.getBookmarkedRecipes().contains(recipe)) {
            currentUser.getBookmarkedRecipes().remove(recipe);
        } else {
            currentUser.getBookmarkedRecipes().add(recipe);
        }
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long recipeId, User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        return currentUser.getBookmarkedRecipes().contains(recipe);
    }
}