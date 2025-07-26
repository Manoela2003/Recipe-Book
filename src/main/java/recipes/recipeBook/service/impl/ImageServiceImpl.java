package recipes.recipeBook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.entity.Image;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.repository.ImageRepository;
import recipes.recipeBook.service.ImageService;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Image with id %s not found", id)));
    }

    @Override
    public Image createImage(ImageDTO imageDto, Recipe recipe) {
        Image imageToBeSaved = new Image(imageDto.getImageBytes(), imageDto.getMimeType(), recipe);
        return imageRepository.save(imageToBeSaved);
    }
}
