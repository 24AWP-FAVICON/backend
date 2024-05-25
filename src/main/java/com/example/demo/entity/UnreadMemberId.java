package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnreadMemberId implements Serializable {
    // UnreadMember 테이블 복합 키 클래스
    private String msgId;
    private String userId;
}
