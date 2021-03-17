package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:21
 */
@Data
public class Traveller implements Serializable {
    /**
     * 根据产品决 游玩人姓名 TRAV_NUM_NO:全不需要
     */
    private String name;
    /**
     * 游玩人手机 TRAV_NUM_NO:全不需要
     * TRAV_NUM_ONE：只要 1 个 TRAV_NUM_ALL：全部需要
     */
    private String mobile;
    /**
     * 根据产品决
     * 定
     * 游玩人英文名 TRAV_NUM_NO:全不需要
     * TRAV_NUM_ONE：只要 1 个 TRAV_NUM_ALL：全部需要
     */
    private String enName ;
    /**
     * 根据产品决
     * 定
     * 游玩人 email TRAV_NUM_NO:全不需要
     * TRAV_NUM_ONE：只要 1 个 TRAV_NUM_ALL：全部需要
     */
    private String email;
    /**
     * 根据产品决
     * 定
     * 游玩人证件 TRAV_NUM_NO:全不需要
     * TRAV_NUM_ONE：只要 1 个 TRAV_NUM_ALL：全部需要
     */
    private String credentials;
    /**
     * no
     * 游玩人生日 yyyy-MM-dd
     */
    private String birthday;
    /**
     * 根据产品决
     * 定
     * 证件类型
     * 身份证：ID_CARD
     * 护照：HUZHAO
     * 港澳通行证：GANGAO
     * 台湾通行证：TAIBAO
     */
    private String credentialsType;

    public Traveller(String name, String mobile, String email, String credentials, String credentialsType) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.credentials = credentials;
        this.credentialsType = credentialsType;
    }

    public Traveller(String name, String mobile, String enName, String email, String credentials, String birthday, String credentialsType) {
        this.name = name;
        this.mobile = mobile;
        this.enName = enName;
        this.email = email;
        this.credentials = credentials;
        this.birthday = birthday;
        this.credentialsType = credentialsType;
    }
}
