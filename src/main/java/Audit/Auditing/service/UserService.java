package Audit.Auditing.service;

import Audit.Auditing.dto.ProfileDto;
import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(UserDto userDto);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllUsers(); // Keep if some parts still need it without pagination

    Page<User> findAllUsers(Pageable pageable); // New method for paginated user list

    Page<User> searchUsers(String keyword, Pageable pageable); // New method for searching users

    Optional<User> findById(Long id);

    User updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    User updateProfile(String username, ProfileDto profileDto);
}