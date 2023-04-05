package com.github.avthart.smtp.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

import static org.apache.activemq.store.kahadb.disk.journal.Journal.JournalDiskSyncStrategy.PERIODIC;

@Slf4j
@Configuration
public class ActiveMQConfiguration {

    @Bean
    public PersistenceAdapter persistenceAdapter() {
        KahaDBStore kahaDBStore = new KahaDBStore();
        kahaDBStore.setDirectory(new File("activemq-data/localhost/KahaDB"));
        kahaDBStore.setMaxAsyncJobs(1000000);
        kahaDBStore.setJournalDiskSyncStrategy(PERIODIC.name());
        return kahaDBStore;
    }

    @Bean
    public BrokerService producerBroker(PersistenceAdapter persistenceAdapter) throws IOException {
        BrokerService brokerService = new BrokerService();
        brokerService.setPersistenceAdapter(persistenceAdapter);
        return brokerService;
    }

}
