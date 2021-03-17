package com.huoli.trip.supplier.self.lvmama.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/15<br>
 */
@Data
public class LmmBaseResponse {

    private LmmState state;

    private long totalPage;

    // todo 景点列表
    private List<LmmScenic>
}
