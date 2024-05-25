package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.UnreadMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnreadMemberRepository extends JpaRepository<UnreadMember,Long> {


}
