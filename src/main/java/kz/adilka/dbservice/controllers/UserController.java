package kz.adilka.dbservice.controllers;

import kz.adilka.dbservice.MessageSender;
import kz.adilka.dbservice.config.ApplicationConfigReader;
import kz.adilka.dbservice.exceptions.ResourceNotFoundException;
import kz.adilka.dbservice.models.User;
import kz.adilka.dbservice.repositories.UserRepository;
import kz.adilka.dbservice.util.ApplicationConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    private final RabbitTemplate rabbitTemplate;

    private ApplicationConfigReader applicationConfig;

    private MessageSender messageSender;

    public ApplicationConfigReader getApplicationConfig() {
        return applicationConfig;
    }

    @Autowired
    public UserController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setApplicationConfig(ApplicationConfigReader applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    @Autowired
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }


    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        String exchange = getApplicationConfig().getApp1Exchange();
        String routingKey = getApplicationConfig().getApp1RoutingKey();

        /* Sending to Message Queue */
        try {

            messageSender.sendMessage(rabbitTemplate, exchange, routingKey, user);
            return new ResponseEntity<String>(ApplicationConstants.IN_QUEUE, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity(ApplicationConstants.MESSAGE_QUEUE_SEND_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        /*return userRepository.save(user);*/
    }

    @PutMapping("users/{userId}")
    public User updateUser(@PathVariable Long userId,
                                @Valid @RequestBody User userRequest) {
        return userRepository.findById(userId).map(user -> {
            user.setUserName(userRequest.getUserName());
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    @DeleteMapping("users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        return userRepository.findById(userId).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }
}
