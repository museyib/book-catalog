package llc.devlab.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "book", uniqueConstraints = {
        @UniqueConstraint(name = "uk_book_isbn", columnNames = "isbn")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"authors", "category"})
public class Book extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(length = 4_000)
    private String description;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Books own the join table because authors are shared across the catalog and must not be deleted transitively.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
}
