package com.njfu.chat.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 服务端响应
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    // 返回类型
    private String type;

    // 来源用户HttpSessionId
    private String httpSessionId;

    // 来源用户host
    private String host;

    // 来源用户昵称
    private String username;

    // 有效信息
    private Object payload;

    public ChatResponse() {
    }

    public ChatResponse(String httpSessionId, String host, String username) {
        this.httpSessionId = httpSessionId;
        this.host = host;
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "type='" + type + '\'' +
                ", httpSessionId='" + httpSessionId + '\'' +
                ", host='" + host + '\'' +
                ", username='" + username + '\'' +
                ", payload=" + payload +
                '}';
    }
}
