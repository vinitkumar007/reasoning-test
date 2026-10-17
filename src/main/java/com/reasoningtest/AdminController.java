package com.reasoningtest;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired QuestionRepo questions;
    @Autowired AttemptRepo attempts;

    @Value("${admin.user}") String adminUser;
    @Value("${admin.pass}") String adminPass;

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
        model.addAttribute("questions", questions.findAll());
        model.addAttribute("attempts", attempts.findAll());
        return "admin";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Question q, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
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
