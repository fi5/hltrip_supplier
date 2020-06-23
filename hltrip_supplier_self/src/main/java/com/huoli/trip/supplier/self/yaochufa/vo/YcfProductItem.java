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
public class YcfProductItem {

    /**
     * 主键
     */
    private String poiID;

    /**
     * 项目名称
     */
    private String poiName;

    /**
     * 项目类型
     * 1:酒店
     * 2:景区
     * 3:餐饮
     */
    private String poiType;

    /**
     * 主标题
     */
    private String pcMain;

    /**
     * 副标题
     */
    private String pcSub;

    /**
     * 移动端主标题
     */
    private String appMain;

    /**
     * 移动端副标题
     */
    private String appSub;

    /**
     * 销量
     */
    private Integer salesVolume;

    /**
     * 级别
     * 无级：0；
     * 一星：1；
     * 二星：2；
     * 三星：3；
     * 四星：4；
     * 五星：5；
     * A：11；
     * AA：12；
     * AAA：13；
     * AAAA：14；
     * AAAAA：15；
     */
    private Integer level;

    /**
     * 评分 eg：4.7
     */
    private Double rate;

    /**
     * 标签/属性
     */
    private List<String> tags;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 商家电话
     */
    private String phone;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目图片
     */
    private List<YcfImageBase> imageList;

    /**
     * 列表图
     */
    private List<YcfImageBase> mainImageList;

    /**
     * 产品特色
     */
    private List<ItemFeature> characterrList;
}
