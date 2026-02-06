package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class TranslateService {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Map<String, String> fallbackPhonetic = Map.of(
            "are", "/ɑːr/",
            "a", "/ə/",
            "the", "/ðə/",
            "to", "/tuː/"
    );

    public String translate(String text, String source, String target) {
        try {
            String url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=" + source +
                    "&tl=" + target +
                    "&dt=t&q=" + text;  // ❌ BỎ URLEncoder

            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            return root.get(0).get(0).get(0).asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Translation error";
        }
    }

    public String getPhonetic(String text) {
        try {
            StringBuilder result = new StringBuilder();

            // Tách câu thành từng từ
            String[] words = text.split(" ");

            for (String word : words) {

                String bestPhonetic = null;

                try {
                    String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
                    ResponseEntity<Map[]> response =
                            restTemplate.getForEntity(url, Map[].class);

                    if (response.getBody() != null && response.getBody().length > 0) {

                        Map first = response.getBody()[0];
                        var phonetics = (List<Map>) first.get("phonetics");

                        if (phonetics != null && !phonetics.isEmpty()) {
                            for (Map p : phonetics) {
                                if (p.get("text") != null) {
                                    bestPhonetic = p.get("text").toString();
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // 👇 FALLBACK CHO CÁC TỪ THÔNG DỤNG
                String wordLower = word.toLowerCase();

                if (bestPhonetic == null && fallbackPhonetic.containsKey(wordLower)) {
                    bestPhonetic = fallbackPhonetic.get(wordLower);
                }

                if (bestPhonetic != null) {
                    result.append(bestPhonetic).append(" ");
                }
            }

            return result.toString().trim();

        } catch (Exception e) {
            return "";
        }
    }
    public byte[] textToSpeech(String text, String lang) {
        try {
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&q="
                    + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&tl=" + lang + "&client=tw-ob";

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0");

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    byte[].class
            );

            return response.getBody();

        } catch (Exception e) {
            return null;
        }
    }


}
