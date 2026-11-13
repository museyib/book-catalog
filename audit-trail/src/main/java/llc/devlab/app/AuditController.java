package llc.devlab.app;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    public List<AuditEventDto> getEvents(@Valid @ModelAttribute AuditFilterRequest filter) {
        log.info("Received audit filter request: {}", filter);
        return auditService.getEvents(filter);
    }
}
