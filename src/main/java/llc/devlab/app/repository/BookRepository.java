package llc.devlab.app.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import llc.devlab.app.model.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, Long id);

    boolean existsByAuthors_Id(Long authorId);

    boolean existsByCategory_Id(Long categoryId);

    @EntityGraph(attributePaths = {"authors", "category"})
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findDetailedById(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT b
            FROM Book b
            LEFT JOIN FETCH b.authors
            LEFT JOIN FETCH b.category
            WHERE b.id IN :ids
            """)
    List<Book> findAllWithAuthorsAndCategoryByIdIn(@Param("ids") Collection<Long> ids);
}
