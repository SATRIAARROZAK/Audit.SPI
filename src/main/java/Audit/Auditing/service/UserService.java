package Audit.Auditing.service;

import Audit.Auditing.dto.ProfileDto;
import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(UserDto userDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllUsers();
    Optional<User> findById(Long id); // Add this for finding user to update/delete
    User updateUser(Long id, UserDto userDto); // Add this to update a user
    void deleteUser(Long id); // Add this to delete a user

    // Metode baru untuk update profil
    User updateProfile(String username, ProfileDto profileDto);
}