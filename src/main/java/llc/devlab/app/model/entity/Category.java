package llc.devlab.app.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "books")
public class Category extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1_000)
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();
}
