package recipes.recipeBook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.dto.mapper.RecipeMapper;
import recipes.recipeBook.entity.*;
import recipes.recipeBook.exception.DuplicateRecipeException;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.service.RecipeService;
import recipes.recipeBook.service.TagService;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeServiceImpl implements RecipeService {
    private RecipeRepository recipeRepository;
    private TagService tagService;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, TagService tagService) {
        this.recipeRepository = recipeRepository;
        this.tagService = tagService;
    }

    @Override
    public Recipe findRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Recipe with id %d not found", id)));

        if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
            Integer mainIndex = recipe.getMainImageIndex();

            if (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) {
                Image mainImage = recipe.getImages().get(mainIndex);
                recipe.setImageBase64(mainImage.parseImage());
            }
        }

        return recipe;
    }

    @Override
    public Recipe save(RecipeDTO recipeDTO, List<ImageDTO> imageDTOs, User user) {
        if (recipeRepository.existsByTitle(recipeDTO.getTitle())) {
            throw new DuplicateRecipeException(recipeDTO.getTitle());
        }

        Recipe recipe = RecipeMapper.mapToRecipe(recipeDTO, user, tagService);
        recipe = recipeRepository.save(recipe);

        if (imageDTOs != null && !imageDTOs.isEmpty()) {
            List<Image> images = new ArrayList<>();
            for (ImageDTO imageDto : imageDTOs) {
                Image image = new Image(imageDto.getImageBytes(), imageDto.getMimeType());
                image.setRecipe(recipe);
                images.add(image);
            }
            List<Image> existingImages = recipe.getImages();
            existingImages.clear();
            existingImages.addAll(images);
            recipe.setMainImageIndex(recipeDTO.getMainImageIndex());

            recipe = recipeRepository.save(recipe);
        }

        return recipe;
    }

    @Override
    public Page<Recipe> findAllRecipes(Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findAll(pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                if (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) {
                    Image mainImage = recipe.getImages().get(mainIndex);
                    recipe.setImageBase64(mainImage.parseImage());
                } else if (!recipe.getImages().isEmpty()) {
                    recipe.setImageBase64(recipe.getImages().get(0).parseImage());
                }
            }
        });

        return recipes;
    }

    @Override
    public Recipe updateRecipe(Long id, RecipeDTO recipeDTO, List<ImageDTO> imageDTOs, User user) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Recipe with id %d not found", id)));

        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!existingRecipe.getAuthor().getId().equals(user.getId()) && !isAdmin) {
            throw new IllegalArgumentException("User not authorized to update this recipe");
        }

        existingRecipe.setTitle(recipeDTO.getTitle());
        existingRecipe.setDescription(recipeDTO.getDescription());
        existingRecipe.setPrimaryCategory(recipeDTO.getPrimaryCategory());
        existingRecipe.setNotes(recipeDTO.getNotes());
        existingRecipe.setPrepTime(recipeDTO.getPrepTime());
        existingRecipe.setCookTime(recipeDTO.getCookTime());
        existingRecipe.setServings(recipeDTO.getServings());

        existingRecipe.getTags().clear();
        if (recipeDTO.getTagNames() != null) {
            recipeDTO.getTagNames().forEach(tagName -> {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    existingRecipe.getTags().add(tagService.findOrCreateTag(tagName.trim()));
                }
            });
        }

        existingRecipe.getInstructions().clear();
        if (recipeDTO.getInstructions() != null) {
            int stepNumber = 1;
            for (String instruction : recipeDTO.getInstructions()) {
                if (instruction != null && !instruction.trim().isEmpty()) {
                    var step = new recipes.recipeBook.entity.RecipeStep();
                    step.setInstruction(instruction.trim());
                    step.setStepNumber(stepNumber++);
                    existingRecipe.getInstructions().add(step);
                }
            }
        }

        existingRecipe.getIngredients().clear();
        if (recipeDTO.getIngredients() != null) {
            for (var ingredientDTO : recipeDTO.getIngredients()) {
                var ingredient = new recipes.recipeBook.entity.Ingredient();
                ingredient.setName(ingredientDTO.getName());
                ingredient.setAmount(ingredientDTO.getAmount());
                ingredient.setUnit(ingredientDTO.getUnit());
                ingredient.setSection(ingredientDTO.getSection());
                existingRecipe.getIngredients().add(ingredient);
            }
        }

        existingRecipe.getImages().clear();
        if (imageDTOs != null && !imageDTOs.isEmpty()) {
            for (ImageDTO imageDTO : imageDTOs) {
                var image = new Image(imageDTO.getImageBytes(), imageDTO.getMimeType());
                image.setRecipe(existingRecipe);
                existingRecipe.getImages().add(image);
            }
            existingRecipe.setMainImageIndex(recipeDTO.getMainImageIndex());
        }

        existingRecipe.setVideoUrl(recipeDTO.getVideoUrl());

        return recipeRepository.save(existingRecipe);
    }

    @Override
    public Page<Recipe> findByCategory(RecipeCategory category, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByPrimaryCategory(category, pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                int indexToUse = (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) ? mainIndex : 0;
                recipe.setImageBase64(recipe.getImages().get(indexToUse).parseImage());
            }
        });

        return recipes;
    }

    @Override
    public long countRecipes() {
        return recipeRepository.count();
    }

    @Override
    public Page<Recipe> searchByTitle(String query, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(query, pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                int indexToUse = (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) ? mainIndex : 0;
                recipe.setImageBase64(recipe.getImages().get(indexToUse).parseImage());
            }
        });

        return recipes;
    }

    @Override
    public Page<Recipe> searchByCategoryAndTitle(RecipeCategory category, String query, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByPrimaryCategoryAndTitleContainingIgnoreCase(category, query, pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                int indexToUse = (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) ? mainIndex : 0;
                recipe.setImageBase64(recipe.getImages().get(indexToUse).parseImage());
            }
        });

        return recipes;
    }

    @Override
    public Page<Recipe> findMyRecipes(User author, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByAuthor(author, pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                int indexToUse = (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) ? mainIndex : 0;
                recipe.setImageBase64(recipe.getImages().get(indexToUse).parseImage());
            }
        });

        return recipes;
    }

    @Override
    public Page<Recipe> searchMyRecipesByTitle(User author, String query, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByAuthorAndTitleContainingIgnoreCase(author, query, pageable);

        recipes.forEach(recipe -> {
            if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
                Integer mainIndex = recipe.getMainImageIndex();
                int indexToUse = (mainIndex != null && mainIndex >= 0 && mainIndex < recipe.getImages().size()) ? mainIndex : 0;
                recipe.setImageBase64(recipe.getImages().get(indexToUse).parseImage());
            }
        });

        return recipes;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Recipe> findBookmarkedRecipes(User user, Pageable pageable) {
        return recipeRepository.findBookmarkedRecipesByUserId(user.getId(), pageable);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.removeRecipeFromAllBookmarks(id);
        recipeRepository.deleteById(id);
    }

    @Override
    public Page<Recipe> searchBookmarkedRecipesByTitle(User user, String query, Pageable pageable) {
        return recipeRepository.searchBookmarkedRecipesNative(user.getId(), query, pageable);
    }

    @Override
    public Page<Recipe> searchByFridge(List<String> ingredients, Pageable pageable) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        java.util.List<Recipe> matchedRecipes = null;

        for (String ingredient : ingredients) {
            if (ingredient != null && !ingredient.trim().isEmpty()) {
                java.util.List<Recipe> recipesForIngredient = recipeRepository.findRecipesContainingIngredient(ingredient.trim());
                if (matchedRecipes == null) {
                    matchedRecipes = new java.util.ArrayList<>(recipesForIngredient);
                } else {
                    matchedRecipes.retainAll(recipesForIngredient);
                }
            }
        }

        if (matchedRecipes == null) {
            matchedRecipes = new java.util.ArrayList<>();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchedRecipes.size());
        java.util.List<Recipe> pageContent = start <= end ? matchedRecipes.subList(start, end) : new java.util.ArrayList<>();

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedRecipes.size());
    }
}
