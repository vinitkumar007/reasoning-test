package com.reasoningtest;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 1000)
    public String text;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public int correctOption;   // 1 = A, 2 = B, 3 = C, 4 = D
    public int timeLimit;       // seconds
    public Long testId;         // kaunse test ka question hai
}
