
package com.app.TechSphere.config;

import com.app.TechSphere.model.Order;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;

    public NotificationService(SimpMessagingTemplate messagingTemplate, JavaMailSender mailSender) {
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender;
    }

    public void notifyAdmin(String message) {
        messagingTemplate.convertAndSend("/topic/adminNotifications", message);
    }

    public void notifyUser(String email, String message) {
        messagingTemplate.convertAndSend("/topic/user/" + email, message);
    }
    public void notifyAdminEmail(String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("shopandsell86@gmail.com"); // your admin email
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
    public void notifyUserEmail(String userEmail, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(userEmail);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
    public void notifyNewOrder(Order order) {
        String message = "New order received!\n\n" +
                         "Order ID: " + order.getId() + "\n" +
                         "User: " + order.getUser().getEmail() + "\n" +
                         "Total: $" + order.getTotalAmount() + "\n" +
                         "Items: " + order.getItemsSummary();

        // WebSocket notification
        notifyAdmin("New order #" + order.getId() + " placed by " + order.getUser().getEmail());

        // Email notification to admin
        notifyAdminEmail("New Order #" + order.getId(), message);

        // Email notification to user
        notifyUserEmail(order.getUser().getEmail(), "Order Confirmation #" + order.getId(),
                        "Thank you for your order! \n\n" + message);
    }
}

