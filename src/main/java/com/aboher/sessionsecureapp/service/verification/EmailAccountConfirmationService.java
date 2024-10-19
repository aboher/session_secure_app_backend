package com.aboher.sessionsecureapp.service.verification;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.service.MessageSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailAccountConfirmationService extends AbstractEmailBasedVerificationService {
    private final FrontendProperties frontendProperties;

    public EmailAccountConfirmationService(FrontendProperties frontendProperties,
                                           ConfirmationTokenService confirmationTokenService,
                                           MessageSender<SimpleMailMessage> emailMessageSender) {
        super(confirmationTokenService, emailMessageSender);
        this.frontendProperties = frontendProperties;
    }

    @Override
    protected int getExpirationTimePeriodInMinutes() {
        return 60 * 24;
    }

    @Override
    protected TokenType getTokenType() {
        return TokenType.EMAIL_CONFIRMATION_TOKEN;
    }

    @Override
    protected String getVerificationEmailSubject() {
        return "Complete Registration";
    }

    @Override
    protected String getVerificationEmailText(String token) {
        return String.format("""
                        To confirm your account, please click here:
                        %s%s?token=%s

                        Please, don't share this link with anyone.""",
                frontendProperties.getUrl(),
                frontendProperties.getUserEmailConfirmationHandlerPath(),
                token);
    }
}
