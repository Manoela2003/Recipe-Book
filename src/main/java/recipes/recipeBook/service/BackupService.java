package recipes.recipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface BackupService {
    void scheduledBackup();
    void performServerBackup() throws Exception;
    String generateBackupJson() throws JsonProcessingException;
}