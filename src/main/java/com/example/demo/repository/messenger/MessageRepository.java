package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message,Long> {


}
