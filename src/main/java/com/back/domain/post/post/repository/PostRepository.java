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

    // 045-B 나아가기 : 추천순 정렬 (추천 수가 같으면 최신순). 컬렉션 크기 정렬은 Sort 로 표현할 수 없어 JPQL 로 작성
    @Query(value = """
            select p from Post p
            left join p.author a
            where :kw = ''
               or p.title like concat('%', :kw, '%')
               or p.content like concat('%', :kw, '%')
               or a.username like concat('%', :kw, '%')
            order by size(p.voters) desc, p.id desc
            """,
            countQuery = """
            select count(p) from Post p
            left join p.author a
            where :kw = ''
               or p.title like concat('%', :kw, '%')
               or p.content like concat('%', :kw, '%')
               or a.username like concat('%', :kw, '%')
            """)
    Page<Post> searchOrderByVoteCount(@Param("kw") String kw, Pageable pageable);
}
