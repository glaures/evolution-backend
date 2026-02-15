package expondo.evolution.user.mapper;

import expondo.evolution.user.AppUser;
import expondo.evolution.user.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(AppUser entity);
}
