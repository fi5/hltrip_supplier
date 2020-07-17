package com.huoli.trip.supplier.web.yaochufa.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import org.apache.commons.lang3.StringUtils;

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
        if(ListUtils.isNotEmpty(product.getBookRules())){
            productPO.setBookRules(product.getBookRules().stream().map(rule -> convertToBookRulePO(rule)).collect(Collectors.toList()));
        }
        productPO.setBuyMax(product.getMaxNum());
        productPO.setBuyMaxNight(product.getMaxNight());
        productPO.setBuyMin(product.getMinNum());
        productPO.setBuyMinNight(product.getMinNight());
        productPO.setSupplierId(Constants.SUPPLIER_CODE_YCF);
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), product.getProductID()));
        productPO.setMainItemCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), product.getPoiId()));
        productPO.setDelayType(product.getAdvanceOrDelayType());
        productPO.setDescription(product.getProductDescription());
        productPO.setDisplayEnd(product.getGlobalSaleDisplayDateEnd());
        productPO.setDisplayStart(product.getGlobalSaleDisplayDateBegin());
        productPO.setExcludeDesc(product.getFeeExclude());
        productPO.setFood(convertToFoodPO(product));
        if(ListUtils.isNotEmpty(product.getProductImageList())){
            productPO.setImages(product.getProductImageList().stream().map(imageBase -> convertToImageBasePO(imageBase)).collect(Collectors.toList()));
        }
        productPO.setIncludeDesc(product.getFeeInclude());
        productPO.setInvalidTime(product.getEndDate());
        if(ListUtils.isNotEmpty(product.getLimitBuyRules())){
            productPO.setLimitRules(product.getLimitBuyRules().stream().map(rule -> convertToLimitRulePO(rule)).collect(Collectors.toList()));
        }
        productPO.setName(product.getProductName());
        productPO.setPreSaleDescription(product.getPreSaleDescription());
        productPO.setPreSaleEnd(product.getPreSaleDateEnd());
        productPO.setPreSaleStart(product.getPreSaleDateBegin());
        productPO.setPrice(product.getMarketPrice());
        productPO.setRefundAheadMin(product.getRefundPreMinute());
        productPO.setRefundDesc(product.getRefundNote());
        productPO.setRefundType(product.getRefundType());
        productPO.setRoom(convertToRoomPO(product));
        productPO.setSalePrice(product.getMarketPrice());
        productPO.setStatus(product.getProductStatus());
        productPO.setSupplierName(Constants.SUPPLIER_NAME_YCF);
        productPO.setSupplierProductId(product.getProductID());
        productPO.setTicket(convertToTicketPO(product));
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
        if(!ListUtils.isEmpty(product.getFoodList())){
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
        if(!ListUtils.isEmpty(product.getTicketList())){
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
        if(!ListUtils.isEmpty(product.getRoomList())){
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
        foodInfoPO.setItemId(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, resourceFood.getPoiId()));
        foodInfoPO.setSupplierItemId(resourceFood.getPoiId());
        foodInfoPO.setTitle(resourceFood.getFoodName());
        foodInfoPO.setSupplierResourceId(resourceFood.getFoodId());
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
        ticketInfoPO.setItemId(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, resourceTicket.getPoiId()));
        ticketInfoPO.setSupplierItemId(resourceTicket.getPoiId());
        ticketInfoPO.setSupplierResourceId(resourceTicket.getTicketId());
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
        roomInfoPO.setBaseNight(resourceRoom.getRoomBaseNight());
        roomInfoPO.setArea(resourceRoom.getArea());
        roomInfoPO.setBedSize(resourceRoom.getBedSize());
        roomInfoPO.setBedType(resourceRoom.getBedType());
        roomInfoPO.setBreakfast(resourceRoom.getBreakfast());
        roomInfoPO.setBroadNet(resourceRoom.getBroadNet());
        if(resourceRoom.getEarliestTime() != null){
            roomInfoPO.setEarliestTime(DateTimeUtil.format(resourceRoom.getEarliestTime(), "HH:mm"));
        }
        if(resourceRoom.getLatestTime() != null){
            roomInfoPO.setLatestTime(DateTimeUtil.format(resourceRoom.getLatestTime(), "HH:mm"));
        }
        roomInfoPO.setFacility(resourceRoom.getRoomFac());
        roomInfoPO.setBaseNum(resourceRoom.getRoomBaseNum());
        roomInfoPO.setItemId(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, resourceRoom.getPoiId()));
        roomInfoPO.setSupplierItemId(resourceRoom.getPoiId());
        roomInfoPO.setSupplierResourceId(resourceRoom.getRoomId());
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
        String city = productItem.getCity();
        if(StringUtils.isNotBlank(city) && city.endsWith("市")){
            productItemPO.setCity(city);
        }
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_YCF);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productItem.getPoiID()));
        productItemPO.setCountry(productItem.getCountry());
        productItemPO.setDescription(productItem.getDescription());
        if(ListUtils.isNotEmpty(productItem.getCharacterrList())){
            productItemPO.setFeatures(productItem.getCharacterrList().stream().map(f -> convertToItemFeaturePO(f)).collect(Collectors.toList()));
        }
        if(ListUtils.isNotEmpty(productItem.getImageList())){
            productItemPO.setImages(productItem.getImageList().stream().map(i -> convertToImageBasePO(i)).collect(Collectors.toList()));
        }
        if(ListUtils.isNotEmpty(productItem.getMainImageList())){
            productItemPO.setMainImages(productItem.getMainImageList().stream().map(i -> convertToImageBasePO(i)).collect(Collectors.toList()));
        }
        productItemPO.setItemType(productItem.getPoiType());
        if(StringUtils.isNotBlank(productItem.getLatitude()) && StringUtils.isNotBlank(productItem.getLongitude())){
            productItemPO.setItemCoordinate(new Double[]{Double.parseDouble(productItem.getLongitude()), Double.parseDouble(productItem.getLatitude())});
        }
        productItemPO.setLevel(productItem.getLevel());
        productItemPO.setMainTitle(productItem.getPcMain());
        productItemPO.setName(productItem.getPoiName());
        productItemPO.setPhone(productItem.getPhone());
        productItemPO.setProvince(productItem.getProvince());
        productItemPO.setSales(productItem.getSalesVolume());
        productItemPO.setScore(productItem.getRate());
        productItemPO.setSubTitle(productItem.getPcSub());
        productItemPO.setTags(productItem.getTags());
        return productItemPO;
    }

    /**
     * 转换成价格数据库对象
     * @param price
     * @return
     */
    public static PricePO convertToPricePO(YcfPrice price){
        PricePO pricePO = new PricePO();
        pricePO.setSupplierProductId(price.getProductID());
        pricePO.setProductCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, price.getProductID()));
        if(!ListUtils.isEmpty(price.getSaleInfos())){
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
        priceInfoPO.setPriceType(priceInfo.getPriceType());
        priceInfoPO.setSaleDate(priceInfo.getDate());
        priceInfoPO.setSalePrice(priceInfo.getPrice());
        priceInfoPO.setSettlePrice(priceInfo.getSettlementPrice());
        priceInfoPO.setStock(priceInfo.getStock());
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
        product.setProductItemIds(ids);
    }

    /**
     * 转换成图片数据库对象
     * @param imageBase
     * @return
     */
    public static ImageBasePO convertToImageBasePO(YcfImageBase imageBase){
        ImageBasePO imageBasePO = new ImageBasePO();
        imageBasePO.setDesc(imageBase.getImageName());
        imageBasePO.setUrl(imageBase.getImageUrl());
        return imageBasePO;
    }

    /**
     * 转换成特殊说明数据库对象
     * @param itemFeature
     * @return
     */
    public static ItemFeaturePO convertToItemFeaturePO(YcfItemFeature itemFeature){
        ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
        itemFeaturePO.setDetail(itemFeature.getDetail());
        itemFeaturePO.setType(itemFeature.getType());
        return itemFeaturePO;
    }

    /**
     * 预定规则
     * @param bookRule
     * @return
     */
    public static BookRulePO convertToBookRulePO(YcfProductBookRule bookRule){
        BookRulePO bookRulePO = new BookRulePO();
        bookRulePO.setCnName(bookRule.getCName());
        bookRulePO.setCredential(bookRule.getCredential());
        bookRulePO.setCredentials(bookRule.getCredentialType());
        bookRulePO.setEmail(bookRule.getEmail());
        bookRulePO.setEnName(bookRule.getEName());
        bookRulePO.setPeopleNum(bookRule.getPeopleNum());
        bookRulePO.setPhone(bookRule.getMobile());
        bookRulePO.setRuleType(bookRule.getPersonType());
        return bookRulePO;
    }

    /**
     * 预售规则
     * @param limitRule
     * @return
     */
    public static LimitRulePO convertToLimitRulePO(YcfProductLimitRule limitRule){
        LimitRulePO limitRulePO = new LimitRulePO();
        limitRulePO.setLimitDays(limitRule.getBuyDay());
        limitRulePO.setLimitTotal(limitRule.getBuyCount());
        limitRulePO.setRuleType(limitRule.getBuyRuleType());
        return limitRulePO;
    }
}
