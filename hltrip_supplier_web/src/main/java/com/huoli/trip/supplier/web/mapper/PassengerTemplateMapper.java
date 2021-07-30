package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.po.PassengerTemplatePO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/6/9<br>
 */
@Repository
@Mapper
public interface PassengerTemplateMapper {

    /**
     * 查询符合条件的模板
     * @param channel
     * @param peopleLimit
     * @param passengerInfo
     * @return
     */
    @Select("select id from trip_passenger_template where channel = #{channel} " +
            "and peopleLimit = #{peopleLimit} and passengerInfo = #{passengerInfo} and idInfo = #{idInfo} and status = 1")
    PassengerTemplatePO getPassengerTemplateByCond(@Param("channel") String channel,
                                                   @Param("peopleLimit") int peopleLimit,
                                                   @Param("passengerInfo") String passengerInfo,
                                                   @Param("idInfo") String idInfo);

    /**
     * 新增模板
     * @param passengerTemplatePO
     */
    @Insert("insert into trip_passenger_template (peopleLimit, passengerInfo, idInfo, templateType, channel, status, createTime) " +
            "values(#{peopleLimit}, #{passengerInfo}, #{idInfo}, #{templateType}, #{channel}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    void addPassengerTemplate(PassengerTemplatePO passengerTemplatePO);


    @Select("SELECT id FROM trip_passenger_template WHERE name=#{name}")
    int getIdByName(@Param("name") String name);

}
