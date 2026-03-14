package recipes.recipeBook.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Enumerated(EnumType.STRING)
    private RecipeCategory primaryCategory;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "recipe_tags",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @OrderBy("stepNumber ASC")
    private List<RecipeStep> instructions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images = new ArrayList<>();
    private Integer mainImageIndex;

    private long prepTime;
    private long cookTime;
    private int servings;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Transient
    private String imageBase64;
    private Date createdOn;

    @Column(name = "video_url")
    private String videoUrl;

    public Image getMainImage() {
        if (mainImageIndex != null && images != null && mainImageIndex >= 0 && mainImageIndex < images.size()) {
            return images.get(mainImageIndex);
        }
        return null;
    }

    @Transient
    public String getImageBase64() {
        Image mainImage = getMainImage();
        return (mainImage != null) ? mainImage.parseImage() : null;
    }

    public String getImageUrl() {
        String fullBase64String = getImageBase64();
        if (fullBase64String == null || fullBase64String.isEmpty()) {
            return "/images/cover-image-recipes.png";
        }
        return fullBase64String;
    }
}
