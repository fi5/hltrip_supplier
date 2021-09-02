package com.huoli.trip.supplier.self.universal.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRLoginResponse implements Serializable {

    /**
     * 鉴权
     */
    private UBRAuth auth;

    /**
     * 用户信息
     */
    @JsonProperty("user_info")
    private UBRUserInfo userInfo;

    @Setter
    @Getter
    public static class UBRAuth implements Serializable{
        /**
         * 秘钥
         */
        private String token;

        /**
         * 时间戳
         */
        private Long exp;
    }

    @Setter
    @Getter
    public static class UBRUserInfo implements Serializable{

        private String uid;
        /**
         * 账号
         */
        private String account;
        /**
         * 电话
         */
        private String telephone;
        /**
         * 商户名称
         */
        @JsonProperty("real_name")
        private String realName;
        /**
         * 头像
         */
        private String avatar;
        /**
         *
         */
        @JsonProperty("merchant_uid")
        private String merchantUid;
        /**
         * 状态
         */
        private Integer status;
        /**
         * 邮箱
         */
        private String email;
        /**
         * 最后登录时间
         */
        @JsonProperty("last_login")
        private String lastLogin;
    }
}
