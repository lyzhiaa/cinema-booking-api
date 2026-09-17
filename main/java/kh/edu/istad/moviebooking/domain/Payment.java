package kh.edu.istad.moviebooking.domain;

import jakarta.persistence.*;
import kh.edu.istad.moviebooking.domain.enums.PaymentMethod;
import kh.edu.istad.moviebooking.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_reference")
    private String transactionReference;

//    for connect to the KHQR
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "provider_reference", unique = true)
    private String providerReference;

    @Column(
            name = "qr_payload",
            columnDefinition = "TEXT"
    )
    private String qrPayload;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (status == null) {
            status = PaymentStatus.PENDING;
        }

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
