package nl.kadaster.ozon.mqclient;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import nl.kadaster.ozon.queues.domain.QueueRequest;
import nl.kadaster.ozon.queues.domain.audit.AuditRequest;
import nl.kadaster.ozon.queues.domain.constants.Bewaartermijn;
import nl.kadaster.ozon.queues.domain.constants.ProcesType;
import nl.kadaster.ozon.queues.domain.download.DownloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@SpringBootApplication
public class MqClientApplication {

    @Autowired
    JmsTemplate jmsTemplate;

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
        connectionFactory.setUris(Arrays.asList("amqp://localhost:5672"));
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory<? extends MessageListenerContainer> myFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setMessageConverter(this.jacksonJmsMessageConverter());
        return jmsTemplate;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.BYTES);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public void sendMessage() throws MalformedURLException {
       var downloadRequest= new DownloadRequest("8119a196-22d9-4527-9f91-6834c52db747", null, ProcesType.GEOVALIDATIE, new URL("http://localhost:1234/zipservice"), Bewaartermijn.GEEN, "782934793247");
//        jmsTemplate.convertAndSend("AUDIT_LOG_REQ", new AuditRequest("123", "download", ProcesType.REGISTRATIE, "sta[", true));
        jmsTemplate.convertAndSend("RODAQ_OWN.ROD_VAL_GEOMETRY_REQ", new QueueRequest("8119a196-22d9-4527-9f91-6834c52db747", null, ProcesType.GEOVALIDATIE));
//        jmsTemplate.convertAndSend("RODAQ_OWN.ROD_DOWNLOAD_REQ", downloadRequest);
//        jmsTemplate.convertAndSend("RODAQ_OWN.ROD_OW_SCHEMA_REQ", new QueueRequest("bbffe28e-8db8-49bb-801f-b1ba1d8abb56", "2021.07", ProcesType.REGISTRATIE));
    }

    public static void main(String[] args) throws MalformedURLException {
        var run = SpringApplication.run(MqClientApplication.class, args);
        run.getBean(MqClientApplication.class).sendMessage();
    }

}
