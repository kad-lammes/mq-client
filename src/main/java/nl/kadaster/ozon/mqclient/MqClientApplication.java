package nl.kadaster.ozon.mqclient;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.RMQSession;
import nl.kadaster.ozon.queues.domain.constants.ProcesType;
import nl.kadaster.ozon.queues.domain.request.QueueRequest;
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
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Arrays;
import java.util.Map;

@SpringBootApplication
public class MqClientApplication {

    @Autowired
    JmsTemplate jmsTemplate;

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        var connectionFactory = new RMQConnectionFactory();
        connectionFactory.setUris(Arrays.asList("amqp://localhost:5672"));
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory<? extends MessageListenerContainer> myFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
        var factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        var jmsTemplate = new JmsTemplate();
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        jmsTemplate.setDestinationResolver(destinationResolver());
        return jmsTemplate;
    }

    public MessageConverter jacksonJmsMessageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.BYTES);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public DynamicDestinationResolver destinationResolver() {
        return new DynamicDestinationResolver() {
            public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain) throws JMSException {
                if (session instanceof RMQSession) {
                    ((RMQSession) session).setQueueDeclareArguments(Map.of("x-max-priority", 10));
                }

                return super.resolveDestinationName(session, destinationName, pubSubDomain);
            }
        };
    }

    public void sendMessage() {
        var queueRequest = new QueueRequest("22444db1-ca5f-4748-b068-6e17f324120b", null, ProcesType.ONTWERPPROEFREGISTRATIE, false);
        jmsTemplate.convertAndSend("RODAQ_OWN.ROD_VALIDATOR_REQ", queueRequest);
    }

    public static void main(String[] args) {
        var run = SpringApplication.run(MqClientApplication.class, args);
        run.getBean(MqClientApplication.class).sendMessage();
    }

}
