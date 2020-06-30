package com.huoli.trip.supplier.web.yaochufa.service;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.push.OrderStatusInfo;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;

/**
 * 描述: <br> 推送service
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public interface IYaoChuFaCallBackService {

    /**
     * 获取同步订单状态数据
     * @param req
     * @author= wangdm
     * @return
     */
    OrderStatusInfo synOrderStatus(YcfPushOrderStatusReq req);

    //退款通知
    void refundNotice(YcfRefundNoticeRequest req);
}
