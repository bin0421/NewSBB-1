package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
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

    // 029-A 나아가기 : 글 삭제
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

    public Post write(Member author, String title, String content) {
        Post post = new Post(author, title, content);

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

    public PostComment writeComment(Post post, Member author, String content) {
        PostComment postComment = post.addComment(author, content);
        postRepository.flush(); // 043-A : cascade 로 INSERT 를 바로 실행해 댓글 id 를 얻는다

        return postComment;
    }

    public boolean deleteComment(Post post, PostComment postComment) {
        return post.deleteComment(postComment);
    }

    public void modifyComment(PostComment postComment, String content) {
        postComment.modify(content);
    }

    // 044-A 나아가기 : 글 추천 / 추천 취소
    public void toggleVote(Post post, Member actor) {
        post.toggleVote(actor);
    }
}
