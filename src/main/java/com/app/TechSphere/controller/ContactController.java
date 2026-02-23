package com.app.TechSphere.controller;

import com.app.TechSphere.config.NotificationService;
import com.app.TechSphere.model.ContactMessage;
import com.app.TechSphere.service.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final ContactService contactService;
    private final NotificationService notificationService;

    public ContactController(ContactService contactService, NotificationService notificationService) {
        this.contactService = contactService;
        this.notificationService = notificationService;
    }

    @PostMapping("/contact/submit")
    public String submitContact(@ModelAttribute ContactMessage message,
                                RedirectAttributes redirectAttributes) {

        contactService.saveMessage(message);
        String subject = "New Contact Message from " + message.getFirstName();
        String body = "Name: " + message.getFirstName() + "\n" +
                      "Email: " + message.getEmail() + "\n" +
                      "Message: " + message.getMessage();
        notificationService.notifyAdminEmail(subject, body);

        redirectAttributes.addFlashAttribute("success",
                "Your message has been sent successfully!");

        return "redirect:/user/contact";
    }
}
