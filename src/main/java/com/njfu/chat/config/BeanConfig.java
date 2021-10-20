package com.njfu.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author: Molk
 * @Date: 2021/10/20 16:38
 * @Description: Bean注入
 */

@Component
public class BeanConfig {

    /**
     * 用来存放在线的用户名
     */
    @Bean
    public CopyOnWriteArraySet<String> userNameSet(){
        return new CopyOnWriteArraySet<>();
    }
}
