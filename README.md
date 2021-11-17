#### Start een rabbitmq docker container
docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management

Rabbit MQ admin is te benaderen via http://localhost:15672 

- username: guest
- password: guest

#### Pas de volgende properties in de application.yml van het project aan:
- application.queue.rabbitmq.uris= "amqp://localhost:5672"
- spring.jms.listener.auto-startup: true
