package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Post write(String title, String content) {
        Post post = new Post(title, content);

        return postRepository.save(post);
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public void modify(Post post, String title, String content) {
        post.modify(title, content);
    }

    public void writeComment(Post post, String content) {
        post.addComment(content);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    // 029-B 나아가기 : 페이징 (page 는 1부터 시작)
    public Page<Post> getPage(int page) {
        int pageNumber = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Order.desc("id")));

        return postRepository.findAll(pageable);
    }

    public static final int PAGE_SIZE = 10;
}
