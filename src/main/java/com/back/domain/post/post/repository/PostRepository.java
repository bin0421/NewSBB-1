package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {
    // 045-A 나아가기 : 제목·내용·작성자 아이디 검색 (kw 가 빈 문자열이면 전체)
    @Query("""
            select p from Post p
            left join p.author a
            where :kw = ''
               or p.title like concat('%', :kw, '%')
               or p.content like concat('%', :kw, '%')
               or a.username like concat('%', :kw, '%')
            """)
    Page<Post> search(@Param("kw") String kw, Pageable pageable);
}
