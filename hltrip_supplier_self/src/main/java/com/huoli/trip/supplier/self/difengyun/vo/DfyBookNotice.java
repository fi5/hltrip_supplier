package com.huoli.trip.supplier.self.difengyun.vo;

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
public class DfyBookNotice {

    /**
     * 交通信息
     */
    private List<String> trafficInfos;

    /**
     * 住宿信息
     */
    private String accInfos;

    /**
     * 游览
     */
    private List<String> tour;

    /**
     * 购物
     */
    private String shopping;

    /**
     * 差价说明
     */
    private List<String> diffPrice;

    /**
     * 出团通知
     */
    private String departureNotice;

    /**
     * 意见反馈
     */
    private String suggestionFeedback;

    /**
     * 活动说明
     */
    private List<String> activityArrangment;

    /**
     * 附加预订须知
     */
    private List<String> orderAttentions;

    /**
     * 温馨提示
     */
    private String warmAttention;

    /**
     * 特殊信息
     */
    private List<String> specialTerms;

    /**
     * 注意事项
     */
    private List<String> abroadNotice;

    /**
     * 0:常规预订须知,1:自驾游手动编写预订须知
     */
    private Integer selfDrive;

    /**
     * 手动编写的预订须知，seleDrive=1是有值
     */
    private String manualAttention;

    /**
     * 团队用餐
     */
    private String mealInfos;
}
