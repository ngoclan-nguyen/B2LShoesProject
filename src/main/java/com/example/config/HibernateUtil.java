package com.example.config;

import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.example.model.*;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private HibernateUtil() {} // Tránh khởi tạo ngoài ý muốn

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                Properties settings = new Properties();
                
                settings.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
                settings.put(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:mysql://localhost:3306/b2lshoes?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
                settings.put(AvailableSettings.JAKARTA_JDBC_USER, "webshoes");
                settings.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, "123456");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");

                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.FORMAT_SQL, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "update");

                configuration.setProperties(settings);
                
                configuration.addAnnotatedClass(Brand.class);
                configuration.addAnnotatedClass(Category.class);
                configuration.addAnnotatedClass(Color.class);
                configuration.addAnnotatedClass(Sport.class);
                configuration.addAnnotatedClass(MasterSize.class);
                configuration.addAnnotatedClass(ProductGroup.class);
                configuration.addAnnotatedClass(Product.class);
                configuration.addAnnotatedClass(ProductVariant.class);
                configuration.addAnnotatedClass(ProductImage.class);
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(RememberMeToken.class);
                configuration.addAnnotatedClass(UserWishlist.class);
                configuration.addAnnotatedClass(ProductView.class);
                configuration.addAnnotatedClass(CartItem.class);
                configuration.addAnnotatedClass(OrderWeb.class);
                configuration.addAnnotatedClass(OrderWebDetail.class);
                configuration.addAnnotatedClass(PaymentLog.class);
                configuration.addAnnotatedClass(DeliveryLog.class);
                configuration.addAnnotatedClass(ProductReview.class);
                configuration.addAnnotatedClass(ReviewImage.class);
                configuration.addAnnotatedClass(Voucher.class);
                configuration.addAnnotatedClass(Notification.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
    
    public static Session getSession() {
        return getSessionFactory().getCurrentSession();
    }
}