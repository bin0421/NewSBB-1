package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Post extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member author; // 042-A 나아가기 : 작성자
    private String title;
    private String content;

    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<PostComment> comments = new ArrayList<>();

    public Post(Member author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    // 043-A 나아가기 : 작성자 본인만 수정·삭제 가능
    public void checkActorCanModify(Member actor) {
        if (!isAuthor(actor)) throw new AccessDeniedException("작성자만 글을 수정할 수 있습니다.");
    }

    public void checkActorCanDelete(Member actor) {
        if (!isAuthor(actor)) throw new AccessDeniedException("작성자만 글을 삭제할 수 있습니다.");
    }

    // author 는 LAZY 프록시라 getClass() 를 쓰는 equals 대신 id 로 비교한다
    public boolean isAuthor(Member actor) {
        return author != null && actor != null && author.getId() == actor.getId();
    }

    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public PostComment addComment(Member author, String content) {
        PostComment postComment = new PostComment(this, author, content);
        comments.add(postComment);

        return postComment;
    }

    public Optional<PostComment> findCommentById(int id) {
        return comments
                .stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    public boolean deleteComment(PostComment postComment) {
        if (postComment == null) return false;

        return comments.remove(postComment);
    }
}