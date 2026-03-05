package study.blog.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.member.entity.Member;

public interface MemberQueryRepository extends JpaRepository<Member, Long> {
}
