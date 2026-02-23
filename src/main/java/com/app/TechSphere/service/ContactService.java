package com.app.TechSphere.service;

import com.app.TechSphere.model.ContactMessage;
import com.app.TechSphere.repository.ContactMessageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactMessageRepository repository;
    

    public ContactService(ContactMessageRepository repository) {
        this.repository = repository;
       
    }

    public void saveMessage(ContactMessage message) {
        repository.save(message);
    }
    public List<ContactMessage> getAllMessages() {
        return repository.findAll();
    }

    // ✅ Get a single message by id
    public ContactMessage getMessageById(Long id) {
        Optional<ContactMessage> message = repository.findById(id);
        return message.orElseThrow(() -> new RuntimeException("Message not found"));
    }
}
