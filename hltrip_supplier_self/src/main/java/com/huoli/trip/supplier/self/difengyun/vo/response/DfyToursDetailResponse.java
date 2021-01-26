package com.huoli.trip.supplier.self.difengyun.vo.response;

import com.huoli.trip.supplier.self.difengyun.vo.DfyCustomerCondition;
import com.huoli.trip.supplier.self.difengyun.vo.DfyImage;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.DfyPosition;
import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/26<br>
 */
@Data
public class DfyToursDetailResponse {

    /**
     * 产品名称
     */
    private String productName;

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

    /**
     * 出发城市
     */
    private List<String> departCitys;

    /**
     * 去程交通方式 1 飞机2 火车卧铺3 火车硬卧4 火车软座5 火车硬座6 汽车7 邮轮8 火车9 动车组10 游船11 高铁二等座12 高铁一等座13 高铁商务座14 自行安排
     */
    private Integer trafficGo;

    /**
     * 返程交通方式 1 飞机2 火车卧铺3 火车硬卧4 火车软座5 火车硬座6 汽车7 邮轮8 火车9 动车组10 游船11 高铁二等座12 高铁一等座13 高铁商务座14 自行安排
     */
    private Integer trafficBack;

    /**
     * 行程天数
     */
    private Integer duration;

    /**
     * 行程晚数
     */
    private Integer productNight;

    /**
     * 是否多行程 0 不是1 是
     */
    private Integer isSupportMultipleJourney;

    /**
     * 成团地点
     * :0出发地成团;1目的地成团;2中转地联运;3无
     */
    private Integer teamType;

    /**
     * 二级品类id
     */
    private Integer classBrandId;

    /**
     * 二级品类name
     */
    private String classBrandName;

    /**
     * 目的地大类id
     */
    private Integer productNewLineTypeId;

    /**
     * 目的地大类name
     */
    private String productNewLineTypeName;

    /**
     * 一级目的地分组id
     */
    private Integer firstDestGroupId;

    /**
     * 一级目的地分组name
     */
    private String firstDestGroupName;

    /**
     * 二级目的地分组id
     */
    private Integer destGroupId;

    /**
     * 二级目的地分组name
     */
    private String destGroupName;

    /**
     * 目的地id
     */
    private Integer productNewLineDestId;

    /**
     * 目的地name
     */
    private String productNewLineDestName;

    /**
     * 产品轮播图片信息
     */
    private List<DfyImage> productPicList;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 儿童价标准
     */
    private String childStdInfo;

    /**
     * 目的地信息（不保证与行程完全一致，仅用于推荐）
     */
    private List<DfyPosition> desPoiNameList;

    /**
     * 筛选条件信息
     */
    private List<DfyCustomerCondition> customCondition;

    /**
     * 行程详情
     */
    private DfyJourneyInfo journeyInfo;

}
