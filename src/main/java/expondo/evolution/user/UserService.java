package expondo.evolution.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public AppUser findOrCreate(String oid, String email, String displayName) {
        return userRepository.findByMicrosoftOid(oid)
                .map(user -> {
                    user.setLastLoginAt(Instant.now());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setMicrosoftOid(oid);
                    newUser.setEmail(email);
                    newUser.setDisplayName(displayName);
                    newUser.setEnabled(true);
                    newUser.setRoles(Set.of(AppRole.USER));
                    newUser.setLastLoginAt(Instant.now());
                    return userRepository.save(newUser);
                });
    }
}