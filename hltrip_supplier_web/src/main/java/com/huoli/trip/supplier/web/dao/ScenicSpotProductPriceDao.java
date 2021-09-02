package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductPriceMPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/24<br>
 */
public interface ScenicSpotProductPriceDao {

    /**
     * 新增产品价格
     * @param scenicSpotProductPriceMPO
     * @return
     */
    ScenicSpotProductPriceMPO addScenicSpotProductPrice(ScenicSpotProductPriceMPO scenicSpotProductPriceMPO);

    /**
     * 获取产品价格
     * @param productId
     * @return
     */
    List<ScenicSpotProductPriceMPO> getByProductId(String productId);

    /**
     * 保存
     * @param scenicSpotProductPriceMPO
     */
    void saveScenicSpotProductPrice(ScenicSpotProductPriceMPO scenicSpotProductPriceMPO);

    ScenicSpotProductPriceMPO getPriceByPackageId(String packageId);

    void updatePriceStock(ScenicSpotProductPriceMPO priceMPO);

    /**
     * 查询已有价格
     * @param productId
     * @param ruleId
     * @param startDate
     * @return
     */
    ScenicSpotProductPriceMPO getExistPrice(String productId, String ruleId, String startDate);

    /**
     * 批量更新
     * @param scenicSpotProductPriceMPOs
     */
    void saveScenicSpotProductPrice(List<ScenicSpotProductPriceMPO> scenicSpotProductPriceMPOs);
}
