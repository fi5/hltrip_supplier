package com.huoli.trip.supplier.web.caissa.service;

import com.huoli.trip.common.entity.mpo.groupTour.GroupTourPrice;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealMPO;

import java.io.IOException;
import java.util.List;

public interface ParseService {

    /**
     * 获取列表页
     *
     * @param url
     */
    void getList(String url) throws IOException;

    /**
     * 获取详情页
     *
     * @param defaultUrl
     * @param dbId
     */
    void getDetail(String defaultUrl, String dbId);

    String getDetail(String url, String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO, String activeDay) throws IOException;

    /**
     * 获取费用说明、须知
     *
     * @param dbId
     */
    void getFee(String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO) throws IOException;


    List<GroupTourPrice> getCalendars(String dbId) throws IOException;

    void getCalendars(String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO) throws IOException;

    void getWebCalendars(String url, GroupTourProductSetMealMPO mealMPO, String chdPrice) throws IOException;

}
