package recipes.recipeBook.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;

    @ManyToOne
    private User author;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @OrderBy("stepNumber ASC")
    private List<RecipeStep> instructions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();
    private Integer mainImageIndex;

    private long prepTime;
    private long cookTime;
    private int servings;

    @Transient
    private String imageBase64;
    private Date createdOn;

    public Image getMainImage() {
        if (mainImageIndex != null && images != null && mainImageIndex >= 0 && mainImageIndex < images.size()) {
            return images.get(mainImageIndex);
        }
        return null;
    }
}
