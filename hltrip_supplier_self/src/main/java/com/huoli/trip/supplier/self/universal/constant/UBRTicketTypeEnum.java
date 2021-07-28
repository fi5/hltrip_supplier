package com.huoli.trip.supplier.self.universal.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
public enum UBRTicketTypeEnum {

    UBR_TICKET_TYPE_SINGLE("SINGLE", "单日票"),
    UBR_TICKET_TYPE_YEAR("YEAR", "年票"),
    UBR_TICKET_TYPE_SEASON("SEASON", "季票"),
    UBR_TICKET_TYPE_VIP("VIP", "VIP"),
    UBR_TICKET_TYPE_ALL("ALL", "ALL");



    /**
     * code
     */
    @Getter
    private String code;

    /**
     * 描述
     */
    @Getter
    private String desc;

    UBRTicketTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据类型code获取描述
     * @param code
     * @return
     */
    public static String getDesc(String code) {
        UBRTicketTypeEnum[] types = UBRTicketTypeEnum.values();
        for(UBRTicketTypeEnum type : types){
            if(StringUtils.equals(type.getCode(), code)){
                return type.getDesc();
            }
        }
        return null;
    }
}
