package com.huoli.trip.supplier.self.difengyun.vo.response;

import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyScenicListResponse {

    /**
     * 景点总数
     */
    private Integer totalCount;

    /**
     * 景点列表
     */
    private List<DfyScenic> rows;
}
