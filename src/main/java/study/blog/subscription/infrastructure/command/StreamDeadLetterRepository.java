package study.blog.subscription.infrastructure.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.subscription.domain.entity.StreamDeadLetter;

public interface StreamDeadLetterRepository extends JpaRepository<StreamDeadLetter, Long> {
}