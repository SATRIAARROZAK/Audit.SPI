package Audit.Auditing.service;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(UserDto userDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllUsers();
}