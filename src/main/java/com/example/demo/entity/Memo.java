package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@ToString
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long memoId;

    private Long tripId;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
