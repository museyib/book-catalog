package llc.devlab.app.service;

import java.util.List;
import llc.devlab.app.exception.ConflictException;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.mapper.AuthorMapper;
import llc.devlab.app.model.dto.reference.AuthorRequest;
import llc.devlab.app.model.dto.reference.AuthorResponse;
import llc.devlab.app.model.entity.Author;
import llc.devlab.app.repository.AuthorRepository;
import llc.devlab.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuthorMapper authorMapper;

    @Transactional(readOnly = true)
    public List<AuthorResponse> list() {
        return authorRepository.findAllByOrderByNameAsc()
                .stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponse get(Long id) {
        return authorMapper.toResponse(loadAuthor(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        Author author = new Author();
        author.setName(request.name().trim());
        author.setBiography(blankToNull(request.biography()));
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = loadAuthor(id);
        author.setName(request.name().trim());
        author.setBiography(blankToNull(request.biography()));
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Transactional
    public void delete(Long id) {
        Author author = loadAuthor(id);
        if (bookRepository.existsByAuthors_Id(id)) {
            throw new ConflictException("Author cannot be deleted while books still reference it.");
        }
        authorRepository.delete(author);
    }

    private Author loadAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id %d was not found.".formatted(id)));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
