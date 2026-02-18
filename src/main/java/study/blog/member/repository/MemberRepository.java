package study.blog.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
