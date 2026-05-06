package io.antcamp.assistantservice.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat-test")
public class ChatTestViewController {

    @GetMapping
    public String chatTest() {
        return "chat-test";
    }
}