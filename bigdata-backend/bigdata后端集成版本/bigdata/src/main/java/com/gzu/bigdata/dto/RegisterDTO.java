package com.gzu.bigdata.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String smsCode;       // 短信验证码
    private String inviteCode;    // 邀请码（可选）

    private String avatar;

}
