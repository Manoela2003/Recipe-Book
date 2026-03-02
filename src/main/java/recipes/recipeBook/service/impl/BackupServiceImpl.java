package recipes.recipeBook.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.dto.mapper.RecipeMapper;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.service.BackupService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupServiceImpl implements BackupService {
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    public BackupServiceImpl(RecipeRepository recipeRepository, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public void performServerBackup() throws Exception {
        String json = generateBackupJson();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        File backupFile = new File("recipe_backup_" + timestamp + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, objectMapper.readTree(json));
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(readOnly = true)
    public void scheduledBackup() {
        try {
            performServerBackup();
        } catch (Exception e) {
            System.err.println("Scheduled backup failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateBackupJson() throws JsonProcessingException {
        List<Recipe> recipes = recipeRepository.findAll();
        List<RecipeDTO> recipeDTOs = recipes.stream()
                .map(RecipeMapper::mapToRecipeDTO)
                .collect(Collectors.toList());
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(recipeDTOs);
    }
}