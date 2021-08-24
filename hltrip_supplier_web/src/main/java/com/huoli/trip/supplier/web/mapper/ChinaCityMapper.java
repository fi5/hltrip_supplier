package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.ChinaCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
@Repository
@Mapper
public interface ChinaCityMapper {

    @Select("<script> select * from china_city where name = #{name} and type = #{type}" +
            "<if test = 'parentCode != null'> and parentCode = #{parentCode}</if></script>")
    List<ChinaCity> getCityByNameAndTypeAndParentId(@Param("name") String name,
                                                    @Param("type") Integer type,
                                                    @Param("parentCode") String parentCode);

    @Select("select * from china_city where code = #{code}")
    ChinaCity getCityByCode(@Param("code") String code);

    @Select("SELECT code FROM china_city WHERE name LIKE CONCAT('%',#{condition},'%') AND parentCode is not null")
    List<String> queryCityCodeByName(@Param("condition") String condition);
}
