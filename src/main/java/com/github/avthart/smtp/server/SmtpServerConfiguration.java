package com.github.avthart.smtp.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.MessageHook;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

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
    public PersistenceAdapter persistenceAdapter() {
        PersistenceAdapter persistenceAdapter = new KahaDBPersistenceAdapter();
        persistenceAdapter.setDirectory(new File("c:\\tmp\\KahaDB"));
        return persistenceAdapter;
    }

    @Bean
    public BrokerService producerBroker(PersistenceAdapter persistenceAdapter) throws IOException {
        BrokerService brokerService = new BrokerService();
        brokerService.setPersistenceAdapter(persistenceAdapter);
        return brokerService;
    }

    @Bean
    public ProtocolHandler loggingMessageHook(JmsTemplate jmsTemplate) {
        return new MessageHook() {
            @SneakyThrows
            @Override
            public HookResult onMessage(SMTPSession smtpSession, MailEnvelope mailEnvelope) {
                byte[] mailEnvelopeBytes = mailEnvelope.getMessageInputStream().readAllBytes();

                jmsTemplate.setDefaultDestination(new ActiveMQQueue("smtp.queue"));
                jmsTemplate.send(session -> {
                    ObjectMessage objectMessage = session.createObjectMessage(mailEnvelopeBytes);
                    objectMessage.setStringProperty("sender", mailEnvelope.getMaybeSender().asPrettyString());
                    return objectMessage;
                });

//                FileUtils.copyInputStreamToFile(
//                        mailEnvelope.getMessageInputStream(),
//                        new File("/dev/null"));

                return HookResult.OK;
            }

            @Override
            public void destroy() {
            }
        };
    }
}
