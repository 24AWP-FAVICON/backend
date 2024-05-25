package com.example.demo.repository.community.post;

import com.example.demo.entity.community.post.Attachment;
import com.example.demo.entity.community.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentFileRepository extends JpaRepository<Attachment,String> {

    public void deleteByFilePath(String path);

    public List<Attachment> findByPost(Post post);
}
