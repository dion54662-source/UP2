package models;

public class User {
    private int id;
    private String username;
    private String role;

    public User() {}

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isPicker() {
        return "PICKER".equals(role);
    }

    public boolean isStorekeeper() {
        return "STOREKEEPER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean canMoveProducts() {
        return isStorekeeper() || isAdmin();
    }
}