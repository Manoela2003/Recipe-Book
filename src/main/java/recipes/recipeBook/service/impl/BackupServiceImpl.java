package recipes.recipeBook.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import recipes.recipeBook.dto.BackupDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.dto.UserBackupDTO;
import recipes.recipeBook.dto.mapper.RecipeMapper;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.repository.RecipeRepository;
import recipes.recipeBook.repository.UserRepository;
import recipes.recipeBook.service.BackupService;
import recipes.recipeBook.service.TagService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupServiceImpl implements BackupService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;

    public BackupServiceImpl(RecipeRepository recipeRepository, UserRepository userRepository, ObjectMapper objectMapper, TagService tagService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.tagService = tagService;
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
    public void performServerBackup() throws Exception {
        String json = generateBackupJson();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        File backupFile = new File("recipe_backup_" + timestamp + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, objectMapper.readTree(json));
    }

    @Override
    @Transactional(readOnly = true)
    public String generateBackupJson() throws JsonProcessingException {
        List<User> users = userRepository.findAll();
        List<UserBackupDTO> userDTOs = users.stream().map(user -> {
            UserBackupDTO dto = new UserBackupDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setPassword(user.getPassword());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(user.getRole());
            return dto;
        }).collect(Collectors.toList());

        List<Recipe> recipes = recipeRepository.findAll();
        List<RecipeDTO> recipeDTOs = recipes.stream()
                .map(RecipeMapper::mapToRecipeDTO)
                .collect(Collectors.toList());

        BackupDTO backup = new BackupDTO();
        backup.setUsers(userDTOs);
        backup.setRecipes(recipeDTOs);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup);
    }

    @Override
    @Transactional
    public void restoreDatabase(MultipartFile file) throws Exception {
        BackupDTO backup = objectMapper.readValue(file.getInputStream(), BackupDTO.class);

        if (backup.getUsers() != null) {
            for (UserBackupDTO userDTO : backup.getUsers()) {
                if (!userRepository.existsByUsername(userDTO.getUsername())) {
                    User user = new User();
                    user.setUsername(userDTO.getUsername());
                    user.setPassword(userDTO.getPassword());
                    user.setEmail(userDTO.getEmail());
                    user.setFirstName(userDTO.getFirstName());
                    user.setLastName(userDTO.getLastName());
                    user.setRole(userDTO.getRole());
                    userRepository.save(user);
                }
            }
        }

        if (backup.getRecipes() != null) {
            for (RecipeDTO recipeDTO : backup.getRecipes()) {
                if (recipeDTO.getId() != null && recipeRepository.existsById(recipeDTO.getId())) {
                    continue;
                }

                User author = userRepository.findByUsername(recipeDTO.getAuthorUsername())
                        .orElseThrow(() -> new RuntimeException("Author missing: " + recipeDTO.getAuthorUsername()));

                Recipe recipe = RecipeMapper.mapToRecipe(recipeDTO, author, tagService);
                recipeRepository.save(recipe);
            }
        }
    }
}