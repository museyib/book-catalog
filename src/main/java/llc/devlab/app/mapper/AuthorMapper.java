package llc.devlab.app.mapper;

import llc.devlab.app.model.dto.reference.AuthorResponse;
import llc.devlab.app.model.entity.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getName(),
                author.getBiography(),
                author.getCreatedAt(),
                author.getUpdatedAt()
        );
    }
}
