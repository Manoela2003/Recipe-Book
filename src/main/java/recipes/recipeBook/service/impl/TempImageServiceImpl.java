package recipes.recipeBook.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import recipes.recipeBook.dto.TempImageDTO;
import recipes.recipeBook.service.TempImageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempImageServiceImpl implements TempImageService {
    private final Map<String, TempImageDTO> tempImages = new ConcurrentHashMap<>();

    @Override
    public TempImageDTO storeTemp(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        try {
            TempImageDTO tempImage = new TempImageDTO(id, file.getContentType(), file.getBytes());
            tempImages.put(id, tempImage);
            return tempImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store temporary image", e);
        }
    }

    @Override
    public TempImageDTO getTempImage(String id) {
        return tempImages.get(id);
    }

    @Override
    public void deleteTempImage(String id) {
        tempImages.remove(id);
    }

    @Override
    public List<TempImageDTO> getAllByIds(List<String> ids) {
        List<TempImageDTO> result = new ArrayList<>();
        for (String id : ids) {
            TempImageDTO img = getTempImage(id);
            if (img != null) result.add(img);
        }
        return result;
    }
}
