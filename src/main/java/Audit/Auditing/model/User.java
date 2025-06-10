package Audit.Auditing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// import Audit.Auditing.model.Role;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Jika menggunakan Enum
    @Column(nullable = false)
    private Role role;

    private boolean enabled = true; // Tambahkan field enabled, default true

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "photo_path")
    private String photoPath; // Path ke file foto

    @Column(name = "signature_path")
    private String signaturePath; // Path ke file tanda tangan

    private String position; // Jabatan

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Penanda apakah profil sudah diisi
    @Column(name = "profile_complete", nullable = false)
    private boolean profileComplete = false;
}