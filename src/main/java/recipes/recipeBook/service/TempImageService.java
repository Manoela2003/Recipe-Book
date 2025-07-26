package recipes.recipeBook.service;

import org.springframework.web.multipart.MultipartFile;
import recipes.recipeBook.dto.TempImageDTO;

import java.util.List;

public interface TempImageService {
    TempImageDTO storeTemp(MultipartFile file);
    TempImageDTO getTempImage(String id);
    void deleteTempImage(String id);
    List<TempImageDTO> getAllByIds(List<String> ids);
}
