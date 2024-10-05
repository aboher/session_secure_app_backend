package com.aboher.sessionsecureapp.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class SessionDetails implements Serializable {

    private String remoteAddress;
    private String userAgent;

    @Serial
    private static final long serialVersionUID = 8850489178248613501L;
}
