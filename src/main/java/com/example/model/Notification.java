package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob // Dùng cho kiểu TEXT trong MySQL
    @Column(name = "message")
    private String message;

    @Column(name = "type", length = 50) // Ví dụ: ORDER, STOCK, USER
    private String type;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "is_read")
    private Boolean isRead = false; // Mặc định là chưa đọc (0)

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(String title, String message, String type, String url) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.url = url;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}