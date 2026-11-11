package binhjeremy.bankappspringcore.models;

import binhjeremy.bankappspringcore.models.DTO.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionType type;
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Transaction(BigDecimal amount, TransactionType type, LocalDateTime time,Account account) {
        this.amount = amount;
        this.type = type;
        this.dateTime = time;
        this.account = account;
    }

}
