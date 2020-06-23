package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfProductBookRule {

    /**
     * 规则类型
     * 0联系人
     * 1出行人
     */
    private String personType;

    /**
     * 是否需要中文姓名
     */
    private Boolean cName;

    /**
     * 是否需要英文姓名
     */
    private Boolean eName;

    /**
     * 是否需要手机
     */
    private Boolean mobile;

    /**
     * 是否需要邮箱
     */
    private Boolean email;

    /**
     * 是否需要证件
     */
    private Boolean credential;

    /**
     * 需要填写人数
     * -3：三份套餐填一个
     * -2：两份套餐填一个
     * -1：一张订单填一个
     * 0：不需要
     * 1：一份套餐填一个
     * 2 :一份套餐填两个
     * 以此类推
     */
    private Integer peopleNum;   // todo 有没有一份填一个二份填二个？

    /**
     * 证件支持类型
     * 0：身份证
     * 1：护照
     * 2：港澳通行证
     * 3：台湾通行证
     * 4：回乡证
     * 5：台胞证
     * 6：士兵证
     * 7：军官证
     * 99：其他
     */
    private List<Integer> credentialType;

}
