package recipes.recipeBook.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import recipes.recipeBook.dto.mapper.RecipeMapper;
import recipes.recipeBook.entity.CustomUserDetails;
import recipes.recipeBook.entity.Image;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.entity.RecipeCategory;
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
    public String viewRecipe(@PathVariable("id") Long id, Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request) {
        Recipe recipe = recipeService.findRecipeById(id);
        model.addAttribute("recipe", recipe);

        boolean isAuthor = false;
        boolean isAdmin = false;

        if (userDetails != null) {
            if (recipe.getAuthor() != null) {
                isAuthor = recipe.getAuthor().getId().equals(userDetails.getUser().getId());
            }
            isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        model.addAttribute("isAuthor", isAuthor || isAdmin);

        String referer = request.getHeader("Referer");
        String backUrl = "/recipes";
        String backText = "All Recipes";

        if (referer != null && !referer.contains("/edit/") && !referer.contains("/add")) {
            backUrl = referer;

            if (referer.contains("/my-recipes")) {
                backText = "My Recipes";
            } else if (referer.contains("/home") || referer.endsWith("/")) {
                backText = "Home";
            } else if (referer.contains("query=") || referer.contains("category=")) {
                backText = "Search Results";
            }
        }

        model.addAttribute("backUrl", backUrl);
        model.addAttribute("backText", backText);

        return "recipe-details";
    }

    @GetMapping("/edit/{id}")
    public String editRecipe(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Recipe recipe = recipeService.findRecipeById(id);

        if (recipe == null) {
            redirectAttributes.addFlashAttribute("message", "Recipe not found");
            return "redirect:/recipes";
        }

        if (!recipe.getAuthor().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this recipe.");
            return "redirect:/recipes/" + id;
        }

        List<TempImageDTO> tempImages = new ArrayList<>();
        if (recipe.getImages() != null) {
            for (Image img : recipe.getImages()) {
                TempImageDTO temp = new TempImageDTO();
                temp.setId(img.getId().toString());
                temp.setData(img.getImage());
                temp.setMimeType(img.getMimeType());
                tempImages.add(temp);
            }
        }
        model.addAttribute("tempImages", tempImages);

        model.addAttribute("formMode", "edit");
        model.addAttribute("formAction", "/recipes/edit/" + recipe.getId());
        model.addAttribute("recipe", RecipeMapper.mapToRecipeDTO(recipe));
        return "add-recipe";
    }

    @PostMapping("/edit/{id}")
    public String updateRecipe(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("recipe") RecipeDTO recipeDTO,
                               BindingResult bindingResult,
                               @RequestParam("images") MultipartFile[] images,
                               @RequestParam(value = "tempImageIds", required = false) List<String> tempImageIds,
                               @RequestParam(value = "mainImageIndex", required = false) String mainImageIndexParam,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<String> filteredSteps = recipeDTO.getInstructions() == null ? new ArrayList<>() :
                recipeDTO.getInstructions().stream()
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .toList();
        recipeDTO.setInstructions(filteredSteps);

        List<IngredientDTO> filteredIngredients = recipeDTO.getIngredients() == null ? new ArrayList<>() :
                recipeDTO.getIngredients().stream()
                        .filter(i -> i != null && i.getName() != null && !i.getName().trim().isEmpty())
                        .toList();
        recipeDTO.setIngredients(filteredIngredients);

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "edit");
            model.addAttribute("formAction", "/recipes/edit/" + id);

            Recipe recipe = recipeService.findRecipeById(id);
            List<TempImageDTO> tempImages = new ArrayList<>();
            if (recipe.getImages() != null) {
                for (Image img : recipe.getImages()) {
                    TempImageDTO temp = new TempImageDTO();
                    temp.setId(img.getId().toString());
                    temp.setData(img.getImage());
                    temp.setMimeType(img.getMimeType());
                    tempImages.add(temp);
                }
            }
            model.addAttribute("tempImages", tempImages);
            return "add-recipe";
        }

        List<ImageDTO> imageDTOs = new ArrayList<>();
        if (tempImageIds != null) {
            for (String imageId : tempImageIds) {
                try {
                    TempImageDTO temp = imageService.getTempImage(imageId);
                    if (temp != null) {
                        imageDTOs.add(new ImageDTO(temp.getData(), temp.getMimeType()));
                    } else {
                        Recipe current = recipeService.findRecipeById(id);
                        current.getImages().stream()
                                .filter(img -> img.getId().toString().equals(imageId))
                                .findFirst()
                                .ifPresent(img -> imageDTOs.add(new ImageDTO(img.getImage(), img.getMimeType())));
                    }
                } catch (Exception e) {}
            }
        }

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                try {
                    imageDTOs.add(new ImageDTO(file.getBytes(), file.getContentType()));
                } catch (Exception e) {}
            }
        }

        if (mainImageIndexParam != null && !mainImageIndexParam.isEmpty()) {
            recipeDTO.setMainImageIndex(Integer.parseInt(mainImageIndexParam));
        }

        recipeService.updateRecipe(id, recipeDTO, imageDTOs, userDetails.getUser());
        return "redirect:/recipes/" + id;
    }

    @GetMapping("/add")
    public String showAddRecipeForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("recipe")) {
            RecipeDTO recipeDTO = new RecipeDTO();
            recipeDTO.setIngredients(List.of(new IngredientDTO()));
            recipeDTO.setInstructions(List.of(""));
            model.addAttribute("recipe", recipeDTO);
        } else {
            RecipeDTO recipeDTO = (RecipeDTO) model.getAttribute("recipe");

            if (recipeDTO != null && recipeDTO.getInstructions() != null) {
                recipeDTO.setInstructions(
                        recipeDTO.getInstructions().stream()
                                .filter(step -> step != null && !step.trim().isEmpty())
                                .toList()
                );
            }

            if (recipeDTO != null && recipeDTO.getIngredients() != null) {
                recipeDTO.setIngredients(recipeDTO.getIngredients().stream()
                        .filter(ingredient -> ingredient.getName() != null)
                        .toList());
            }

            model.addAttribute("recipe", recipeDTO);
        }
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("formMode", "add");
        model.addAttribute("formAction", "/recipes/add");
        model.addAttribute("tempImages", new ArrayList<TempImageDTO>());
        return "add-recipe";
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

    @GetMapping
    public String getAllRecipes(
            @PageableDefault(size = 9) Pageable pageable,
            @RequestParam(value = "category", required = false) RecipeCategory category,
            @RequestParam(value = "query", required = false) String query,
            Model model,
            HttpServletRequest request) {

        Page<Recipe> recipes;

        if (query != null && !query.trim().isEmpty() && category != null) {
            recipes = recipeService.searchByCategoryAndTitle(category, query.trim(), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            recipes = recipeService.searchByTitle(query.trim(), pageable);
        } else if (category != null) {
            recipes = recipeService.findByCategory(category, pageable);
        } else {
            recipes = recipeService.findAllRecipes(pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", query);
        model.addAttribute("requestURI", request.getRequestURI());
        return "recipes-list";
    }

    @GetMapping("/my-recipes")
    public String getMyRecipes(
            @PageableDefault(size = 9) Pageable pageable,
            @RequestParam(value = "query", required = false) String query,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            HttpServletRequest request) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Page<Recipe> recipes;
        if (query != null && !query.trim().isEmpty()) {
            recipes = recipeService.searchMyRecipesByTitle(userDetails.getUser(), query.trim(), pageable);
        } else {
            recipes = recipeService.findMyRecipes(userDetails.getUser(), pageable);
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("isMyRecipes", true);
        model.addAttribute("searchQuery", query);
        model.addAttribute("requestURI", request.getRequestURI());

        return "recipes-list";
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
