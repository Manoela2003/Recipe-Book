package recipes.recipeBook.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean isPredefined = false;

    @ManyToMany(mappedBy = "tags")
    private List<Recipe> recipes = new ArrayList<>();

    public Tag() {}

    public Tag(String name, boolean isPredefined) {
        this.name = name;
        this.isPredefined = isPredefined;
    }
}