package com.huoli.trip.supplier.web.lvmama.service;

import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListByIdRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
 */
public interface LmmScenicService {

    /**
     * 同步景点
     * @param request
     */
    boolean syncScenicList(LmmScenicListRequest request);

    /**
     * 同步景点
     * @param request
     */
    void syncScenicListById(LmmScenicListByIdRequest request);

    /**
     * 同步景点
     * @param id
     */
    void syncScenicListById(String id);

    /**
     * 获取供应商景点id
     * @return
     */
    List<String> getSupplierScenicIds();
}
