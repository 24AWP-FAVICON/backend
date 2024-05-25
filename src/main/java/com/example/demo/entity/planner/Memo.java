package com.example.demo.entity.planner;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long memoId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
