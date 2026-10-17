package com.reasoningtest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepo extends JpaRepository<Answer, Long> {
    List<Answer> findByAttemptId(Long attemptId);
}
