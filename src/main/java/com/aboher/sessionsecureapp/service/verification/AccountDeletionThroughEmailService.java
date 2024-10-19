package com.aboher.sessionsecureapp.service.verification;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.service.MessageSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class AccountDeletionThroughEmailService extends AbstractEmailBasedVerificationService {
    private final FrontendProperties frontendProperties;

    public AccountDeletionThroughEmailService(FrontendProperties frontendProperties,
                                              ConfirmationTokenService confirmationTokenService,
                                              MessageSender<SimpleMailMessage> emailMessageSender) {
        super(confirmationTokenService, emailMessageSender);
        this.frontendProperties = frontendProperties;
    }

    @Override
    protected int getExpirationTimePeriodInMinutes() {
        return 10;
    }

    @Override
    protected TokenType getTokenType() {
        return TokenType.ACCOUNT_DELETION_TOKEN;
    }

    @Override
    protected String getVerificationEmailSubject() {
        return "Account Deletion request";
    }

    @Override
    protected String getVerificationEmailText(String token) {
        return String.format("""
                        You have requested to delete your account. Be aware that this process is irreversible,
                        if you really wish to delete your account, please click here to continue:
                        %s%s?token=%s

                        Please, don't share this link with anyone.""",
                frontendProperties.getUrl(),
                frontendProperties.getRequestAccountDeletionPath(),
                token);
    }
}
