package llc.devlab.app.model.dto.common;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        Long nextCursorId,
        boolean hasNextPage
) {
}
