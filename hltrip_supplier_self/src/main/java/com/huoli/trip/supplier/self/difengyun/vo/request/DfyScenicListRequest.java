package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyScenicListRequest {

    /**
     * 查询关键字,城市或者景点名称
     */
    private String key;

    /**
     * 第几页.页从1开始
     */
    private Integer page;

    /**
     * 分页大小
     */
    private Integer pageSize;
}
