package recipes.recipeBook.service;

import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.entity.Image;
import recipes.recipeBook.entity.Recipe;

public interface ImageService {

    Image getImageById(Long id);
    Image createImage(ImageDTO image, Recipe recipe);
}
