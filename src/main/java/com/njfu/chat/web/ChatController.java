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

    /**
     * 验证是否存在用户信息
     * 根据HttpSession唯一确定用户身份
     *
     * @param session session
     * @return json
     */
    @RequestMapping("/verifyUser")
    public @ResponseBody
    String verifyUser(HttpSession session) {
        return (String) session.getAttribute("username");
    }

    /**
     * 新增用户信息
     *
     * @param session  session
     * @param username username
     */
    @RequestMapping("/addUser")
    public @ResponseBody
    void addUser(HttpSession session, String username) {
        session.setAttribute("username", username);
    }
}
