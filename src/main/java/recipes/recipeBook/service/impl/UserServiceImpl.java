package recipes.recipeBook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.recipeBook.dto.UserDTO;
import recipes.recipeBook.dto.mapper.UserDTOToUserMapper;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.exception.DuplicateEmailException;
import recipes.recipeBook.exception.DuplicateUsernameException;
import recipes.recipeBook.repository.UserRepository;
import recipes.recipeBook.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new DuplicateUsernameException(userDTO.getUsername());
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateEmailException(userDTO.getEmail());
        }

        return userRepository.save(UserDTOToUserMapper.map(userDTO));
    }
}
