package expondo.evolution.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public AppUser findOrCreate(String oid, String email, String displayName) {
        return userRepository.findByMicrosoftOid(oid)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setMicrosoftOid(oid);
                    newUser.setEmail(email);
                    newUser.setDisplayName(displayName);
                    newUser.setEnabled(false);
                    return userRepository.save(newUser);
                });
    }
}