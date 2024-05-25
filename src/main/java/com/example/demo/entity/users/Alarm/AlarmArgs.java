package com.example.demo.entity.users.Alarm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlarmArgs {
    //알람을 발생시킨 사람
    private String fromUserId;

    //알람이 발생한 주체에 대한 아이디
    private Long targetId;

}
