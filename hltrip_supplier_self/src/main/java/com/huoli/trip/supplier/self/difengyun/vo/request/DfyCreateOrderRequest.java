package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lunatic
 * @Title: DyfCreateOrderRequest
 * @Package
 * @Description: 创建订单请求参数对象
 * @date 2020/12/1010:47
 */
@Data
public class DfyCreateOrderRequest implements Serializable {
    private static final long serialVersionUID = -7706569465744965399L;
    /**
     * NO
     * 销商订单号。
     * 非空时，可防止重复下单。若重复下单，则返回之前已生成的orderId，且isNewFlag=0。
     */
    private String sourceOrderId;
    /**
     * 笛风账号
     */
    private String acctId;

    /**
     * 门票产品id
     */
    private String productId;

    /**
     * 出游日期. “YYYY-MM-DD”。请在前台限制，只有价格日历中存在的团期，才能作为出游日期。
     */
    private String startTime;

    /**
     * 预订数量
     */
    private Integer bookNumber;

    /**
     * 取票人信息
     */
    private Contact contact;

    /**
     * 游客资料列表。“门票详情接口->custInfoLimit“=2、3、6、7时，此字段必填。
     */
    private List<Tourist> touristList;

    /**
     * 配送信息。
     *
     * drawtype=1时，必填；否则不要填；
     */
    private Delivery delivery;

    /**
     * NO
     * 使用笛风券金额
     *
     * 使用笛风券会自动扣减订单金额，比如预订产品总额100元，使用旅游券10元，则下单后订单金额为90元；
     *
     * 假如使用笛风券金额=预订产品总额，则订单金额为0元，并且下单后不需要调用【出票（代扣）接口】，会自动出票，因为这种情况系统认为钱已经付清了；
     *
     * 如遇退票、预订失败等情况，已使用的笛风券会自动退回；
     */
    private Integer couponValue;
}
