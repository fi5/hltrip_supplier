package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.huoli.trip.supplier.self.yaochufa.vo.push.OrderStatusInfo;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.yaochufa.service.YcfSynOrderStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 描述: <br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Service
@Slf4j
public class YcfSynOrderStatusServiceImpl implements YcfSynOrderStatusService {

    @Override
    public OrderStatusInfo synOrderStatus(YcfPushOrderStatusReq req) {
        OrderStatusInfo orderInfos = new OrderStatusInfo();
        BeanUtils.copyProperties(req,orderInfos);
//        log.info("获取到推送过来的订单状态信息: {}", orderInfos.toString());
        return orderInfos;
    }
}
