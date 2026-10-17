package com.reasoningtest;

import jakarta.persistence.*;

@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long attemptId;
    public Long questionId;
    public int selected;     // 0 = skip / time out
    public boolean correct;
    public int timeTaken;    // seconds
}
