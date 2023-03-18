package com.github.avthart.smtp.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.store.PListStore;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.store.kahadb.plist.PListStoreImpl;
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
import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
    public PersistenceAdapter persistenceAdapter() throws IOException {
        PersistenceAdapter persistenceAdapter = new KahaDBPersistenceAdapter();
        persistenceAdapter.setDirectory(new File("c:\\tmp\\KahaDB"));
        persistenceAdapter.createQueueMessageStore(new ActiveMQQueue("smtp.queue"));
        return persistenceAdapter;
    }

    @Bean
    public PListStore tempDataStore() {
        PListStore pListStore = new PListStoreImpl();
        pListStore.setDirectory(new File("c:\\tmp\\KahaDBtmp"));
        return pListStore;
    }

    @Bean
    public BrokerService producerBroker(PersistenceAdapter persistenceAdapter, PListStore tempDataStore) throws IOException {
        BrokerService brokerService = new BrokerService();
        brokerService.setPersistenceAdapter(persistenceAdapter);
        brokerService.setTempDataStore(tempDataStore);

        return brokerService;
    }

    @Bean
    public ProtocolHandler loggingMessageHook(JmsTemplate jmsTemplate) {
        return new MessageHook() {
            @SneakyThrows
            @Override
            public HookResult onMessage(SMTPSession smtpSession, MailEnvelope mailEnvelope) {
                byte[] mailEnvelopeBytes = mailEnvelope.getMessageInputStream().readAllBytes();

                jmsTemplate.send("smtp.queue", session -> {
                    ObjectMessage objectMessage = session.createObjectMessage(mailEnvelopeBytes);
                    objectMessage.setStringProperty("sender", mailEnvelope.getMaybeSender().asPrettyString());
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
