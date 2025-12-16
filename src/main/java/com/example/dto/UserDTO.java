package com.example.dto;
import com.example.model.User;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private String role;

    public UserDTO() {
    }

    public UserDTO(Long id, String name, String email, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public UserDTO(Long id, String name, String email, String phone, String address, String gender, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.role = role;
    }

    // 3. Các hàm Getter và Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {return gender;}

    public void setGender(String gender) {this.gender = gender;}

    public String getRole() {return role;}

    public void setRole(String role) {this.role = role;}

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        return user;
    }
}