package com.reasoningtest;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TestController {

    @Autowired QuestionRepo questions;
    @Autowired AttemptRepo attempts;
    @Autowired AnswerRepo answers;
    @Autowired QuizRepo quizzes;

    @GetMapping("/")
    public String home(Model model) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Quiz z : quizzes.findAll()) {
            long count = questions.countByTestId(z.id);
            if (count == 0) continue;     // khali test students ko nahi dikhana
            Map<String, Object> m = new HashMap<>();
            m.put("id", z.id);
            m.put("title", z.title);
            m.put("count", count);
            list.add(m);
        }
        model.addAttribute("quizzes", list);
        return "start";
    }

    @PostMapping("/start")
    public String start(@RequestParam String name, @RequestParam Long testId, HttpSession session) {
        Quiz quiz = quizzes.findById(testId).orElse(null);
        if (quiz == null) return "redirect:/";
        List<Long> ids = questions.findByTestId(testId).stream().map(q -> q.id).collect(Collectors.toList());
        if (ids.isEmpty()) return "redirect:/";
        Collections.shuffle(ids);

        Attempt a = new Attempt();
        a.studentName = name;
        a.testId = quiz.id;
        a.testTitle = quiz.title;
        a.startedAt = LocalDateTime.now();
        attempts.save(a);

        session.setAttribute("ids" + a.id, ids);
        session.setAttribute("idx" + a.id, 0);
        return "redirect:/test/" + a.id;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/test/{aid}")
    public String question(@PathVariable Long aid, HttpSession session, Model model) {
        List<Long> ids = (List<Long>) session.getAttribute("ids" + aid);
        if (ids == null) return "redirect:/";
        int idx = (int) session.getAttribute("idx" + aid);
        if (idx >= ids.size()) return "redirect:/result/" + aid;

        Question q = questions.findById(ids.get(idx)).orElse(null);
        if (q == null) {               // question beech me delete ho gaya to aage badho
            session.setAttribute("idx" + aid, idx + 1);
            return "redirect:/test/" + aid;
        }

        // page refresh karne se timer reset na ho
        Object shownFor = session.getAttribute("shownFor" + aid);
        if (shownFor == null || (int) shownFor != idx) {
            session.setAttribute("shownFor" + aid, idx);
            session.setAttribute("shownAt" + aid, System.currentTimeMillis());
        }
        long shownAt = (long) session.getAttribute("shownAt" + aid);
        int elapsed = (int) ((System.currentTimeMillis() - shownAt) / 1000);
        int remaining = Math.max(q.timeLimit - elapsed, 0);

        model.addAttribute("q", q);
        model.addAttribute("aid", aid);
        model.addAttribute("number", idx + 1);
        model.addAttribute("total", ids.size());
        model.addAttribute("remaining", remaining);
        return "question";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/test/{aid}/answer")
    public String answer(@PathVariable Long aid, @RequestParam(defaultValue = "0") int selected, HttpSession session) {
        List<Long> ids = (List<Long>) session.getAttribute("ids" + aid);
        if (ids == null) return "redirect:/";
        int idx = (int) session.getAttribute("idx" + aid);
        if (idx >= ids.size()) return "redirect:/result/" + aid;

        Question q = questions.findById(ids.get(idx)).orElse(null);
        if (q != null) {
            Object shownAt = session.getAttribute("shownAt" + aid);
            int taken = shownAt == null ? 0 : (int) ((System.currentTimeMillis() - (long) shownAt) / 1000);

            // server pe bhi time check, browser se cheating na ho
            if (taken > q.timeLimit + 2) selected = 0;
            taken = Math.min(taken, q.timeLimit);

            Answer a = new Answer();
            a.attemptId = aid;
            a.questionId = q.id;
            a.selected = selected;
            a.correct = selected != 0 && selected == q.correctOption;
            a.timeTaken = taken;
            answers.save(a);
        }
        session.setAttribute("idx" + aid, idx + 1);
        session.removeAttribute("shownFor" + aid);
        return "redirect:/test/" + aid;
    }

    @GetMapping("/result/{aid}")
    public String result(@PathVariable Long aid, Model model) {
        Attempt attempt = attempts.findById(aid).orElse(null);
        if (attempt == null) return "redirect:/";

        List<Answer> list = answers.findByAttemptId(aid);
        int correct = 0, wrong = 0, skipped = 0, totalTime = 0;
        List<Map<String, Object>> rows = new ArrayList<>();

        for (Answer a : list) {
            Question q = questions.findById(a.questionId).orElse(null);
            if (a.selected == 0) skipped++;
            else if (a.correct) correct++;
            else wrong++;
            totalTime += a.timeTaken;

            Map<String, Object> row = new HashMap<>();
            row.put("text", q == null ? "(question delete ho chuka hai)" : q.text);
            row.put("yours", a.selected == 0 ? "Skip" : optionText(q, a.selected));
            row.put("right", q == null ? "-" : optionText(q, q.correctOption));
            row.put("status", a.selected == 0 ? "skip" : (a.correct ? "ok" : "bad"));
            row.put("time", a.timeTaken);
            rows.add(row);
        }

        int total = list.size();
        model.addAttribute("name", attempt.studentName);
        model.addAttribute("testTitle", attempt.testTitle);
        model.addAttribute("total", total);
        model.addAttribute("correct", correct);
        model.addAttribute("wrong", wrong);
        model.addAttribute("skipped", skipped);
        model.addAttribute("accuracy", total == 0 ? 0 : Math.round(correct * 100.0 / total));
        model.addAttribute("totalTime", totalTime);
        model.addAttribute("avgTime", total == 0 ? 0 : Math.round(totalTime * 10.0 / total) / 10.0);
        model.addAttribute("rows", rows);
        return "result";
    }

    private String optionText(Question q, int n) {
        if (q == null) return "-";
        switch (n) {
            case 1: return q.optionA;
            case 2: return q.optionB;
            case 3: return q.optionC;
            case 4: return q.optionD;
            default: return "-";
        }
    }
}
