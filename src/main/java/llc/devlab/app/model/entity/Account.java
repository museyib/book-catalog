package llc.devlab.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import llc.devlab.app.model.enums.Role;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "account", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "refreshTokens")
public class Account extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens = new HashSet<>();
}
