package llc.devlab.app.security;

import llc.devlab.app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return accountRepository.findByEmailIgnoreCase(username)
                .map(AccountPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found."));
    }
}
