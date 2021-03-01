package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/25<br>
 */
@Data
public class DfyProductInfo {

    /**
     * 产品id
     */
    private String productId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 二级品类id
     */
    private Integer classBrandId;
    /**
     * 二级品类名称
     */
    private String classBrandName;
    /**
     * 是否多行程  1 是  0 否
     */
    private Integer isSupportMultipleJourney;
    /**
     * 目的地大类id
     */
    private Integer productNewLineTypeId;
    /**
     * 目的地大类名称
     */
    private String productNewLineTypeIdName;
    /**
     * 一级目的地分组id
     */
    private Integer firstDestGroupId;
    /**
     * 一级目的地分组名称
     */
    private String firstDestGroupName;
    /**
     * 二级目的地分组id
     */
    private Integer destGroupId;
    /**
     * 二级目的地分组名称
     */
    private String destGroupName;
    /**
     * 去程交通 1飞机 2火车卧铺 3火车硬卧 4火车软座 5火车硬座 6汽车 7邮轮 8火车 9动车组 10游船 11高铁二等座 12高铁一等座 13高铁商务座 14自行安排
     */
    private Integer trafficGo;
    /**
     * 返程交通 1飞机 2火车卧铺 3火车硬卧 4火车软座 5火车硬座 6汽车 7邮轮 8火车 9动车组 10游船 11高铁二等座 12高铁一等座 13高铁商务座 14自行安排
     */
    private Integer trafficBack;
    /**
     * 出发城市id
     */
    private Integer departCityCode;
    /**
     * 销量
     */
    private Integer sales;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 出发城市名称
     */
    private String departCityName;
    /**
     * 最低利润率 为0表示没有值
     */
    private Double minProfitRate;
    /**
     * 最高利润率 为0表示没有值
     */
    private Double maxProfitRate;
    /**
     * 平均利润率 为0表示没有值
     */
    private Double avgProfitRate;

}
