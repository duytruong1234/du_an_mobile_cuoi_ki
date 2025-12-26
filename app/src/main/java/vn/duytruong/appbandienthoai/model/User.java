package vn.duytruong.appbandienthoai.model;

public class User {
    int id;
    String email;
    String pass;
    String username;
    String mobile;
    int role; // 0 = user thường, 1 = admin
    String loginType; // "normal" = đăng nhập thường, "google" = đăng nhập Google
    int status; // 1 = Active (hoạt động), 0 = Locked (bị khóa)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == 1;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public boolean isGoogleAccount() {
        // Kiểm tra cả loginType và mobile để đảm bảo tương thích ngược
        return "google".equals(loginType) || "0000000000".equals(mobile);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isActive() {
        return status == 1;
    }

    public boolean isLocked() {
        return status == 0;
    }
}
