package recipes.recipeBook.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import recipes.recipeBook.service.BackupService;
import recipes.recipeBook.service.RecipeService;
import recipes.recipeBook.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BackupService backupService;
    private final RecipeService recipeService;
    private final UserService userService;

    public AdminController(BackupService backupService, RecipeService recipeService, UserService userService) {
        this.backupService = backupService;
        this.recipeService = recipeService;
        this.userService = userService;
    }

    @GetMapping
    public String adminDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("totalRecipes", recipeService.countRecipes());
        model.addAttribute("totalUsers", userService.countUsers());
        return "admin-dashboard";
    }

    @PostMapping("/backup/server")
    public String triggerServerBackup(RedirectAttributes redirectAttributes) {
        try {
            backupService.performServerBackup();
            redirectAttributes.addFlashAttribute("successMessage", "Database backed up successfully to the server filesystem.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save backup to server: " + e.getMessage());
        }
        return "redirect:/admin";
    }
    @PostMapping("/backup/download")
    public ResponseEntity<byte[]> downloadBackup() {
        try {
            String json = backupService.generateBackupJson();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filename = "recipe_backup_" + timestamp + ".json";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/backup/restore")
    public String restoreBackup(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/admin";
        }

        try {
            backupService.restoreDatabase(file);
            redirectAttributes.addFlashAttribute("successMessage", "Database successfully restored from backup.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to restore database: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}