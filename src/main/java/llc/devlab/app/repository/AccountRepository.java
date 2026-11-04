package llc.devlab.app.repository;

import java.util.Optional;
import llc.devlab.app.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Account> findByEmailIgnoreCase(String email);
}
