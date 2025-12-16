package com.example.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import com.example.config.HibernateUtil;
import com.example.model.CartItem;
import com.example.model.OrderWeb;
import com.example.model.OrderWebDetail;
import com.example.model.ProductVariant;
import com.example.model.User;

@Repository
public class OrderDao {

    // Add this method to find all orders by customer ID
    public List<OrderWeb> findByCustomer(Long customerId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();

            String hql = 
                "select distinct o from OrderWeb o " +
                "left join fetch o.orderDetails d " +
                "left join fetch d.productVariant pv " +
                "left join fetch pv.product p " +
                "where o.customer.id = :cid " +
                "order by o.createdAt desc";

            List<OrderWeb> orders = session.createQuery(hql, OrderWeb.class)
                    .setParameter("cid", customerId)
                    .getResultList();

            tx.commit();
            return orders;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public OrderWeb findByIdAndCustomer(Long orderId, Long customerId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();

            String hql =
                "select distinct o from OrderWeb o " +
                "left join fetch o.orderDetails d " +
                "left join fetch d.productVariant pv " +
                "left join fetch pv.product p " +
                "left join fetch pv.size s " +
                "where o.id = :oid and o.customer.id = :cid";

            OrderWeb order = session.createQuery(hql, OrderWeb.class)
                    .setParameter("oid", orderId)
                    .setParameter("cid", customerId)
                    .uniqueResult();

            tx.commit();
            return order;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public Long createOrderFromCart(
            Long customerId,
            String consignee,
            String phone,
            String address,
            String paymentMethod,
            Long deliveryFee
    ) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();

            User customer = session.get(User.class, customerId);

            // Lấy cart items (join fetch để tính tiền)
            String cartHql =
                "select c from CartItem c " +
                "join fetch c.productVariant pv " +
                "join fetch pv.product p " +
                "join fetch pv.size s " +
                "where c.user.id = :cid";

            List<CartItem> cartItems = session.createQuery(cartHql, CartItem.class)
                    .setParameter("cid", customerId)
                    .getResultList();

            if (cartItems == null || cartItems.isEmpty()) {
                throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn.");
            }

            long subTotal = 0;
            for (CartItem c : cartItems) {
                long price = c.getProductVariant().getProduct().getPrice();
                subTotal += price * c.getQuantity();
            }

            long ship = (deliveryFee == null) ? 0 : deliveryFee;
            long total = subTotal + ship;

            // Tạo order_web
            OrderWeb order = new OrderWeb();
            order.setCustomer(customer);
            order.setConsignee(consignee);
            order.setPhoneNumber(phone);
            order.setDeliveryAddress(address);
            order.setDeliveryFee(ship);
            order.setTotalAmount(total);
            order.setPaymentMethod(paymentMethod);

            // ✅ set status để khớp trigger integrity
            // - COD không được chứa chữ "ATM" trong payment_status
            order.setPaymentStatus(paymentMethod.equals("Tiền mặt (COD)") ? "Chưa thanh toán" : "Chờ thanh toán");
            order.setDeliveryStatus("Chờ xử lý");

            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            session.persist(order);
            session.flush(); // lấy order.id

            // Tạo order_web_detail (trigger sẽ tự trừ kho)
            for (CartItem c : cartItems) {
                ProductVariant pv = c.getProductVariant();
                long price = pv.getProduct().getPrice();
                int qty = c.getQuantity();

                OrderWebDetail d = new OrderWebDetail();
                d.setOrderWeb(order);
                d.setProductVariant(pv);
                d.setPrice(price);
                d.setQuantity(qty);
                d.setTotalAmount(price * qty); // ✅ trigger check total_amount
                d.setCreatedAt(LocalDateTime.now());
                d.setUpdatedAt(LocalDateTime.now());

                session.persist(d);
            }

            // Clear cart
            session.createQuery("delete from CartItem c where c.user.id = :cid", CartItem.class)
                    .setParameter("cid", customerId)
                    .executeUpdate();

            tx.commit();
            return order.getId();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            // nếu bị trigger báo hết hàng -> exception sẽ ném ở đây
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    // Add this method to cancel an order
    public void cancelOrder(Long orderId, Long customerId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();

            String hql = 
                "update OrderWeb o set o.deliveryStatus = 'Đã hủy', o.updatedAt = :now " +
                "where o.id = :oid and o.customer.id = :cid and o.deliveryStatus = 'Chờ xử lý'";

            int updated = session.createQuery(hql, int.class)
                    .setParameter("now", LocalDateTime.now())
                    .setParameter("oid", orderId)
                    .setParameter("cid", customerId)
                    .executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Không thể hủy đơn hàng này.");
            }

            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
}
