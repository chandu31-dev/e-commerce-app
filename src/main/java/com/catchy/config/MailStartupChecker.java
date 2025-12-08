package com.catchy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "features", name = "addresses-only", havingValue = "false", matchIfMissing = false)
public class MailStartupChecker implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(MailStartupChecker.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(@org.springframework.lang.NonNull ApplicationReadyEvent event) {
        if (mailSender == null) {
            logger.warn("MailStartupChecker: JavaMailSender bean not configured; application will write mail outputs to target/ files.");
            return;
        }

        try {
            // `testConnection` is available on JavaMailSenderImpl; attempt to validate if possible
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                try {
                    impl.testConnection();
                    logger.info("MailStartupChecker: JavaMailSender configured and test connection succeeded.");
                } catch (Exception e) {
                    logger.warn("MailStartupChecker: JavaMailSender configured but test connection failed: {}", e.getMessage());
                }
            } else {
                logger.info("MailStartupChecker: JavaMailSender is present but not a JavaMailSenderImpl; skipping connection test.");
            }
        } catch (Exception e) {
            logger.error("MailStartupChecker: Error while testing mail connection: {}", e.getMessage());
        }
    }
}
