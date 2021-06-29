package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealMPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/31<br>
 */
public interface GroupTourProductSetMealDao {

    /**
     * 根据跟团游产品获取套餐
     * @param groupTourProductId
     * @return
     */
    GroupTourProductSetMealMPO getSetMeal(String groupTourProductId, String depCode);

    /**
     * 新增套餐
     * @param setMealMPO
     */
    void addSetMeals(GroupTourProductSetMealMPO setMealMPO);

    /**
     * 保存套餐
     * @param setMealMPO
     */
    void saveSetMeals(GroupTourProductSetMealMPO setMealMPO);

    /**
     * 根据产品获取所有套餐
     * @param groupTourProductId
     * @return
     */
    List<GroupTourProductSetMealMPO> getSetMealByProductId(String groupTourProductId);

    GroupTourProductSetMealMPO getSetMealByPackageId(String packageId);

    void updatePriceStock(GroupTourProductSetMealMPO groupTourProductSetMealMPO);
}
