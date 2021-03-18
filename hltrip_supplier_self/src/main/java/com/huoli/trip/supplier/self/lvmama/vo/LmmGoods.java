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
 * 创建日期：2021/3/18<br>
 */
@Data
public class LmmGoods {

    /**
     * 商品所属规格名称
     */
    private String standardName;

    private String goodsId;

    /**
     * 商品ID
     * 外键含下划健的ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品分类(普通票:common、特殊票(期票):special，实体票：EXPRESSTYPE_DISPLAY)
     */
    private String goodsSort;

    /**
     * 商品状态(true上线、false下线)
     */
    private String status;

    /**
     * online:预付给驴妈妈
     * offline:景区现付
     */
    private String  paymentType;

    /**
     * PARENTAGE 亲子票
     * FAMILY 家庭票
     * LOVER 情侣票
     * COUPE 双人票
     * ADULT 成人票
     * CHILDREN 儿童票
     * OLDMAN 老人票
     * STUDENT 学生票
     * ACTIVITY 活动票
     * SOLDIER 军人票
     * MAN 男士票
     * WOMAN 女士票
     * TEACHER 教师票
     * DISABILITY 残疾票
     * GROUP 团体票
     * FREE 自定义
     */
    private String ticketType;

    /**
     * 成人数
     */
    private int adultTicket;

    /**
     * 儿童数
     */
    private int childTicket;

    /**
     * EXPRESSTYPE_DISPLAY 实体商品
     * NOTICETYPE_DISPLAY 虚拟商品
     */
    private String  goodsType;

    /**
     * SMS 普通短信
     * QRCODE 二维码
     * EMAIL 邮件
     * 邮件需要过滤
     */
    private String certificate;

    /**
     * 指定出游日几日内有效， 取值为具体数字
     */
    private int effective;

    /**
     * 结算方式
     */
    private String settlementMethod;

    /**
     * 退改规则
     */
    private List<Rule> rules;

    /**
     * 不满足上述时间条件时的退改规则。默认不显示，根据商品是否设置其他规则而输出。
     */
    private Rule otherRule;

    /**
     * 费用包含
     */
    private String costInclude;

    /**
     * 费用不包含
     */
    private String costNoinclude;


    /**
     * 最小购买数量
     */
    private int minimum;

    /**
     * 最大购买数量
     */
    private int maximum;

    /**
     * 商品购买须知
     */
    private Notice notice;

    private String visitAddress;

    /**
     * 是否需要取票
     */
    private String needTicket;

    /**
     *票种说明
     */
    private String Ticketdescription;

    /**
     * 通关时间限制，组合门票按“名称+限制时间”展现各单门票的限制
     */
    private PassTimeLimit passTimeLimit;

    /**
     *重要提示+退改规则组合方式
     */
    private String importentPoint;

    /**
     * 重要提示,单独方式
     */
    private String importantNotice;

    /**
     * 退改规则,单独方式
     */
    private String refundRuleNotice;

    /**
     * 预定限制
     */
    private Limitation limitation;

    /**
     * 预定人信息
     */
    private Booker booker;

    /**
     * 游玩人信息
     */
    private Traveller traveller;

    /**
     * 是否场次票
     */
    private String ticketSeason;

    /**
     * 除了证件类型其它都是是否
     */
    @Getter
    @Setter
    public class Traveller{
        /**
         * 游玩人姓名
         */
        private String name;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 英文名称
         */
        private String enName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 证件
         */
        private String credentials;

        /**
         * 身份证:ID_CARD
         * 护照:HUZHAO
         * 港澳通行证:GANGAO
         * 台湾通行证:TAIBAO
         */
        private String credentialsType;
    }

    @Getter
    @Setter
    public class Booker{
        /**
         * 名字是否必须填
         */
        private boolean name;

        /**
         * 是否必须填
         */
        private boolean mobile;

        /**
         * 是否必须填
         */
        private boolean email;
    }

    @Getter
    @Setter
    public class Limitation{
        /**
         * 限制类型，手机号、身份证限购
         * phoneNum 代表手机号，
         * IDcard 代表身份证号，
         * phoneAndIDCard 代表手机+身份证
         */
        private String limitType;


        /**
         * 下单开始时间
         * HH:mm:ss
         */
        private String orderStartTime;

        /**
         * 下单结束时间
         */
        private String orderEntTime;


        /**
         * 游玩开始时间
         * HH:mm:ss
         */
        private String playStartTime;


        /**
         * 游玩结束时间
         */
        private String playEntTime;


        /**
         * orderTime(下单日)
         * playTime(游玩日)
         */
        private String timeType;


        /**
         *限制数量的周期
         * 比如每 1 天限制下单 3 张
         * 每 3 天限制下单 1 张，单位天
         */
        private int amountCycle;


        /**
         * ORDERNUM 订单数
         * GOODSNUM 商品数量
         */
        private String limitWay;

        /**
         * 限制数量,单位:订单为“笔”，票为“张”。
         */
        private int limitAmount;
    }
    
    @Getter
    @Setter
    public class Notice{

        /**
         * 取票时间(入园 须知内容项)
         */
        private String getTicketTime;

        /**
         * 取票地点(入园 须知内容项)
         */
        private String getTicketPlace;

        /**
         * 入园方式(入园 须知内容项)
         */
        private String ways;

        /**
         * 有效期(入园须 知内容项)
         */
        private String effectiveDesc;

        /**
         * 入园限制
         */
        private EnterLimit enterLimit;
    }

    @Getter
    @Setter
    public class PassLimit{

        private String goodsName;

        private int passLimitTime;
    }

    @Getter
    @Setter
    public class EnterLimit{
        /**
         * 是否有入园限制
         */
        private boolean limitFlag;

        /**
         * 限制时间格式 hh:mm
         */
        private String limitTime;
    }

    @Getter
    @Setter
    public class Rule{
        /**
         * 是否可退
         */
        private boolean isChange;

        /**
         * 退款申请前时间或游玩日后时间，单位分。
         * 值为正数:代表游玩前时间;
         * 举例:如值为 1，代表游玩 前一日 23:59 分前申请;
         * 值为负数:代表游玩日后 的时间;
         * 举例:如值为-1439，代表 游玩日当天 23:59 分前申 请;
         */
        private int aheadTime;

        /**
         * 扣费类型
         * AMOUNT 金额
         * PERCENT 百分比
         */
        private  String deductionType;

        /**
         * 扣费金额
         */
        private double deductionValue;
    }

    @Getter
    @Setter
    public class PassTimeLimit{

        private List<PassLimit> passLimit;
    }

}
