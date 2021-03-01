package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/2/110:11
 */
@Data
public class ToursTourist implements Serializable {
    /**
     * 中文姓名 必填
     */
    private String name;
    /**
     * 英文名
     */
    private String firstname;
    /**
     * 因为姓
     */
    private String lastname;
    /**
     * 性别
     */
    private Integer sex;
    /**
     * 证件类型 1、二代身份证
     * 2、护照
     * 3、军官证
     * 4、港澳通行证
     * 6、其他
     * 7、台胞证
     *  必填
     */
    private Integer paper_type;
    /**
     * 证件号码
     */
    private String paper_num;
    /**
     * 出生年月
     */
    private String birthday;
    /**
     * 出游人类型：0成人1儿童
     */
    private Integer touristType;
    /**
     * 有效期
     */
    private String psptEndDate;
    /**
     * 手机
     */
    private String tel;
}
