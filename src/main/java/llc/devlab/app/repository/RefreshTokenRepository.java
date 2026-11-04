package llc.devlab.app.repository;

import java.util.UUID;
import llc.devlab.app.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
}
