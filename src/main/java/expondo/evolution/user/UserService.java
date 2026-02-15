package expondo.evolution.user;

import expondo.evolution.user.dto.UserDto;
import expondo.evolution.user.dto.UserRoleUpdateDto;
import expondo.evolution.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
