package Audit.Auditing.service;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import Audit.Auditing.model.Role;
import Audit.Auditing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Enkripsi password

        // Pastikan role disimpan dengan prefix "ROLE_" jika belum
        String roleStr = userDto.getRole().toUpperCase();
        // if (!roleStr.startsWith("ROLE_")) {
        //     roleStr = "ROLE_" + roleStr;
        // }
        user.setRole(Role.valueOf(roleStr));
        user.setEnabled(true); // Default user aktif
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
}