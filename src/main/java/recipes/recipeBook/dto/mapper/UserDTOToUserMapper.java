package recipes.recipeBook.dto.mapper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.entity.User;

public class UserDTOToUserMapper {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static User map(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        return user;
    }
}
