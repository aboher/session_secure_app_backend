package com.aboher.sessionsecureapp.dto;

import com.aboher.sessionsecureapp.model.SessionDetails;
import lombok.Builder;

import java.util.Date;

@Builder
public record SessionInfo(
        String id,
        Date creationDate,
        Date lastAccessedDate,
        Date expirationDate,
        SessionDetails sessionDetails
) {
}
