package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/26<br>
 */
@Data
public class DfyImage {

    /**
     * 图片名
     */
    private String name;

    /**
     * 图片路径
     */
    private String path;

    /**
     * 图片显示顺序
     */
    private String seqNum;
}
