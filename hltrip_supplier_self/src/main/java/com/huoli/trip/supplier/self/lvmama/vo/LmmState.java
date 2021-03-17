package com.huoli.trip.supplier.self.lvmama.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/15<br>
 */
@Data
public class LmmState {

    /**
     * 状态码
     */
    @JacksonXmlProperty(localName = "code")
    private String code;

    /**
     * 状态描述
     */
    @JacksonXmlProperty(localName = "message")
    private String message;

    /**
     * 信息描述
     */
    @JacksonXmlProperty(localName = "solution")
    private String solution;
}
