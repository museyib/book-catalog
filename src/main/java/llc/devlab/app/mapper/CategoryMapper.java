package llc.devlab.app.mapper;

import llc.devlab.app.model.dto.reference.CategoryResponse;
import llc.devlab.app.model.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
