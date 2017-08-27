package com.njfu.chat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ChatController {

    @RequestMapping("/")
    public String index() {
        return "chat";
    }

    @RequestMapping("/1")
    public String index1() {
        return "chat1";
    }

}
