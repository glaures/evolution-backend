package expondo.evolution.user;

import expondo.evolution.user.dto.UserDto;
import expondo.evolution.user.dto.UserRoleUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRoles(@PathVariable Long id, @RequestBody UserRoleUpdateDto dto) {
        return userService.updateRoles(id, dto);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public AppRole[] getAvailableRoles() {
        return AppRole.values();
    }
}
