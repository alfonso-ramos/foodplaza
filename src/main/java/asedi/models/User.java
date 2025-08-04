package asedi.models;

/**
 * Represents a user of the application.
 */
public class User {
    private String email;
    private String password;
    private String fullName;
    private UserRole role;

    /**
     * Constructs a new User.
     *
     * @param email    the user's email
     * @param password the user's password
     * @param fullName the user's full name
     * @param role     the user's role
     */
    public User(String email, String password, String fullName, UserRole role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    /**
     * Represents the role of a user.
     */
    public enum UserRole {
        ADMIN("Administrador"),
        MANAGER("Gerente"),
        USER("Usuario");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
