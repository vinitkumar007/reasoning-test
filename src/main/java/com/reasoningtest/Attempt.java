package com.reasoningtest;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String studentName;
    public Long testId;
    public String testTitle;
    public LocalDateTime startedAt;
}
