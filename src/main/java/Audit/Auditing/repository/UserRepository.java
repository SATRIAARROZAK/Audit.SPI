package Audit.Auditing.repository;

import Audit.Auditing.model.Role; // Tambahkan import ini
import Audit.Auditing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Tambahkan import ini
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // TAMBAHKAN METODE INI
    List<User> findByRole(Role role); 
}