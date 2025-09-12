package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repostiory.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public List<Post> getList() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).get();
    }

    public Post findByTitle(String title) {
        return postRepository.findByTitle(title).get();
    }

    public Post create(String title, String content) {
        Post post = new Post(title, content);

        return postRepository.save(post);
    }

    public void update(Post post, String title, String content) {
        post.modify(title, content);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public Optional<Post> findLastest() {
        return postRepository.findFirstByOrderByIdDesc();
    }
}
