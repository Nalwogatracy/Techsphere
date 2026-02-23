package com.app.TechSphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<CartItem> items  = new ArrayList<>(); // The items from the cart
    
    private double subtotal;

    private double shippingCost;
    
    private double depositAmount = 0;
    private Double paidAmount;

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }
        
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Transient
    private double paymentProgress;

    public double getPaymentProgress() {
        return paymentProgress;
    }

    public void setPaymentProgress(double paymentProgress) {
        this.paymentProgress = paymentProgress;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentProof() {
        return paymentProof;
    }

    public void setPaymentProof(String paymentProof) {
        this.paymentProof = paymentProof;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getDepositDueDate() {
        return depositDueDate;
    }

    public void setDepositDueDate(LocalDateTime depositDueDate) {
        this.depositDueDate = depositDueDate;
    }
    
    public enum PaymentStatus {
        PENDING,          // No payment yet
        DEPOSIT_PAID,     // Deposit received, waiting for full payment
        PARTIALLY_PAID,   // Partial payment made
        FULLY_PAID,       // Fully paid
        VERIFYING,        // Payment proof uploaded, awaiting admin verification
        VERIFIED,         // Payment verified by admin
        REJECTED,         // Payment proof rejected
        REFUNDED          // Payment refunded
    }
    
    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        MOBILE_MONEY,
        BANK_TRANSFER,
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        OTHER
    }
    
    private String paymentReference; // Transaction ID, Mobile Money ref, etc.
    
    private String paymentProof; // Path to uploaded receipt/screenshot
    
    private LocalDateTime paymentDate;
    
    private LocalDateTime depositDueDate;

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }


    @Column(nullable = false)
    private double totalAmount;

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }


    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum OrderStatus {
        PENDING, APPROVED, SHIPPED, DELIVERED, CANCELLED
    }

    // Helper method to summarize items
    public String getItemsSummary() {
        return items.size() + " items • " +
               items.stream().map(i -> i.getProduct().getName()).reduce((a,b) -> a + ", " + b).orElse("");
    }

    // getters & setters
}
