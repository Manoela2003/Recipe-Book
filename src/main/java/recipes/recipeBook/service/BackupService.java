package recipes.recipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

public interface BackupService {
    void scheduledBackup();
    void performServerBackup() throws Exception;
    String generateBackupJson() throws JsonProcessingException;
    void restoreDatabase(MultipartFile file) throws Exception;
}