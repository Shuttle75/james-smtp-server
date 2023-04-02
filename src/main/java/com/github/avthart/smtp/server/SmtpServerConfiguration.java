package com.github.avthart.smtp.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.james.core.MailAddress;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.MessageHook;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ObjectMessage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.avthart.smtp.server.MailGatewayConsumer.ACTIVEMQ_MAIL_IN;

@Configuration
@Slf4j
@EnableConfigurationProperties(SmtpServerProperties.class)
@EnableJms
public class SmtpServerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SmtpServer smtpServer(SmtpServerProperties properties, Collection<ProtocolHandler> handlers) {
        return new SmtpServer(properties, handlers);
    }

    @Bean
    public ProtocolHandler loggingMessageHook(JmsTemplate jmsTemplate) {
        return new MessageHook() {
            @SneakyThrows
            @Override
            public HookResult onMessage(SMTPSession smtpSession, MailEnvelope mailEnvelope) {
                byte[] mailEnvelopeBytes = mailEnvelope.getMessageInputStream().readAllBytes();
                List<String> recipients = mailEnvelope.getRecipients().stream()
                        .map(MailAddress::asPrettyString)
                        .collect(Collectors.toList());

                jmsTemplate.send(ACTIVEMQ_MAIL_IN, session -> {
                    ObjectMessage objectMessage = session.createObjectMessage(mailEnvelopeBytes);
                    objectMessage.setStringProperty("sender", mailEnvelope.getMaybeSender().asPrettyString());
                    objectMessage.setStringProperty("recipients", String.join(",", recipients));
                    return objectMessage;
                });

                return HookResult.OK;
            }

            @Override
            public void destroy() {
            }
        };
    }
}
