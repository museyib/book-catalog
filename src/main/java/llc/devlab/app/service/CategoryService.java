package llc.devlab.app.service;

import java.util.List;
import llc.devlab.app.exception.ConflictException;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.mapper.CategoryMapper;
import llc.devlab.app.model.dto.reference.CategoryRequest;
import llc.devlab.app.model.dto.reference.CategoryResponse;
import llc.devlab.app.model.entity.Category;
import llc.devlab.app.repository.BookRepository;
import llc.devlab.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return categoryMapper.toResponse(loadCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Category name must be unique.");
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Category name must be unique.");
        }
        Category category = loadCategory(id);
        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = loadCategory(id);
        if (bookRepository.existsByCategory_Id(id)) {
            throw new ConflictException("Category cannot be deleted while books still reference it.");
        }
        categoryRepository.delete(category);
    }

    public Category loadCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id %d was not found.".formatted(id)));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
