package com.example.demo.controller;

import com.example.demo.dto.planner.TripCreationDTO;
import com.example.demo.dto.planner.TripDateDetailsDTO;
import com.example.demo.dto.planner.TripPatchDTO;
import com.example.demo.dto.planner.UserIdsDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.KafkaProducerService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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