package com.huoli.trip.supplier.web.yaochufa.convert;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.yaochufa.vo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class YcfConverter {

    /**
     * 转换数据库产品对象
     * @param product
     * @return
     */
    public static ProductPO convertToProductPO(YcfProduct product){
        ProductPO productPO = new ProductPO();
        productPO.setAllPreSale(product.getIsGlobalSale());
        productPO.setBookAheadMin(product.getBookAheadMin());
        productPO.setBookRules(JSON.toJSONString(product.getBookRules()));
        productPO.setBuyMax(product.getMaxNum());
        productPO.setBuyMaxNight(product.getMaxNight());
        productPO.setBuyMin(product.getMinNum());
        productPO.setBuyMinNight(product.getMinNight());
        productPO.setSupplierId(""); // todo
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), product.getProductID()));
        productPO.setDelayType(product.getAdvanceOrDelayType());
        productPO.setDescription(product.getProductDescription());
        productPO.setDisplayEnd(product.getGlobalSaleDisplayDateEnd());
        productPO.setDisplayStart(product.getGlobalSaleDisplayDateBegin());
        productPO.setExcludeDesc(product.getFeeExclude());
        productPO.setFoods(Lists.newArrayList(convertToFoodPO(product)));
        productPO.setImages(JSON.toJSONString(product.getProductImageList()));
        productPO.setIncludeDesc(product.getFeeInclude());
        productPO.setInvalidTime(product.getEndDate());
        productPO.setLimitRules(JSON.toJSONString(product.getLimitBuyRules()));
        productPO.setName(product.getProductName());
        productPO.setPreSaleDescription(product.getPreSaleDescription());
        productPO.setPreSaleEnd(product.getPreSaleDateEnd());
        productPO.setPreSaleStart(product.getPreSaleDateBegin());
        productPO.setPrice(product.getMarketPrice());
        productPO.setProductType(product.getProductType());
        productPO.setRefundAheadMin(product.getRefundPreMinute());
        productPO.setRefundDesc(product.getRefundNote());
        productPO.setRefundType(product.getRefundType());
        productPO.setRooms(Lists.newArrayList(convertToRoomPO(product)));
        productPO.setSalePrice(product.getMarketPrice());
        productPO.setStatus(product.getProductStatus());
        productPO.setSupplierName("要出发");
        productPO.setSupplierProductId(product.getProductID());
        productPO.setTickets(Lists.newArrayList(convertToTicketPO(product)));
        productPO.setValidTime(product.getStartDate());
        return productPO;
    }

    /**
     * 转换成餐饮数据库对象
     * @param product
     * @return
     */
    public static FoodPO convertToFoodPO(YcfProduct product){
        FoodPO foodPO = new FoodPO();
        foodPO.setChoiceNum(product.getFoodChoiceNum());
        foodPO.setOptionNum(product.getFoodOptionNum());
        if(ListUtils.isEmpty(product.getFoodList())){
            List<FoodInfoPO> foodInfoPOs = product.getFoodList().stream().map(food -> convertToFoodInfoPO(food)).collect(Collectors.toList());
            foodPO.setFoods(foodInfoPOs);
            List<String> poiIds = product.getFoodList().stream().map(f -> f.getPoiId()).collect(Collectors.toList());
            buildPoiIds(product, poiIds);
        }
        return foodPO;
    }

    /**
     * 转换成门票数据库对象
     * @param product
     * @return
     */
    public static TicketPO convertToTicketPO(YcfProduct product){
        TicketPO ticketPO = new TicketPO();
        ticketPO.setObtainTicketMode(product.getGetTicketMode());
        ticketPO.setTicketType(product.getTicketType());
        ticketPO.setChoiceNum(product.getTicketChoiceNum());
        ticketPO.setOptionNum(product.getFoodOptionNum());
        if(ListUtils.isEmpty(product.getTicketList())){
            List<TicketInfoPO> ticketInfoPOs = product.getTicketList().stream().map(ticket -> convertToTicketInfoPO(ticket)).collect(Collectors.toList());
            ticketPO.setTickets(ticketInfoPOs);
            List<String> poiIds = product.getTicketList().stream().map(t -> t.getPoiId()).collect(Collectors.toList());
            buildPoiIds(product, poiIds);
        }
        return ticketPO;
    }

    /**
     * 转成酒店数据库对象
     * @param product
     * @return
     */
    public static RoomPO convertToRoomPO(YcfProduct product){
        RoomPO roomPO = new RoomPO();
        roomPO.setChoiceNum(product.getTicketChoiceNum());
        roomPO.setOptionNum(product.getFoodOptionNum());
        if(ListUtils.isEmpty(product.getRoomList())){
            List<RoomInfoPO> roomInfoPOs = product.getRoomList().stream().map(room -> convertToRoomInfoPO(room)).collect(Collectors.toList());
            roomPO.setRooms(roomInfoPOs);
            List<String> poiIds = product.getRoomList().stream().map(r -> r.getPoiId()).collect(Collectors.toList());
            buildPoiIds(product, poiIds);
        }
        return roomPO;
    }

    /**
     * 转换成餐饮资源数据库对象
     * @param resourceFood
     * @return
     */
    public static FoodInfoPO convertToFoodInfoPO(YcfResourceFood resourceFood){
        FoodInfoPO foodInfoPO = new FoodInfoPO();
        foodInfoPO.setBaseNum(resourceFood.getFoodBaseNum());
        foodInfoPO.setItemId(""); // todo 项目应该先落地，然后这里去查库
        foodInfoPO.setSupplierItemId(resourceFood.getPoiId());
        foodInfoPO.setTitle(resourceFood.getFoodName());
        foodInfoPO.setSupplierResourceId(resourceFood.getFoodID());
        return foodInfoPO;
    }

    /**
     * 转换成门票资源数据库对象
     * @param resourceTicket
     * @return
     */
    public static TicketInfoPO convertToTicketInfoPO(YcfResourceTicket resourceTicket){
        TicketInfoPO ticketInfoPO = new TicketInfoPO();
        ticketInfoPO.setBaseNum(resourceTicket.getTicketBaseNum());
        ticketInfoPO.setItemId(""); // todo
        ticketInfoPO.setSupplierItemId(resourceTicket.getPoiId());
        ticketInfoPO.setSupplierResourceId(resourceTicket.getTicketID());
        ticketInfoPO.setTitle(resourceTicket.getTicketName());
        return ticketInfoPO;
    }

    /**
     * 转换成酒店资源数据库对象
     * @param resourceRoom
     * @return
     */
    public static RoomInfoPO convertToRoomInfoPO(YcfResourceRoom resourceRoom){
        RoomInfoPO roomInfoPO = new RoomInfoPO();
        roomInfoPO.setArea(resourceRoom.getArea());
        roomInfoPO.setBedSize(resourceRoom.getBedSize());
        roomInfoPO.setBedType(resourceRoom.getBedType());
        roomInfoPO.setBreakfast(resourceRoom.getBreakfast());
        roomInfoPO.setBroadNet(resourceRoom.getBroadNet());
        roomInfoPO.setEarliestTime(resourceRoom.getEarliestTime());
        roomInfoPO.setFacility(resourceRoom.getRoomFac());
        roomInfoPO.setLatestTime(resourceRoom.getLatestTime());
        roomInfoPO.setBaseNum(resourceRoom.getRoomBaseNum());
        roomInfoPO.setItemId(""); // todo
        roomInfoPO.setSupplierItemId(resourceRoom.getPoiId());
        roomInfoPO.setSupplierResourceId(resourceRoom.getRoomID());
        roomInfoPO.setTitle(resourceRoom.getRoomName());
        return roomInfoPO;
    }

    /**
     * 转换成产品项目数据库对象
     * @param productItem
     * @return
     */
    public static ProductItemPO convertToProductItemPO(YcfProductItem productItem){
        ProductItemPO productItemPO = new ProductItemPO();
        productItemPO.setSupplierItemId(productItem.getPoiID());
        productItemPO.setAddress(productItem.getAddress());
        productItemPO.setAppMainTitle(productItem.getAppMain());
        productItemPO.setAppSubTitle(productItem.getAppSub());
        productItemPO.setCity(productItem.getCity());
        productItemPO.setSupplierId(""); // todo
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productItem.getPoiID()));
        productItemPO.setCountry(productItem.getCountry());
        productItemPO.setDescription(productItem.getDescription());
        productItemPO.setFeatures(JSON.toJSONString(productItem.getCharacterrList()));
        productItemPO.setImages(JSON.toJSONString(productItem.getImageList()));
        productItemPO.setMainImages(JSON.toJSONString(productItem.getMainImageList()));
        productItemPO.setItemType(productItem.getPoiType());
        productItemPO.setLatitude(productItem.getLatitude());
        productItemPO.setLevel(productItem.getLevel());
        productItemPO.setLongitude(productItem.getLongitude());
        productItemPO.setMainTitle(productItem.getPcMain());
        productItemPO.setName(productItem.getPoiName());
        productItemPO.setPhone(productItem.getPhone());
        productItemPO.setProvince(productItem.getProvince());
        productItemPO.setSales(productItem.getSalesVolume());
        productItemPO.setScore(productItem.getRate());
        productItemPO.setSubTitle(productItem.getPcSub());
        productItemPO.setTags(JSON.toJSONString(productItem.getTags()));
        return productItemPO;
    }

    /**
     * 转换成价格数据库对象
     * @param price
     * @return
     */
    public static PricePO convertToPricePO(YcfPrice price){
        PricePO pricePO = new PricePO();
        pricePO.setProductId(""); // todo 需要查
        pricePO.setSupplierId(""); // todo
        pricePO.setCode(CommonUtils.genCodeBySupplier(pricePO.getSupplierId(), price.getProductID()));
        pricePO.setSupplierProductId(price.getProductID());
        if(ListUtils.isEmpty(price.getSaleInfos())){
            List<PriceInfoPO> priceInfoPOs = price.getSaleInfos().stream().map(priceInfo -> convertToPriceInfoPO(priceInfo)).collect(Collectors.toList());
            pricePO.setPriceInfos(priceInfoPOs);
        }
        return pricePO;
    }

    /**
     * 转换成价格日历数据库对象
     * @param priceInfo
     * @return
     */
    public static PriceInfoPO convertToPriceInfoPO(YcfPriceInfo priceInfo){
        PriceInfoPO priceInfoPO = new PriceInfoPO();
        priceInfo.setPriceType(priceInfo.getPriceType());
        priceInfo.setSaleDate(priceInfo.getSaleDate());
        priceInfo.setSalePrice(priceInfo.getSalePrice());
        priceInfo.setSettlePrice(priceInfo.getSettlePrice());
        priceInfo.setStock(priceInfo.getStock());
        return priceInfoPO;
    }

    /**
     * 集合所有产品项id
     * @param product
     * @param list
     */
    public static void buildPoiIds(YcfProduct product, List<String> list){
        List<String> ids = product.getProductItemIds();
        if(ids == null){
            ids = Lists.newArrayList();
        }
        ids.addAll(list);
    }
}
