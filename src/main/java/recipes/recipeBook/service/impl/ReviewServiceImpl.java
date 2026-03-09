package recipes.recipeBook.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recipes.recipeBook.dto.ReviewDTO;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.Review;
import recipes.recipeBook.entity.Role;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.repository.ReviewRepository;
import recipes.recipeBook.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, RecipeRepository recipeRepository) {
        this.reviewRepository = reviewRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void addReview(Long recipeId, ReviewDTO reviewDTO, User user) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setRecipe(recipe);
        review.setUser(user);

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsForRecipe(Long recipeId) {
        return reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId).stream()
                .map(review -> {
                    ReviewDTO dto = new ReviewDTO();
                    dto.setId(review.getId());
                    dto.setRating(review.getRating());
                    dto.setComment(review.getComment());
                    dto.setUsername(review.getUser().getUsername());
                    dto.setUserId(review.getUser().getId());
                    dto.setCreatedAt(review.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating(Long recipeId) {
        List<Review> reviews = reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!review.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new IllegalArgumentException("Not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserId(review.getUser().getId());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, ReviewDTO reviewDTO, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!review.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new IllegalArgumentException("Not authorized to update this review");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        reviewRepository.save(review);
    }
}