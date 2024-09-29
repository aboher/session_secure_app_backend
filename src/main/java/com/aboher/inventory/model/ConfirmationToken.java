package com.aboher.inventory.model;

import com.aboher.inventory.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "confirmation_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationToken {
    private static final int EXPIRATION_TIME_PERIOD_IN_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 36, nullable = false)
    private String token;

    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public ConfirmationToken(User user, TokenType tokenType) {
        this.user = user;
        this.expiryDate = calculateExpiryDate();
        this.token = UUID.randomUUID().toString();
        this.tokenType = tokenType;
    }

    private Date calculateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, EXPIRATION_TIME_PERIOD_IN_HOURS);
        return calendar.getTime();
    }

    public boolean hasTokenExpired() {
        return new Date().after(this.expiryDate);
    }

}
