package com.example.service;

import com.example.dao.NotificationDao;
import com.example.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    public Integer getUnreadCount() {
        return notificationDao.getUnreadCount();
    }

    public List<Notification> getRecentNotifications(int limit) {
        return notificationDao.getRecentNotifications(limit);
    }

    public boolean markAsRead(Long id) {
        return notificationDao.markAsRead(id);
    }

    public void createNewNotification(String title, String message, String type, String url) {
        Notification notification = new Notification(title, message, type, url);
        notificationDao.save(notification);
    }

    public List<Notification> findAll() {
        return notificationDao.findAll();
    }
}