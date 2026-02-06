package com.example.demo.controller;

import com.example.demo.model.TranslateRequest;
import com.example.demo.service.TranslateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin // nếu sau này gọi khác port
public class TranslateController {
    private final TranslateService service;

    public TranslateController(TranslateService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/translate",
            consumes = "application/json",
            produces = "application/json;charset=UTF-8"
    )
    public Map<String, String> translate(@RequestBody TranslateRequest request) {

        String translated = service.translate(
                request.getText(),
                request.getSource(),
                request.getTarget()
        );

        String phonetic = "";
        if ("en".equalsIgnoreCase(request.getTarget())) {
            phonetic = service.getPhonetic(translated);
        }

        Map<String, String> result = new HashMap<>();
        result.put("translatedText", translated);
        result.put("phonetic", phonetic);

        return result;
    }

    @GetMapping(value = "/speak", produces = "audio/mpeg")
    public ResponseEntity<byte[]> speak(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String lang) {

        byte[] audio = service.textToSpeech(text, lang);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=speech.mp3")
                .body(audio);
    }
}
