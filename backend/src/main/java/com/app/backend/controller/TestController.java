package com.app.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("")
public class TestController {

    @GetMapping("/test")
    public String showTestPage() {
        log.info("들어옴");
        return "test";
    }

    @GetMapping("/kakao")
    public String showKakaoPage() {
        log.info("카카오 들어옴");
        return "kakao";
    }

}
