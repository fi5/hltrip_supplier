package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotRuleMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/24<br>
 */
public interface ScenicSpotRuleDao {

    /**
     * 新增规则
     * @param scenicSpotRuleMPO
     * @return
     */
    ScenicSpotRuleMPO addScenicSpotRule(ScenicSpotRuleMPO scenicSpotRuleMPO);
}
