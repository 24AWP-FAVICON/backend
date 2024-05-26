package com.example.demo.repository;

import com.example.demo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {

}
