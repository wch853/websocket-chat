package com.njfu.chat.enums;

/**
 * 返回响应type类型
 */
public enum ResponseTypeEnum {

    ONLINE("online", "上线提示"),
    OFFLINE("offline", "下线提示"),
    AUTHENTICATE("authenticate", "认证信息"),
    ERROR("error", "连接异常"),
    CHAT("chat", "聊天文本");

    // 响应关键字
    private String key;

    // 类型说明
    private String info;

    ResponseTypeEnum(String key, String info) {
        this.key = key;
        this.info = info;
    }

    public String getKey() {
        return key;
    }

    public String getInfo() {
        return info;
    }
}
