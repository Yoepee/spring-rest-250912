package com.back.domain.post.post.repostiory;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findFirstByOrderByIdDesc();

    Optional<Post> findByTitle(String title);
}
