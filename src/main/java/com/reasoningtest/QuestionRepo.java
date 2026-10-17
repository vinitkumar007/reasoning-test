package com.reasoningtest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findByTestId(Long testId);
    long countByTestId(Long testId);
}
