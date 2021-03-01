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
public class DfyJourneyInfo {
    /**
     * 行程ID
     */
    private Integer journeyId;

    /**
     * 适用团期
     */
    private List<String> applyDateList;

    /**
     * 几天几晚
     */
    private String daysAndNight;

    /**
     * 行程内容JSON格式
     */
    private DfyJourneyDetail journeyDescJson;

    /**
     * 重要信息_成团方式 组团形式:1,途牛独家发团;0,联合发团;2,无
     */
    private Integer independentTeam;

    /**
     * 儿童价标准
     */
    private String childStdInfo;

    /**
     * 重要信息_成团地点
     */
    private String beginPlaceDesc;

    /**
     * 重要信息_
     * 拼团方式   是否拼团:0,非行程中拼团;1,行程中拼团
     */
    private Integer joinGroupItem;

    /**
     * 附加说明
     */
    private List<String> importantAddition;

    /**
     * 预定须知
     */
    private DfyBookNotice bookNotice;

    /**
     * 费用包含
     */
    private String costInclude;

    /**
     * 费用不包含
     */
    private String costExclude;

    /**
     * 发车信息
     */
    private DfyTourTrafficInfo tourTrafficInfo;

    /**
     * 特殊人群限制
     */
    private String peopleLimitDesc;

    /**
     * 安全须知附件
     */
    private String safeNoticeUrl;

    /**
     * 文明公约附件
     */
    private String civilizedLedge;

    /**
     * 补充条款
     */
    private String legalRemark;

    /**
     * 高危项目安全须知
     */
    private List<DfyRiskContents> riskContents;

    /**
     * 目的地信息（不保证与行程完全一致，仅用于推荐）
     */
    private List<DfyPosition> desPoiNameList;

    /**
     * 产品推荐
     */
    private List<DfyTourRecommend> tourRecommend;
}
