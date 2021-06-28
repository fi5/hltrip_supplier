package com.huoli.trip.supplier.web.mapper;

import org.apache.ibatis.annotations.Insert;
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
 * 创建日期：2021/6/22<br>
 */
@Repository
@Mapper
public interface TripDictionaryMapper {

    /**
     * 根据名称查code
     * @param name
     * @return
     */
    @Select("select code from trip_dictionary where type = #{type} and name = #{name}")
    String getCodeByName(@Param("name") String name, @Param("type") Integer type);

    @Select("select code from trip_dictionary where type = #{type}")
    List<String> getCodesByType(@Param("type") Integer type);

    @Insert("insert into trip_dictionary (code, name, type) values(#{code}, #{name}, #{type})")
    void addDictionary(@Param("code") String code, @Param("name")String name, @Param("type") Integer type);
}
