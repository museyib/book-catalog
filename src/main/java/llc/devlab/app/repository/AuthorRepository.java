package llc.devlab.app.repository;

import java.util.List;
import llc.devlab.app.model.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findAllByOrderByNameAsc();
}
