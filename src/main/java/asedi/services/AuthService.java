package asedi.services;

import asedi.models.User;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static AuthService instance;
    private final Map<String, User> users = new HashMap<>();
    private User currentUser;

    private AuthService() {
        // Initialize with some test users
        initializeTestUsers();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private void initializeTestUsers() {
        // Admin user
        users.put("admin@foodplaza.com", new User("admin@foodplaza.com", "admin123", "Administrador Principal", User.UserRole.ADMIN));
        
        // Manager user
        users.put("gerente@foodplaza.com", new User("gerente@foodplaza.com", "gerente123", "Gerente de Plaza", User.UserRole.MANAGER));
        
        // Regular user
        users.put("usuario@foodplaza.com", new User("usuario@foodplaza.com", "usuario123", "Usuario Regular", User.UserRole.USER));
    }

    public boolean login(String email, String password) {
        User user = users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public boolean isManager() {
        return currentUser != null && currentUser.getRole() == User.UserRole.MANAGER;
    }

    public boolean isUser() {
        return currentUser != null && currentUser.getRole() == User.UserRole.USER;
    }
}
