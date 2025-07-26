package recipes.recipeBook.service;

import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.entity.User;

public interface UserService {
    User createUser(UserDTO userDTO);

    User getUserByUsername(String username);
}
