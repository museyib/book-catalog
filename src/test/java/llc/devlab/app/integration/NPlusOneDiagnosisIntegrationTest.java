package llc.devlab.app.integration;

import static org.assertj.core.api.Assertions.assertThat;

import llc.devlab.app.model.dto.book.BookFilterRequest;
import llc.devlab.app.service.BookService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.seed.enabled=true")
@ActiveProfiles("test")
class NPlusOneDiagnosisIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Test
    void optimizedFetchPlanReducesPreparedStatements() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        bookService.runNPlusOneDiagnosis(0, 20);
        long nPlusOneQueryCount = statistics.getPrepareStatementCount();

        statistics.clear();
        bookService.list(0, 20, "title:asc", new BookFilterRequest());
        long optimizedQueryCount = statistics.getPrepareStatementCount();

        System.out.printf(
                "N+1 diagnosis: before=%d prepared statements, after=%d prepared statements%n",
                nPlusOneQueryCount,
                optimizedQueryCount
        );

        assertThat(nPlusOneQueryCount).isPositive();
        assertThat(optimizedQueryCount).isPositive();
        assertThat(nPlusOneQueryCount).isGreaterThan(optimizedQueryCount);
    }
}
