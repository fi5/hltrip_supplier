package com.huoli.trip.supplier.self.lvmama.vo.request;


import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
 */
@Data
public class LmmScenicRequest extends LmmBaseRequest {

    /**
     * 当前页数
     */
    private int currentPage = 1;
}
