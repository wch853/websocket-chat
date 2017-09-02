package com.njfu.chat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class ChatController {

    /**
     * index页
     *
     * @return page
     */
    @RequestMapping("/")
    public String index() {
        return "chat";
    }

    @RequestMapping("/verifyUser")
    public @ResponseBody
    String verifyUser(HttpSession session) {
        return (String) session.getAttribute("username");
    }

    @RequestMapping("/addUser")
    public @ResponseBody void addUser(HttpSession session, String username) {
        session.setAttribute("username", username);
    }
}
