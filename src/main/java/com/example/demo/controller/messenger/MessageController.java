package com.example.demo.controller.messenger;

import com.example.demo.repository.messenger.MessageRepository;
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
    private MessageRepository messageRepository;


}