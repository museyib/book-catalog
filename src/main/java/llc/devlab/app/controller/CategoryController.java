package llc.devlab.app.controller;

import jakarta.validation.Valid;
import java.util.List;
import llc.devlab.app.model.dto.reference.CategoryRequest;
import llc.devlab.app.model.dto.reference.CategoryResponse;
import llc.devlab.app.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    List<CategoryResponse> list() {
        return categoryService.list();
    }

    @GetMapping("/{id}")
    CategoryResponse get(@PathVariable Long id) {
        return categoryService.get(id);
    }

    @PostMapping
    ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
