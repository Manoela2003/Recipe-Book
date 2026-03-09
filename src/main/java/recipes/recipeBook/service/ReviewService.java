package recipes.recipeBook.service;

import recipes.recipeBook.dto.ReviewDTO;
import recipes.recipeBook.entity.User;

import java.util.List;

public interface ReviewService {
    void addReview(Long recipeId, ReviewDTO reviewDTO, User user);
    List<ReviewDTO> getReviewsForRecipe(Long recipeId);
    double getAverageRating(Long recipeId);
    void deleteReview(Long reviewId, User user);
    ReviewDTO getReviewById(Long reviewId);
    void updateReview(Long reviewId, ReviewDTO reviewDTO, User user);
}