package Audit.Auditing.model;

public enum Role {
    admin("ADMIN"),
    kepalaspi("KEPALASPI"),
    sekretaris("SEKRETARIS"),
    pegawai("PEGAWAI");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}