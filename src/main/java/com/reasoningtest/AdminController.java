package com.reasoningtest;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired QuestionRepo questions;
    @Autowired AttemptRepo attempts;
    @Autowired QuizRepo quizzes;

    @Value("${admin.user}") String adminUser;
    @Value("${admin.pass}") String adminPass;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.initDirectFieldAccess();
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String user, @RequestParam String pass, HttpSession session, Model model) {
        if (user.equals(adminUser) && pass.equals(adminPass)) {
            session.setAttribute("admin", true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Username ya password galat hai");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        List<Map<String, Object>> quizList = new ArrayList<>();
        Map<Long, String> testNames = new HashMap<>();
        for (Quiz z : quizzes.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", z.id);
            m.put("title", z.title);
            m.put("count", questions.countByTestId(z.id));
            quizList.add(m);
            testNames.put(z.id, z.title);
        }

        model.addAttribute("quizzes", quizList);
        model.addAttribute("testNames", testNames);
        model.addAttribute("questions", questions.findAll());
        model.addAttribute("attempts", attempts.findAll());
        return "admin";
    }

    @PostMapping("/quiz/add")
    public String addQuiz(@RequestParam String title, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        if (!title.isBlank()) {
            Quiz z = new Quiz();
            z.title = title.trim();
            quizzes.save(z);
        }
        return "redirect:/admin";
    }

    @PostMapping("/quiz/delete/{id}")
    public String deleteQuiz(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        questions.deleteAll(questions.findByTestId(id));
        quizzes.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Question q, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        if (q.testId == null) return "redirect:/admin";
        if (q.timeLimit <= 0) q.timeLimit = 30;
        questions.save(q);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        questions.deleteById(id);
        return "redirect:/admin";
    }
}
