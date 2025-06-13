package Audit.Auditing;

// import Audit.Auditing.model.User;
// import Audit.Auditing.model.Role;
// import Audit.Auditing.repository.UserRepository;
// import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // 1. IMPORT INI

// import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.password.PasswordEncoder;

@EnableJpaAuditing // 2. ANNOTASI INI UNTUK MENGAKTIFKAN AUDITING
@SpringBootApplication
public class AuditingSpiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditingSpiApplication.class, args);
    }

    // @Bean
    // CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    //     return args -> {
    //         // Pastikan username dan email unik
    //         if (userRepository.findByUsername("admin").isEmpty() && userRepository.findByEmail("admin@example.com").isEmpty()) {
    //             User adminUser = new User();
    //             adminUser.setUsername("admin");
    //             adminUser.setEmail("admin@example.com");
    //             adminUser.setPassword(passwordEncoder.encode("admin123"));
    //             adminUser.setRole(Role.admin); // Role dengan prefix
    //             adminUser.setEnabled(true);
    //             userRepository.save(adminUser);
    //             System.out.println("Admin user created: admin / admin123 (ADMIN)");
    //         }

    //         if (userRepository.findByUsername("kepalaspi").isEmpty() && userRepository.findByEmail("kepalaspi@example.com").isEmpty()) {
    //             User kepalaSpiUser = new User();
    //             kepalaSpiUser.setUsername("kepalaspi");
    //             kepalaSpiUser.setEmail("kepalaspi@example.com");
    //             kepalaSpiUser.setPassword(passwordEncoder.encode("kepala123"));
    //             kepalaSpiUser.setRole(Role.kepalaspi);
    //             kepalaSpiUser.setEnabled(true);
    //             userRepository.save(kepalaSpiUser);
    //             System.out.println("Kepala SPI user created: kepalaspi / kepala123 (KEPALASPI)");
    //         }

    //         if (userRepository.findByUsername("sekretaris").isEmpty() && userRepository.findByEmail("sekretaris@example.com").isEmpty()) {
    //             User sekretarisUser = new User();
    //             sekretarisUser.setUsername("sekretaris");
    //             sekretarisUser.setEmail("sekretaris@example.com");
    //             sekretarisUser.setPassword(passwordEncoder.encode("sekre123"));
    //             sekretarisUser.setRole(Role.sekretaris); // Role dengan prefix
    //             sekretarisUser.setEnabled(true);
    //             userRepository.save(sekretarisUser);
    //             System.out.println("Sekretaris user created: sekretaris / sekre123 (SEKRETARIS)");
    //         }

    //         if (userRepository.findByUsername("karyawan").isEmpty() && userRepository.findByEmail("karyawan@example.com").isEmpty()) {
    //             User karyawanUser = new User();
    //             karyawanUser.setUsername("pegawai");
    //             karyawanUser.setEmail("karyawan@example.com");
    //             karyawanUser.setPassword(passwordEncoder.encode("karyawan123"));
    //             karyawanUser.setRole(Role.pegawai); // Role dengan prefix
    //             karyawanUser.setEnabled(true);
    //             userRepository.save(karyawanUser);
    //             System.out.println("Karyawan user created: pegawai / karyawan123 (PEGAWAI)");
    //         }
    //     };
    // }
}