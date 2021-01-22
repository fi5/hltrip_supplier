package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:代扣
 * @date 2020/12/1415:10
 */
@Data
public class DfySubmitOrderRequest implements Serializable {
    private static final long serialVersionUID = -6646041412703672450L;
    /**
     * 笛风系统管理员账号
     */
    private String acctId;
    private String orderId;
    /**
     * 支付方式.1:途牛钱包代扣 3.企业支付宝代扣；
     */
    private String payType;
    /**
     * 付款金额。（单位元，不支持小数）
     * 如果付款金额和订单剩余应付金额不一致，接口不会扣款，会返回false。例如：订单金额500，未付款，剩余应付金额就是500。如果付款金额不是500，接口就会返回false，不会扣款。
     */
    private String pay;
    /**
     * 平台标识（10001:PC,20000:m站，30001:app安卓，30002:appIOS，30003:appWindows）（默认是pc）
     */
    private String platform;
}
