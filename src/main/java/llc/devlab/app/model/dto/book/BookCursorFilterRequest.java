package llc.devlab.app.model.dto.book;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCursorFilterRequest extends BookFilterRequest {
    private Long cursor;
}
