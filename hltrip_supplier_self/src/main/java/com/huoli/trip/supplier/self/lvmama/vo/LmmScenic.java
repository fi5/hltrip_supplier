package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/16<br>
 */
@Data
public class LmmScenic {

    /**
     * 景区 ID 唯一性
     */
    private Integer scenicId;

    /**
     * 景区名称
     */
    private String scenicName;

    /**
     * 景区描述
     */
    private String placeInfo;

    /**
     * 景区主题
     */
    private String placeAct;

    /**
     * 景区级别
     * 0:无;
     * 1:A 级; 2:AA 级; 3:AAA 级; 4:AAAA 级; 5:AAAAA 级。
     */
    private String placeLevel;

    /**
     * 图片 URL
     */
    // todo 等拿到测试数据看下实际了类型，文档给的不一致
    private List<String> placeImage;

    /**
     * 景区详细地址
     */
    private String placeToAddr;

    /**
     * 景区营业时间列表
     */
    private List<LmmOpenTime> openTimes;

    /**
     * 所属镇和街道
     */
    private String placeTown;

    /**
     * 所属县
     */
    private String placeXian;

    /**
     * 省/自治区下取市，直 辖市和特别行政区下 取区
     */
    private String placeCity;

    /**
     * 国内取省级别
     */
    private String placeProvince;

    /**
     * 景区所在国家
     */
    private String placeCountry;

    /**
     * 景区经纬度(百度)
     */
    private LmmCoordinate baiduData;

    /**
     * 景区经纬度(谷歌)
     */
    private LmmCoordinate googleData;

    @Getter
    @Setter
    public class LmmCoordinate{

        /**
         * 景区所在经度
         */
        private Double longitude;

        /**
         * 景区所在纬度
         */
        private Double latitude;
    }
}
