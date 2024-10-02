package com.aboher.sessionsecureapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    @Column(name = "first_name", length = 45)
    private String firstName;

    @Column(name = "last_name", length = 45)
    private String lastName;

    public UserInfo(UserInfo userInfo) {
        this.firstName = userInfo.getFirstName();
        this.lastName = userInfo.getLastName();
    }
}
