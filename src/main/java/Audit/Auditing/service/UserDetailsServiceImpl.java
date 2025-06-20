package Audit.Auditing.service;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.dto.ProfileDto;
import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.Role;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
        
        return new CustomUserDetails(user);
    }

    @Override
    public User saveUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        String roleStr = userDto.getRole().toLowerCase();
        user.setRole(Role.valueOf(roleStr));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        // Assuming you add a search method to UserRepository, e.g.:
        // @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        // Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
        // For simplicity, for now, if no specific search method for users, you might fetch all and filter in memory (not ideal for large datasets)
        // or add the @Query as shown above to your UserRepository.
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword, keyword, pageable);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        user.setRole(Role.valueOf(userDto.getRole().toLowerCase()));

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateProfile(String username, ProfileDto profileDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        if (profileDto.getPhoto() != null && !profileDto.getPhoto().isEmpty()) {
            String fileName = fileStorageService.storeFile(profileDto.getPhoto());
            user.setPhotoPath(fileName);
        }

        if (profileDto.getSignatureImage() != null && !profileDto.getSignatureImage().isEmpty()) {
            String fileName = fileStorageService.storeFile(profileDto.getSignatureImage());
            user.setSignaturePath(fileName);
        } else if (profileDto.getSignatureDataUrl() != null && !profileDto.getSignatureDataUrl().isEmpty()) {
            String fileName = fileStorageService.storeBase64File(profileDto.getSignatureDataUrl());
            user.setSignaturePath(fileName);
        }

        user.setFullName(profileDto.getFullName());
        user.setPosition(profileDto.getPosition());
        user.setPhoneNumber(profileDto.getPhoneNumber());
        user.setAddress(profileDto.getAddress());
        user.setProfileComplete(true);

        return userRepository.save(user);
    }
}