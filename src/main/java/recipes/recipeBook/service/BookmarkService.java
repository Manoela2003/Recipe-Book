package recipes.recipeBook.service;

import recipes.recipeBook.entity.User;

public interface BookmarkService {
    void toggleBookmark(Long recipeId, User user);
    boolean isBookmarked(Long recipeId, User user);
}