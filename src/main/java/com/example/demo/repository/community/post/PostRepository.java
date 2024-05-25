package com.example.demo.repository.community.post;


import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {

    public List<Post> findByUser(User user);

}
