package com.example.demo.controller.messenger;

import com.example.demo.dto.planner.UserIdsDTO;
import com.example.demo.repository.messenger.MessageRepository;
import com.example.demo.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping
    public void sendMessage(@RequestBody MessageRequest request) {

    }
}