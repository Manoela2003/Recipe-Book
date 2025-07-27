package recipes.recipeBook.web;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import recipes.recipeBook.dto.ImageDTO;
import recipes.recipeBook.dto.IngredientDTO;
import recipes.recipeBook.dto.RecipeDTO;
import recipes.recipeBook.dto.TempImageDTO;
import recipes.recipeBook.entity.CustomUserDetails;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.exception.DuplicateRecipeException;
import recipes.recipeBook.exception.NotFoundException;
import recipes.recipeBook.service.RecipeService;
import recipes.recipeBook.service.TempImageService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final TempImageService imageService;

    @Autowired
    public RecipeController(RecipeService recipeService, TempImageService imageService) {
        this.recipeService = recipeService;
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public String viewRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findRecipeById(id);
        model.addAttribute("recipe", recipe);
        return "recipe-details";
    }

    @GetMapping("/add")
    public String showAddRecipeForm(Model model) {
        if (!model.containsAttribute("recipe")) {
            RecipeDTO recipeDTO = new RecipeDTO();
            recipeDTO.setIngredients(List.of(new IngredientDTO()));
            recipeDTO.setInstructions(List.of(""));
            model.addAttribute("recipe", recipeDTO);
        } else {
            RecipeDTO recipeDTO = (RecipeDTO) model.getAttribute("recipe");

            if (recipeDTO.getInstructions() != null) {
                recipeDTO.setInstructions(
                        recipeDTO.getInstructions().stream()
                                .filter(step -> step != null && !step.trim().isEmpty())
                                .toList()
                );
            }

            if (recipeDTO.getIngredients() != null) {
                recipeDTO.setIngredients(recipeDTO.getIngredients().stream()
                        .filter(ingredient -> ingredient.getName() != null)
                        .toList());
            }

            model.addAttribute("recipe", recipeDTO);
        }

        return "add-recipe2";
    }

    @PostMapping("/add")
    public String addRecipe(@Valid @ModelAttribute("recipe") RecipeDTO recipeDTO,
                            BindingResult bindingResult,
                            @RequestParam("images") MultipartFile[] images,
                            @RequestParam(value = "tempImageIndexes", required = false) List<Integer> tempImageIndexes,
                            @RequestParam(value = "tempImageIds", required = false) List<String> tempImageIds,
                            @RequestParam(value = "mainImageIndex", required = false) String mainImageIndexParam,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<TempImageDTO> existingTempImages = new ArrayList<>();
        if (tempImageIds != null && !tempImageIds.isEmpty()) {
            existingTempImages.addAll(imageService.getAllByIds(tempImageIds));
        }

        List<TempImageDTO> newTempImages = new ArrayList<>();
        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty() && file.getSize() > 0) {
                newTempImages.add(imageService.storeTemp(file));
            }
        }

        List<TempImageDTO> allTempImages = new ArrayList<>();
        allTempImages.addAll(existingTempImages);
        allTempImages.addAll(newTempImages);

        List<TempImageDTO> selectedTempImages;
        if (tempImageIndexes != null && !tempImageIndexes.isEmpty() && !existingTempImages.isEmpty()) {
            selectedTempImages = new ArrayList<>();
            for (Integer index : tempImageIndexes) {
                if (index >= 0 && index < existingTempImages.size()) {
                    selectedTempImages.add(existingTempImages.get(index));
                }
            }
            selectedTempImages.addAll(newTempImages);
        } else {
            selectedTempImages = allTempImages;
        }

        if (mainImageIndexParam != null && !mainImageIndexParam.trim().isEmpty()) {
            try {
                int mainIndex = Integer.parseInt(mainImageIndexParam);
                recipeDTO.setMainImageIndex(mainIndex);
            } catch (NumberFormatException e) {
                recipeDTO.setMainImageIndex(0);
            }
        } else if (!selectedTempImages.isEmpty()) {
            recipeDTO.setMainImageIndex(0);
        }

        if (bindingResult.hasErrors()) {
            List<String> tempIds = selectedTempImages.stream()
                    .map(TempImageDTO::getId)
                    .collect(Collectors.toList());

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.recipe", bindingResult);
            redirectAttributes.addFlashAttribute("recipe", recipeDTO);
            redirectAttributes.addFlashAttribute("tempImages", selectedTempImages);
            redirectAttributes.addFlashAttribute("tempImageIds", tempIds);

            return "redirect:/recipes/add";
        }

        List<ImageDTO> imageDTOs = selectedTempImages.stream()
                .map(temp -> new ImageDTO(temp.getData(), temp.getMimeType()))
                .collect(Collectors.toCollection(ArrayList::new));

        Recipe recipe = recipeService.save(recipeDTO, imageDTOs, userDetails.getUser());

        selectedTempImages.forEach(img -> imageService.deleteTempImage(img.getId()));

        redirectAttributes.addFlashAttribute("successMessage", "Recipe added successfully!");
        return "redirect:/recipes/" + recipe.getId();
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleDuplicatedEntityException(NotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/home";
    }

    @ExceptionHandler(DuplicateRecipeException.class)
    public String handleDuplicateRecipeException(DuplicateRecipeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/recipes/add";
    }
}
