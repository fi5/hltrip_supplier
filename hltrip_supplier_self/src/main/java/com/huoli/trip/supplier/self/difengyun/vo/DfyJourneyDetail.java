package com.huoli.trip.supplier.self.difengyun.vo;

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
 * 创建日期：2021/1/26<br>
 */
@Data
public class DfyJourneyDetail {

    /**
     * 行程ID
     */
    private Integer id;
    /**
     * 行程类型:1-时间线,2-资源
     */
    private Integer type;

    private JourneyDetail data;

    /**
     * 行程数据
     */
    @Getter
    @Setter
    public static class JourneyDetail{
        /**
         * 行程附注
         */
        private String content;  // 不要

        private List<Journey> data;
    }

    @Setter
    @Getter
    public static class Journey{
        /**
         * 第几天 资源类不存在天数 为0
         */
        private Integer day;
        /**
         * 交通信息 资源类不存在每天的行程交通信息 为空
         */
        private JourneyTraffic traffic;

        private List<JourneyModule> moduleList;
    }

    @Getter
    @Setter
    public static class JourneyTraffic{
        /**
         * 地点 poi id，自行输入时，id为0,（必填）
         */
        private Integer fromId;
        /**
         * 从哪里（必填）
         */
        private String from;

        private List<JourneyDestination> toList;
    }

    @Getter
    @Setter
    public static class JourneyDestination{
        /**
         * 地点 poi id，自行输入时，id为0（means不为‘无’时，必填）
         */
        private Integer toId;
        /**
         * 到哪里（means不为‘无’时，必填）
         */
        private String to;
        /**
         * 方式 "无"，"火车"，"飞机"，"轮船"，"汽车"，"自行安排"
         */
        private String means;
    }

    @Setter
    @Getter
    public static class JourneyModule{
        /**
         * 资源模块名 scenic-景点 hotel-酒店 traffic-小交通 food-餐饮 shopping-购物 activity-活动 reminder-提醒 ship-游轮
         */
        private String moduleType;
        /**
         * 资源模块类型 1-景点，2-酒店 3-小交通，4-餐饮，5-购物，6-活动，7-提醒,9-游轮//对应模块类型不同 对应某些字段才会有值
         */
        private Integer moduleTypeValue;
        /**
         * 上午、下午、晚上、全天
         */
        private String period;
        /**
         * 时间点 13:00，新数据里的“上午下午”以及“00：00”时间点统一的放到moment字段内
         */
        private String moment;  // 这个目前对应到 time 但是可能有汉字，没关系不格式化
        /**
         * 备注
         */
        private String remark;  // 不要
        /**
         * 描述
         */
        private String description;  // 优先取元素内的content,或者根据类型从不同地方取
        /**
         * 图片
         */
        private List<JourneyPicture> picture;
        /**
         * 模块类型为景点时
         * 景点---是否为并关系 0-或，1-并
         */
        private Integer relative; // 本地没有,不要
        /**
         * 景点
         */
        private List<ModuleScenic> scenicList;
        /**
         * 酒店
         */
        private List<ModuleHotel> hotelList;
        /**
         * 交通
         */
        private ModuleTraffic traffic;
        /**
         * 餐饮
         */
        private ModuleFood food;
        /**
         * 购物
         */
        private List<ModuleShop> shopList;
        /**
         * 活动
         */
        private ModuleActivity activity;
        /**
         * 温馨提示
         */
        private ModuleRemind remind;

    }

    @Getter
    @Setter
    public static class ModuleScenic{
        /**
         * 景点 poi id（必填）
         */
        private Integer id;  // 是否关联本地景点库，目前没有，不需要关联
        /**
         * 标题（必填）
         */
        private String title;
        /**
         * 游玩时间
         */
        private Integer times;  // 对应到playTime，转成hh:mm
        /**
         * 正文（必填）
         */
        private String content; // 本地没有，也对应了poiDesc,优先用此项
        /**
         * 图片
         */
        private List<JourneyPicture> picture;
    }

    @Getter
    @Setter
    public static class ModuleHotel{
        /**
         * 酒店POI id，自行输入时，id为0（必填）
         */
        private Integer id; // 是否对应本地酒店，目前没有，不需要
        /**
         * 酒店id，自行输入时，id为0（必填）
         */
        private Integer hotelId;
        /**
         * 酒店名（必填）
         */
        private String title;
        /**
         * 来自哪个系统
         */
        private Integer origin; // 本地没有，不要
        /**
         * 是否属于国内酒店
         */
        private Integer source; // 本地没有，不要
        /**
         * 酒店星级，1，2，3，4，5
         */
        private String starName;
        /**
         * 房间
         */
        private List<ModuleHotelRoom> room; //  本地只有一个房型，目前把集合拼成一个，把酒店描述和房型描述合并起来，名字用逗号分隔
        /**
         * 说明
         */
        private String description;
    }

    @Getter
    @Setter
    public static class ModuleHotelRoom{
        /**
         * 房型标题
         */
        private String title;
        /**
         * 描述
         */
        private String description;
        /**
         * 图片
         */
        private List<JourneyPicture> picture;
    }

    @Getter
    @Setter
    public static class ModuleTraffic{
        /**
         * 出发地
         */
        private String from;  // from meansType to 拼到poiname
        /**
         * 出发地id
         */
        private Integer fromId;
        /**
         * 方式 1-无，2-水飞，3-内飞，4-飞机，5-快艇，6-轮船，7-汽车，8-火车，9-自行安排
         */
        private Integer meansType;
        /**
         * 目的地
         */
        private String to;
        /**
         * 目的地ID
         */
        private Integer toId;
        /**
         * 交通经历时间
         */
        private Integer times;

    }

    @Getter
    @Setter
    public static class ModuleFood{
        /**
         * 餐饮id
         */
        private Integer id;
        /**
         * 餐饮标题
         */
        private String title;
        /**
         * 用餐时间
         */
        private Integer times;
        /**
         * 用餐时间
         */
        private Integer hour;
        /**
         * 用餐时间
         */
        private Integer min;
        /**
         * 餐食
         */
        private List<HasFood> hasList;  // 早餐：含/敬请自理 这种格式放到poiname
    }

    @Getter
    @Setter
    public static class ModuleShop{
        /**
         * 购物点poi（必填）
         */
        private Integer id;
        /**
         * 购物点名称（必填）
         */
        private String title;
        /**
         * 停留时间（必填）
         */
        private Integer times;
        /**
         * 营业产品（必填）
         */
        private String product;  // 拼到说明后面
        /**
         * 说明（必填）
         */
        private String instruction;
    }

    @Getter
    @Setter
    public static class ModuleActivity{
        /**
         * 活动标题（必填）
         */
        private String title;
        /**
         * 活动时间
         */
        private Integer times;
    }

    @Getter
    @Setter
    public static class ModuleRemind{
        /**
         * 提醒内容
         */
        private String content;
        /**
         * 温馨提示，当日概览
         */
        private String type;
    }

    @Getter
    @Setter
    public static class HasFood{
        /**
         * breakfast 早餐 lunch 中餐 dinner 晚餐
         */
        private String type;
        /**
         * 0否 1是
         */
        private Integer has;
    }

    @Getter
    @Setter
    public static class JourneyPicture{
        /**
         * 图片url
         */
        private String url;
        /**
         * 图片描述
         */
        private String title;
    }
}
