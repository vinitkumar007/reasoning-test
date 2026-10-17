package com.reasoningtest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // pehli baar chalane par "Test 1" banata hai.
    // Agar purane questions pehle se hain to unko isi Test 1 me daal deta hai,
    // nahi to 2 sample questions daalta hai
    @Bean
    CommandLineRunner seed(QuestionRepo questions, QuizRepo quizzes) {
        return args -> {
            if (quizzes.count() > 0) return;

            Quiz z = new Quiz();
            z.title = "Test 1";
            quizzes.save(z);

            List<Question> all = questions.findAll();
            if (!all.isEmpty()) {
                for (Question q : all) q.testId = z.id;
                questions.saveAll(all);
                return;
            }

            Question q1 = new Question();
            q1.text = "2, 4, 8, 16, ? series me agla number kya hoga?";
            q1.optionA = "24"; q1.optionB = "32"; q1.optionC = "30"; q1.optionD = "20";
            q1.correctOption = 2; q1.timeLimit = 30; q1.testId = z.id;
            questions.save(q1);

            Question q2 = new Question();
            q2.text = "Agar A, B ka bhai hai aur B, C ki behan hai, to C ka A se kya rishta hai?";
            q2.optionA = "Behan ya bhai"; q2.optionB = "Sirf bhai"; q2.optionC = "Sirf behan"; q2.optionD = "Chacha";
            q2.correctOption = 1; q2.timeLimit = 40; q2.testId = z.id;
            questions.save(q2);
        };
    }
}
