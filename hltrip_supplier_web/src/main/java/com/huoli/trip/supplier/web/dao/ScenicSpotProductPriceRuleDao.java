package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductPriceRuleMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/24<br>
 */
public interface ScenicSpotProductPriceRuleDao {

    /**
     * 新增价格规则
     * @param productPriceRuleMPO
     */
    void addScenicSpotProductPriceRule(ScenicSpotProductPriceRuleMPO productPriceRuleMPO);

    /**
     * 根据价格id查询
     * @param priceId
     * @return
     */
    ScenicSpotProductPriceRuleMPO getByPriceId(String priceId);
}
