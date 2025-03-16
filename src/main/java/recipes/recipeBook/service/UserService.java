package recipes.recipeBook.service;

import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.entity.User;

public interface UserService {
    public User createUser(UserDTO userDTO);
}
