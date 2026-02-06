package com.example.demo.model;

import lombok.Data;

@Data
public class TranslateRequest {
    private String text;
    private String source;
    private String target;
}
