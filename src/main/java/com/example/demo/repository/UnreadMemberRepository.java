package com.example.demo.repository;


import com.example.demo.entity.Message;
import com.example.demo.entity.UnreadMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnreadMemberRepository extends JpaRepository<UnreadMember,Long> {


}
