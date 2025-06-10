package Audit.Auditing.service;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.Role;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ... (kode yang ada tidak berubah)
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().getAuthority())
                .disabled(!user.isEnabled())
                .build();
    }

    @Override
    public User saveUser(UserDto userDto) {
        // ... (kode yang ada tidak berubah)
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Enkripsi password

        String roleStr = userDto.getRole().toUpperCase();
        user.setRole(Role.valueOf(roleStr));
        user.setEnabled(true); // Default user aktif
        return userRepository.save(user);
    }
    
    // ... (find methods tidak berubah)

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User tidak ditemukan dengan id: " + id));

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(Role.valueOf(userDto.getRole().toUpperCase()));

        // Logika untuk update password opsional
        // Hanya update password jika field password di form diisi (tidak null dan tidak kosong)
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        // Jika password kosong, field password user yang ada di database tidak akan diubah

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        // ... (kode yang ada tidak berubah)
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
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