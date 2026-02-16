package expondo.evolution.user;

import expondo.evolution.user.dto.UserDto;
import expondo.evolution.user.dto.UserRoleUpdateDto;
import expondo.evolution.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${evolution.auto-admins:}")
    private String autoAdminEmails;

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
                    newUser.setLastLoginAt(Instant.now());

                    if (isFirstUser()) {
                        log.info("First user registered: {} — granting ADMIN role", email);
                        newUser.setEnabled(true);
                        newUser.setRoles(Set.of(AppRole.ADMIN, AppRole.USER));
                    } else if (isAutoAdmin(email)) {
                        log.info("Auto-admin user registered: {} — granting ADMIN role", email);
                        newUser.setEnabled(true);
                        newUser.setRoles(Set.of(AppRole.ADMIN, AppRole.USER));
                    } else {
                        newUser.setEnabled(true);
                        newUser.setRoles(Set.of(AppRole.USER));
                    }

                    return userRepository.save(newUser);
                });
    }

    private boolean isFirstUser() {
        return userRepository.count() == 0;
    }

    private boolean isAutoAdmin(String email) {
        if (email == null || autoAdminEmails == null || autoAdminEmails.isBlank()) {
            return false;
        }
        return List.of(autoAdminEmails.split(","))
                .stream()
                .map(String::trim)
                .anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email));
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public UserDto updateRoles(Long id, UserRoleUpdateDto dto) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setRoles(dto.roles());
        user.setEnabled(dto.enabled());
        return userMapper.toDto(userRepository.save(user));
    }
}