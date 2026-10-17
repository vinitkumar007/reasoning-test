package com.reasoningtest;

import jakarta.persistence.*;

// ek test (jaise "Test 1 - Series"), isme alag alag questions jude hote hain
@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;
}
