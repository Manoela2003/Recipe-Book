package recipes.recipeBook.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import recipes.recipeBook.dto.*;
import recipes.recipeBook.dto.mapper.RecipeMapper;
import recipes.recipeBook.entity.*;
import recipes.recipeBook.exception.DuplicateRecipeException;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.service.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final TempImageService imageService;
    private final PdfExportService pdfExportService;
    private final ReviewService reviewService;
    private final BookmarkService bookmarkService;

    @Autowired
    public RecipeController(RecipeService recipeService, TempImageService imageService, PdfExportService pdfExportService, ReviewService reviewService, BookmarkService bookmarkService) {
        this.recipeService = recipeService;
        this.imageService = imageService;
        this.pdfExportService = pdfExportService;
        this.reviewService = reviewService;
        this.bookmarkService = bookmarkService;
    }

    @GetMapping("/{id}")
    public String viewRecipe(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        Recipe recipe = recipeService.findRecipeById(id);

        boolean isAuthor = false;
        boolean isAdmin = false;
        boolean isBookmarked = false;

        if (userDetails != null) {
            if (recipe.getAuthor() != null) {
                isAuthor = recipe.getAuthor().getId().equals(userDetails.getUser().getId());
            }
            isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            isBookmarked = bookmarkService.isBookmarked(id, userDetails.getUser());
        }

        String backUrl = "/recipes";
        String backText = "All Recipes";

        if (model.containsAttribute("preservedBackUrl")) {
            backUrl = (String) model.getAttribute("preservedBackUrl");
            backText = (String) model.getAttribute("preservedBackText");
        } else {
            String referer = request.getHeader("Referer");
            if (referer != null) {
                try {
                    java.net.URL url = new java.net.URL(referer);
                    String path = url.getPath();
                    String query = url.getQuery();

                    String exactUrl = path + (query != null ? "?" + query : "");

                    if (path.contains("/recipes/my-recipes")) {
                        backUrl = exactUrl;
                        backText = "My Recipes";
                    } else if (path.contains("/recipes/favorites")) {
                        backUrl = exactUrl;
                        backText = "Bookmarked Recipes";
                    } else if (path.contains("/home") || path.equals("/")) {
                        backUrl = "/home";
                        backText = "Home";
                    } else if (path.contains("/recipes")) {
                        backUrl = exactUrl;
                        backText = "All Recipes";
                    }
                } catch (Exception e) {

                }
            }
        }

        model.addAttribute("recipe", recipe);
        model.addAttribute("isAuthor", isAuthor || isAdmin);
        model.addAttribute("isBookmarked", isBookmarked);
        model.addAttribute("reviews", reviewService.getReviewsForRecipe(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("backText", backText);

        if (!model.containsAttribute("newReview")) {
            model.addAttribute("newReview", new ReviewDTO());
        }

        Map<String, List<Ingredient>> groupedIngredients = new java.util.LinkedHashMap<>();
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                String section = (ingredient.getSection() != null && !ingredient.getSection().trim().isEmpty())
                        ? ingredient.getSection() : "Main";
                groupedIngredients.computeIfAbsent(section, k -> new java.util.ArrayList<>()).add(ingredient);
            }
        }
        model.addAttribute("groupedIngredients", groupedIngredients);

        return "recipe-details";
    }

    @GetMapping("/edit/{id}")
    public String editRecipe(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Recipe recipe = recipeService.findRecipeById(id);

        if (recipe == null) {
            redirectAttributes.addFlashAttribute("message", "Recipe not found");
            return "redirect:/recipes";
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!recipe.getAuthor().getId().equals(userDetails.getUser().getId()) && !isAdmin) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this recipe.");
            return "redirect:/recipes/" + id;
        }

        List<TempImageDTO> tempImages = new ArrayList<>();
        if (recipe.getImages() != null) {
            for (Image img : recipe.getImages()) {
                TempImageDTO temp = new TempImageDTO();
                temp.setId(img.getId().toString());
                temp.setData(img.getImage());
                temp.setMimeType(img.getMimeType());
                tempImages.add(temp);
            }
        }
        model.addAttribute("tempImages", tempImages);

        model.addAttribute("formMode", "edit");
        model.addAttribute("formAction", "/recipes/edit/" + recipe.getId());
        model.addAttribute("recipe", RecipeMapper.mapToRecipeDTO(recipe));
        return "add-recipe";
    }

    @PostMapping("/edit/{id}")
    public String updateRecipe(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("recipe") RecipeDTO recipeDTO,
                               BindingResult bindingResult,
                               @RequestParam("images") MultipartFile[] images,
                               @RequestParam(value = "tempImageIds", required = false) List<String> tempImageIds,
                               @RequestParam(value = "mainImageIndex", required = false) String mainImageIndexParam,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        List<String> filteredSteps = recipeDTO.getInstructions() == null ? new ArrayList<>() :
                recipeDTO.getInstructions().stream()
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .toList();
        recipeDTO.setInstructions(filteredSteps);

        List<IngredientDTO> filteredIngredients = recipeDTO.getIngredients() == null ? new ArrayList<>() :
                recipeDTO.getIngredients().stream()
                        .filter(i -> i != null && i.getName() != null && !i.getName().trim().isEmpty())
                        .toList();
        recipeDTO.setIngredients(filteredIngredients);

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "edit");
            model.addAttribute("formAction", "/recipes/edit/" + id);

            Recipe recipe = recipeService.findRecipeById(id);
            List<TempImageDTO> tempImages = new ArrayList<>();
            if (recipe.getImages() != null) {
                for (Image img : recipe.getImages()) {
                    TempImageDTO temp = new TempImageDTO();
                    temp.setId(img.getId().toString());
                    temp.setData(img.getImage());
                    temp.setMimeType(img.getMimeType());
                    tempImages.add(temp);
                }
            }
            model.addAttribute("tempImages", tempImages);
            return "add-recipe";
        }

        List<ImageDTO> imageDTOs = new ArrayList<>();
        if (tempImageIds != null) {
            for (String imageId : tempImageIds) {
                try {
                    TempImageDTO temp = imageService.getTempImage(imageId);
                    if (temp != null) {
                        imageDTOs.add(new ImageDTO(temp.getData(), temp.getMimeType()));
                    } else {
                        Recipe current = recipeService.findRecipeById(id);
                        current.getImages().stream()
                                .filter(img -> img.getId().toString().equals(imageId))
                                .findFirst()
                                .ifPresent(img -> imageDTOs.add(new ImageDTO(img.getImage(), img.getMimeType())));
                    }
                } catch (Exception e) {
                }
            }
        }

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                try {
                    imageDTOs.add(new ImageDTO(file.getBytes(), file.getContentType()));
                } catch (Exception e) {
                }
            }
        }

        if (mainImageIndexParam != null && !mainImageIndexParam.isEmpty()) {
            recipeDTO.setMainImageIndex(Integer.parseInt(mainImageIndexParam));
        }

        recipeService.updateRecipe(id, recipeDTO, imageDTOs, userDetails.getUser());

        redirectAttributes.addFlashAttribute("successMessage", "Recipe updated successfully!");

        return "redirect:/recipes/" + id;
    }

    @GetMapping("/add")
    public String showAddRecipeForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("recipe")) {
            RecipeDTO recipeDTO = new RecipeDTO();
            recipeDTO.setIngredients(List.of(new IngredientDTO()));
            recipeDTO.setInstructions(List.of(""));
            model.addAttribute("recipe", recipeDTO);
        } else {
            RecipeDTO recipeDTO = (RecipeDTO) model.getAttribute("recipe");

            if (recipeDTO != null && recipeDTO.getInstructions() != null) {
                recipeDTO.setInstructions(
                        recipeDTO.getInstructions().stream()
                                .filter(step -> step != null && !step.trim().isEmpty())
                                .toList()
                );
            }

            if (recipeDTO != null && recipeDTO.getIngredients() != null) {
                recipeDTO.setIngredients(recipeDTO.getIngredients().stream()
                        .filter(ingredient -> ingredient.getName() != null)
                        .toList());
            }

            model.addAttribute("recipe", recipeDTO);
        }
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("formMode", "add");
        model.addAttribute("formAction", "/recipes/add");
        model.addAttribute("tempImages", new ArrayList<TempImageDTO>());
        return "add-recipe";
    }

    @PostMapping("/add")
    public String addRecipe(@Valid @ModelAttribute("recipe") RecipeDTO recipeDTO,
                            BindingResult bindingResult,
                            @RequestParam("images") MultipartFile[] images,
                            @RequestParam(value = "tempImageIndexes", required = false) List<Integer> tempImageIndexes,
                            @RequestParam(value = "tempImageIds", required = false) List<String> tempImageIds,
                            @RequestParam(value = "mainImageIndex", required = false) String mainImageIndexParam,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<TempImageDTO> existingTempImages = new ArrayList<>();
        if (tempImageIds != null && !tempImageIds.isEmpty()) {
            existingTempImages.addAll(imageService.getAllByIds(tempImageIds));
        }

        List<TempImageDTO> newTempImages = new ArrayList<>();
        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty() && file.getSize() > 0) {
                newTempImages.add(imageService.storeTemp(file));
            }
        }

        List<TempImageDTO> allTempImages = new ArrayList<>();
        allTempImages.addAll(existingTempImages);
        allTempImages.addAll(newTempImages);

        List<TempImageDTO> selectedTempImages;
        if (tempImageIndexes != null && !tempImageIndexes.isEmpty() && !existingTempImages.isEmpty()) {
            selectedTempImages = new ArrayList<>();
            for (Integer index : tempImageIndexes) {
                if (index >= 0 && index < existingTempImages.size()) {
                    selectedTempImages.add(existingTempImages.get(index));
                }
            }
            selectedTempImages.addAll(newTempImages);
        } else {
            selectedTempImages = allTempImages;
        }

        if (mainImageIndexParam != null && !mainImageIndexParam.trim().isEmpty()) {
            try {
                int mainIndex = Integer.parseInt(mainImageIndexParam);
                recipeDTO.setMainImageIndex(mainIndex);
            } catch (NumberFormatException e) {
                recipeDTO.setMainImageIndex(0);
            }
        } else if (!selectedTempImages.isEmpty()) {
            recipeDTO.setMainImageIndex(0);
        }

        if (bindingResult.hasErrors()) {
            List<String> tempIds = selectedTempImages.stream()
                    .map(TempImageDTO::getId)
                    .collect(Collectors.toList());

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);
            redirectAttributes.addFlashAttribute("recipe", recipeDTO);
            redirectAttributes.addFlashAttribute("tempImages", selectedTempImages);
            redirectAttributes.addFlashAttribute("tempImageIds", tempIds);

            return "redirect:/recipes/add";
        }

        List<ImageDTO> imageDTOs = selectedTempImages.stream()
                .map(temp -> new ImageDTO(temp.getData(), temp.getMimeType()))
                .collect(Collectors.toCollection(ArrayList::new));

        Recipe recipe = recipeService.save(recipeDTO, imageDTOs, userDetails.getUser());

        selectedTempImages.forEach(img -> imageService.deleteTempImage(img.getId()));

        redirectAttributes.addFlashAttribute("successMessage", "Recipe added successfully!");
        return "redirect:/recipes/" + recipe.getId();
    }

    @GetMapping
    public String getAllRecipes(
            @PageableDefault(size = 9) Pageable pageable,
            @RequestParam(value = "category", required = false) RecipeCategory category,
            @RequestParam(value = "query", required = false) String query,
            Model model,
            HttpServletRequest request) {

        Page<Recipe> recipes;

        if (query != null && !query.trim().isEmpty() && category != null) {
            recipes = recipeService.searchByCategoryAndTitle(category, query.trim(), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            recipes = recipeService.searchByTitle(query.trim(), pageable);
        } else if (category != null) {
            recipes = recipeService.findByCategory(category, pageable);
        } else {
            recipes = recipeService.findAllRecipes(pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", query);
        model.addAttribute("requestURI", request.getRequestURI());
        return "recipes-list";
    }

    @GetMapping("/my-recipes")
    public String getMyRecipes(
            @PageableDefault(size = 9) Pageable pageable,
            @RequestParam(value = "query", required = false) String query,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            HttpServletRequest request) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Page<Recipe> recipes;
        if (query != null && !query.trim().isEmpty()) {
            recipes = recipeService.searchMyRecipesByTitle(userDetails.getUser(), query.trim(), pageable);
        } else {
            recipes = recipeService.findMyRecipes(userDetails.getUser(), pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("isMyRecipes", true);
        model.addAttribute("searchQuery", query);
        model.addAttribute("requestURI", request.getRequestURI());

        return "recipes-list";
    }

    @GetMapping("/{id}/export/pdf")
    public void exportToPdf(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        Recipe recipe = recipeService.findRecipeById(id);

        response.setContentType("application/pdf");
        String encodedTitle = java.net.URLEncoder.encode(recipe.getTitle(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename*=UTF-8''" + encodedTitle + ".pdf";
        response.setHeader(headerKey, headerValue);

        pdfExportService.exportRecipeToPdf(recipe, response);
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable("id") Long id,
                            @Valid @ModelAttribute("newReview") ReviewDTO reviewDTO,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newReview", bindingResult);
            redirectAttributes.addFlashAttribute("newReview", reviewDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a star rating to submit your review.");
            return "redirect:/recipes/" + id + "#reviews";
        }

        reviewService.addReview(id, reviewDTO, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Review added successfully!");

        return "redirect:/recipes/" + id + "#reviews";
    }

    @PostMapping("/{recipeId}/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("recipeId") Long recipeId,
                               @PathVariable("reviewId") Long reviewId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            reviewService.deleteReview(reviewId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recipes/" + recipeId + "#reviews";
    }

    @PostMapping("/{recipeId}/reviews/{reviewId}/edit")
    public String updateReview(@PathVariable("recipeId") Long recipeId,
                               @PathVariable("reviewId") Long reviewId,
                               @Valid @ModelAttribute("review") ReviewDTO reviewDTO,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            return "edit-review";
        }
        try {
            reviewService.updateReview(reviewId, reviewDTO, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Review updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recipes/" + recipeId + "#reviews";
    }

    @GetMapping("/{recipeId}/reviews/{reviewId}/edit")
    public String showEditReviewForm(@PathVariable("recipeId") Long recipeId,
                                     @PathVariable("reviewId") Long reviewId,
                                     Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        ReviewDTO reviewDTO = reviewService.getReviewById(reviewId);
        model.addAttribute("review", reviewDTO);
        model.addAttribute("recipeId", recipeId);
        return "edit-review";
    }

    @PostMapping("/{id}/bookmark")
    public String toggleBookmark(@PathVariable("id") Long id,
                                 @RequestParam(value = "backUrl", required = false) String backUrl,
                                 @RequestParam(value = "backText", required = false) String backText,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        boolean wasBookmarked = bookmarkService.isBookmarked(id, userDetails.getUser());
        bookmarkService.toggleBookmark(id, userDetails.getUser());

        if (wasBookmarked) {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe removed from bookmarks.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Recipe saved to bookmarks!");
        }

        if (backUrl != null && backText != null) {
            redirectAttributes.addFlashAttribute("preservedBackUrl", backUrl);
            redirectAttributes.addFlashAttribute("preservedBackText", backText);
        }

        return "redirect:/recipes/" + id;
    }

    @GetMapping("/favorites")
    public String getFavoriteRecipes(
            @PageableDefault(size = 9) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            HttpServletRequest request) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Page<Recipe> recipes = recipeService.findBookmarkedRecipes(userDetails.getUser(), pageable);

        model.addAttribute("recipes", recipes);
        model.addAttribute("isFavorites", true);
        model.addAttribute("requestURI", request.getRequestURI());

        return "recipes-list";
    }

    @PostMapping("/{id}/delete")
    public String deleteRecipe(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Recipe recipe = recipeService.findRecipeById(id);
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!recipe.getAuthor().getId().equals(userDetails.getUser().getId()) && !isAdmin) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this recipe.");
                return "redirect:/recipes/" + id;
            }

            recipeService.deleteRecipe(id);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe deleted successfully.");
            return "redirect:/recipes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete recipe: " + e.getMessage());
            return "redirect:/recipes/" + id;
        }
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleDuplicatedEntityException(NotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/home";
    }

    @ExceptionHandler(DuplicateRecipeException.class)
    public String handleDuplicateRecipeException(DuplicateRecipeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/recipes/add";
    }
}