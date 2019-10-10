package kz.adilka.dbservice;


import kz.adilka.dbservice.models.Answer;
import kz.adilka.dbservice.models.User;
import kz.adilka.dbservice.repositories.QuestionRepository;
import kz.adilka.dbservice.repositories.UserRepository;
import kz.adilka.dbservice.util.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import kz.adilka.dbservice.config.ApplicationConfigReader;


@Service
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    ApplicationConfigReader applicationConfigReader;

    @Autowired
    UserRepository userRepository;

    @RabbitListener(queues = "${app1.queue.name}")
    public void receiveMessageForApp1(final User data) {
        log.info("Received message: {} from app1 queue.", data);

        try {
            log.info("Making REST call to the API");
            userRepository.save(data);
            log.info("<< Exiting receiveMessageForApp1() after API call.");
        } catch(HttpClientErrorException  ex) {

            if(ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Delay...");
                try {
                    Thread.sleep(ApplicationConstants.MESSAGE_RETRY_DELAY);
                } catch (InterruptedException e) { }

                log.info("Throwing exception so that message will be requed in the queue.");
                // Note: Typically Application specific exception should be thrown below
                throw new RuntimeException();
            } else {
                throw new AmqpRejectAndDontRequeueException(ex);
            }

        } catch(Exception e) {
            log.error("Internal server error occurred in API call. Bypassing message requeue {}", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
