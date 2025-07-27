package recipes.recipeBook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.dto.mapper.RecipeDTOToRecipeMapper;
import recipes.recipeBook.entity.Image;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.exception.DuplicateRecipeException;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.service.ImageService;
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

        Recipe recipe = RecipeDTOToRecipeMapper.map(recipeDTO, user, tagService);
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
}
