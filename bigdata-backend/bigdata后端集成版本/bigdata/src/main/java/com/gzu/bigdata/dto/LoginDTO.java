package com.gzu.bigdata.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String loginType;     // 登录类型：password,
    private String username;      // 用户名（密码登录用）
    private String password;      // 密码（密码登录用）
    private String phone;         // 手机号（短信登录用）
    private String smsCode;       // 验证码（短信登录用）
    private String email;         // 邮箱（邮箱登录用）
    private String authCode;      // 授权码（第三方登录用）
    private String platformType;  // 平台类型（第三方登录用）

    // ✨✨✨ 新增：图形验证码输入 ✨✨✨
    private String captchaInput;
}