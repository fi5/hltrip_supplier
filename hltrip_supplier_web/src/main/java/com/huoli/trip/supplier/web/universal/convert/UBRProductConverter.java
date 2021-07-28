package com.huoli.trip.supplier.web.universal.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.RefundRule;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotRuleMPO;
import com.huoli.trip.supplier.self.universal.vo.UBRBaseProduct;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
public class UBRProductConverter {

    public static ScenicSpotProductMPO convertToProduct(UBRTicketInfo ticketInfo){
        /*
        ScenicSpotProductMPO productMPO = new ScenicSpotProductMPO();
        UBRBaseProduct baseProduct = ticketInfo.getBaseProduct();
        productMPO.setSupplierProductId(baseProduct.getCode());
        productMPO.setId();
        productMPO.setName(baseProduct.getName());
        ScenicSpotRuleMPO ruleMPO = new ScenicSpotRuleMPO();
        ruleMPO.setId();
        ruleMPO.setScenicSpotId();
        ruleMPO.setRuleName("退改规则");
        ruleMPO.setMaxCount(StringUtils.isBlank(ticketInfo.getMaxQuantity()) ? 99 : Integer.valueOf(ticketInfo.getMaxQuantity()));
        // todo 是否需要最小购买数量
        // todo 是否需要最大最小年龄
        // todo 没有
        //      passType	string 年卡季卡类型：Seasonal, Silver, Gold, Platinum
        //      ticketCategory	string 票类别：Park Ticket, Express, VIP Experiences, Annual Pass
        //      uepEntitlement	string 快速通道票的类型
        if(ticketInfo.getRefundable()){
            ruleMPO.setRefundCondition();
            RefundRule refundRule = new RefundRule();
            // todo 其它，供应商没有类型
            refundRule.setRefundRuleType(5);
            refundRule.setDeductionType(1);
            refundRule.setFee(StringUtils.isBlank(ticketInfo.getServiceFee()) ? 0d : Double.valueOf(ticketInfo.getServiceFee()));
            ruleMPO.setRefundRules(Lists.newArrayList(refundRule));
        }
        ruleMPO.setTicketInfos(Lists.newArrayList(0, 1, 2));
        ruleMPO.setTicketCardTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        ruleMPO.setTravellerInfos(Lists.newArrayList(0, 1));
        ruleMPO.setTravellerTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        // todo 缺少证件或人脸识别
        if(StringUtils.equals(ticketInfo.getMediaType(), "GID/FR")){
            ruleMPO.setVoucherType();
        } else if(StringUtils.equals(ticketInfo.getMediaType(), "QR Code")){
            ruleMPO.setVoucherType(0);
        } else {
            ruleMPO.setVoucherType(5);
        }
        productMPO.setScenicSpotId(ruleMPO.getId());
        productMPO.setPcDescription(ticketInfo.getDescription());
         */
        return null;
    }
}
