package llc.devlab.app.controller;

import jakarta.validation.Valid;
import java.util.List;
import llc.devlab.app.model.dto.reference.AuthorRequest;
import llc.devlab.app.model.dto.reference.AuthorResponse;
import llc.devlab.app.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    ResponseEntity<List<AuthorResponse>> list() {
        return ResponseEntity.ok(authorService.list());
    }

    @GetMapping("/{id}")
    ResponseEntity<AuthorResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.get(id));
    }

    @PostMapping
    ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    @PutMapping("/{id}")
    ResponseEntity<AuthorResponse> update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(authorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
