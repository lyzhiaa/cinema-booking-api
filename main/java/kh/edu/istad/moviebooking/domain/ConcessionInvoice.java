package kh.edu.istad.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "concession_invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcessionInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concession_order_id", nullable = false, unique = true)
    private ConcessionOrder concessionOrder;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false, unique = true)
    private String qrToken;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    // null = not picked up
    private LocalDateTime pickedUpAt;

    @PrePersist
    void prePersist() {

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }
}