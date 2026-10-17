package com.reasoningtest;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/admin")
public class GeminiController {
 
    // key aur model Render ke Environment variables se aate hain (code me key nahi rakhni)
    @Value("${GEMINI_KEY:}")
    private String apiKey;
 
    @Value("${GEMINI_MODEL:gemini-2.5-flash}")
    private String model;
 
    private final ObjectMapper mapper = new ObjectMapper();
 
    @PostMapping(value = "/generate", produces = "application/json")
    public ResponseEntity<String> generate(@RequestParam String topic,
                                           @RequestParam(defaultValue = "5") int count,
                                           @RequestParam(defaultValue = "Hard") String difficulty,
                                           @RequestParam(defaultValue = "English") String language,
                                           HttpSession session) {
 
        if (session.getAttribute("admin") == null) return error(401, "Pehle admin login karo");
        if (apiKey.isBlank()) return error(400, "GEMINI_KEY set nahi hai (Render > Environment me daalo)");
        count = Math.max(1, Math.min(count, 10));
 
        String prompt = "Create " + count + " multiple choice reasoning questions on the topic: " + topic + ". "
                + "Difficulty: " + difficulty + " (competitive exam level, tricky multi-step questions, NOT easy). "
                + "Language: " + language + ". "
                + "Each question must have exactly 4 options and exactly one correct option. "
                + "Solve every question step by step first and make sure the correct option is really right. "
                + "Do not use images or diagrams, describe everything in text. "
                + "Return ONLY a JSON array with no extra text. Each item must look like: "
                + "{\"text\": string, \"optionA\": string, \"optionB\": string, \"optionC\": string, "
                + "\"optionD\": string, \"correctOption\": number from 1 to 4, \"timeLimit\": seconds between 60 and 150}";
 
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json"));
 
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(90000);
        RestTemplate rest = new RestTemplate(factory);
 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
 
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
 
        try {
            String resp = rest.postForObject(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = mapper.readTree(resp);
            String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            text = text.replace("```json", "").replace("```", "").trim();
 
            JsonNode arr = mapper.readTree(text);
            if (!arr.isArray() || arr.size() == 0) {
                return error(502, "Gemini ne sahi format me jawab nahi diya, dobara try karo");
            }
            return ResponseEntity.ok(mapper.writeValueAsString(arr));
 
        } catch (HttpStatusCodeException e) {
            int code = e.getStatusCode().value();
            String msg;
            if (code == 429) msg = "Free limit khatam ho gayi, thodi der baad try karo";
            else if (code == 404) msg = "Model ka naam galat ya purana hai (Render me GEMINI_MODEL badlo)";
            else if (code == 400 || code == 403) msg = "API key galat hai ya is model ke liye allowed nahi hai";
            else msg = "Gemini error " + code;
            return error(502, msg);
        } catch (Exception e) {
            return error(500, "Questions nahi ban paye, dobara try karo");
        }
    }
 
    private ResponseEntity<String> error(int status, String msg) {
        String json;
        try {
            json = mapper.writeValueAsString(Map.of("error", msg));
        } catch (Exception e) {
            json = "{\"error\":\"error\"}";
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(json);
    }
}

      
