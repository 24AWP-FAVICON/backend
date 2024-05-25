package com.example.demo.repository.users.user;

import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByDeleteAt(LocalDate deleteAt);
    List<User> findAllByUserIdIn(List<String> googleIds);
}
