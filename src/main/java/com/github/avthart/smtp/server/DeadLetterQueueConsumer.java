package com.github.avthart.smtp.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.Objects;

import static com.github.avthart.smtp.server.MailOutConsumer.ACTIVEMQ_MAIL_OUT;

@Slf4j
@ManagedResource(objectName="org.apache.activemq:name=Support", description="Reload Dead Letter Queue")
@Service
public class DeadLetterQueueConsumer {
    protected static final String ACTIVEMQ_DLQ = "ActiveMQ.DLQ";
    private final JmsTemplate jmsTemplate;

    public DeadLetterQueueConsumer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @ManagedOperation(description = "Reload Dead Letter Queue")
    public String reloadDeadLetterQueue(Integer amount) {

        for (int i = 0; i < amount; i++) {
            Message message = jmsTemplate.receive(ACTIVEMQ_DLQ);
            if (Objects.nonNull(message)) {
                if (message instanceof ObjectMessage) {
                    ObjectMessage inMessage = (ObjectMessage) message;
                    jmsTemplate.send(ACTIVEMQ_MAIL_OUT, session -> {
                        ObjectMessage outMessage = session.createObjectMessage(inMessage.getObject());
                        outMessage.setStringProperty("sender", inMessage.getStringProperty("sender"));
                        outMessage.setStringProperty("recipients", inMessage.getStringProperty("recipients"));
                        return outMessage;
                    });
                }
            }
            else {
                break;
            }
        }
        return "1000 moded to Mail Out";
    }
}
