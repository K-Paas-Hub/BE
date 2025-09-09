package com.app.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class TestController {

    @GetMapping("/test")
    public String showTestPage() {
        return "test";
    }

    @GetMapping("/kakao")
    public String showKakaoPage() {
        return "kakao";
    }

}
