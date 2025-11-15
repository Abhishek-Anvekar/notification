package com.ganpati.notification_service.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String phoneNumber;
    //required for message delivery status
    private String statusCallbackUrl;

    @PostConstruct
    public void setup(){
        Twilio.init(accountSid,authToken);
    }
}
