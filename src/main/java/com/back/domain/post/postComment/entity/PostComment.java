package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Post post; // POST_ID
    @ManyToOne(fetch = LAZY)
    private Member author; // 042-A 나아가기 : 작성자
    private String content;

    public PostComment(Post post, Member author, String content) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    // 043-A 나아가기 : 작성자 본인만 수정·삭제 가능
    public void checkActorCanModify(Member actor) {
        if (!isAuthor(actor)) throw new AccessDeniedException("작성자만 댓글을 수정할 수 있습니다.");
    }

    public void checkActorCanDelete(Member actor) {
        if (!isAuthor(actor)) throw new AccessDeniedException("작성자만 댓글을 삭제할 수 있습니다.");
    }

    public boolean isAuthor(Member actor) {
        return author != null && actor != null && author.getId() == actor.getId();
    }

    public void modify(String content) {
        this.content = content;
    }
}