package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description: 笛风游客信息
 * @date 2020/12/1010:57
 */
@Data
public class Tourist implements Serializable {
    /**
     * 游客姓名
     */
    private String name;
    /**
     * 证件类型：
     *
     * 1、二代身份证
     *
     * 2、护照
     *
     * 3、军官证
     *
     * 4、港澳通行证
     *
     * 7、台胞证
     *
     * 8、回乡证
     *
     * 9、户口簿
     *
     * 10、出生证明
     *
     * 11、台湾通行证
     *
     * “门票详情接口->custInfoLimit“=3、6时必传；否则不要传。
     */
    private Integer psptType;
    /**
     * 证件号码
     *
     * “门票详情接口->custInfoLimit”=3、6时必填；否则不要传。
     */
    private String psptId;

    /**
     * 电话
     */
    private String tel;
    /**
     * 邮箱
     */
    private String email;
}
