package com.huoli.trip.supplier.web.yaochufa.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.hotel.HotelMPO;
import com.huoli.trip.common.entity.mpo.hotel.HotelPic;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.*;
import com.huoli.trip.supplier.self.util.CommonUtil;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Slf4j
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
        productPO.setDisplayEnd(MongoDateUtils.handleTimezoneInput(parseDate(product.getGlobalSaleDisplayDateEnd())));
        productPO.setDisplayStart(MongoDateUtils.handleTimezoneInput(parseDate(product.getGlobalSaleDisplayDateBegin())));
        productPO.setExcludeDesc(product.getFeeExclude());
        productPO.setFood(convertToFoodPO(product));
        if(ListUtils.isNotEmpty(product.getProductImageList())){
            productPO.setImages(product.getProductImageList().stream().map(imageBase -> convertToImageBasePO(imageBase)).collect(Collectors.toList()));
        }
        productPO.setIncludeDesc(product.getFeeInclude());
        productPO.setInvalidTime(MongoDateUtils.handleTimezoneInput(parseDate(product.getEndDate())));
        if(ListUtils.isNotEmpty(product.getLimitBuyRules())){
            productPO.setLimitRules(product.getLimitBuyRules().stream().map(rule -> convertToLimitRulePO(rule)).collect(Collectors.toList()));
        }
        productPO.setName(product.getProductName());
        productPO.setPreSaleDescription(product.getPreSaleDescription());
        productPO.setPreSaleEnd(MongoDateUtils.handleTimezoneInput(parseDate(product.getPreSaleDateEnd())));
        productPO.setPreSaleStart(MongoDateUtils.handleTimezoneInput(parseDate(product.getPreSaleDateBegin())));
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
        productPO.setValidTime(MongoDateUtils.handleTimezoneInput(parseDate(product.getStartDate())));
        productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        productPO.setOperator(Constants.SUPPLIER_CODE_YCF);
        productPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
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
            List<TicketInfoPO> ticketInfoPOs = product.getTicketList().stream().map(ticket -> convertToTicketInfoPO(ticket, product.getTicketType())).collect(Collectors.toList());
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
    public static TicketInfoPO convertToTicketInfoPO(YcfResourceTicket resourceTicket, Integer ticketType){
        TicketInfoPO ticketInfoPO = new TicketInfoPO();
        ticketInfoPO.setBaseNum(resourceTicket.getTicketBaseNum());
        ticketInfoPO.setItemId(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, resourceTicket.getPoiId()));
        ticketInfoPO.setSupplierItemId(resourceTicket.getPoiId());
        ticketInfoPO.setSupplierResourceId(resourceTicket.getTicketId());
        ticketInfoPO.setTitle(resourceTicket.getTicketName());
        ticketInfoPO.setTicketType(ticketType);
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
        if(ListUtils.isNotEmpty(resourceRoom.getBroadNet())){
            roomInfoPO.setBroadNet(String.join(",", resourceRoom.getBroadNet().stream().map(net ->
                    String.valueOf(net)).collect(Collectors.toList())));
        }
        if(resourceRoom.getEarliestTime() != null){
            roomInfoPO.setEarliestTime(DateTimeUtil.format(DateTimeUtil.parseFullDate(resourceRoom.getEarliestTime()), "HH:mm"));
        }
        if(resourceRoom.getLatestTime() != null){
            roomInfoPO.setLatestTime(DateTimeUtil.format(DateTimeUtil.parseFullDate(resourceRoom.getLatestTime()), "HH:mm"));
        }
        if(ListUtils.isNotEmpty(resourceRoom.getRoomFac())){
            roomInfoPO.setFacility(String.join(",", resourceRoom.getRoomFac().stream().map(fac ->
                    String.valueOf(fac)).collect(Collectors.toList())));
        }
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
        productItemPO.setStatus(1);
        productItemPO.setSupplierItemId(productItem.getPoiID());
        productItemPO.setAddress(productItem.getAddress());
        productItemPO.setAppMainTitle(productItem.getAppMain());
        productItemPO.setAppSubTitle(productItem.getAppSub());
        productItemPO.setCity(CommonUtil.getCity(productItem.getCity()));
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_YCF);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productItem.getPoiID()));
        productItemPO.setCountry(productItem.getCountry());
        productItemPO.setDescription(productItem.getDescription());
        if(ListUtils.isNotEmpty(productItem.getCharacterrList())){
            productItemPO.setFeatures(productItem.getCharacterrList().stream().map(f ->
                    convertToItemFeaturePO(f)).filter(f -> f != null).collect(Collectors.toList()));
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
            try {
                double[] coordinate = CoordinateUtil.bd09_To_Gcj02(Double.parseDouble(productItem.getLatitude()), Double.parseDouble(productItem.getLongitude()));
                productItemPO.setItemCoordinate(new Double[]{coordinate[1], coordinate[0]});
            } catch (Exception e) {
                log.error("转换经纬度失败，不影响主流程，productItemCode = {}", productItemPO.getCode(), e);
            }
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
        productItemPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        productItemPO.setOperator(Constants.SUPPLIER_CODE_YCF);
        productItemPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
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
        if(StringUtils.isBlank(itemFeature.getDetail())){
            return null;
        }
        ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
        itemFeaturePO.setDetail(itemFeature.getDetail());
        if(itemFeature.getType() != null
                && itemFeature.getType() == YcfConstants.POI_FEATURE_BOOK_NOTE
                && StringUtils.isNotBlank(itemFeature.getDetail())){
            itemFeaturePO.setDetail(format(itemFeature.getDetail()));
        }
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(itemFeaturePO.getDetail());
        // 没有中文就舍掉
        if (!m.find()) {
            return null;
        }
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

    public static Date parseDate(String date){
        if(StringUtils.isNotBlank(date)){
            return DateTimeUtil.parseDate(date);
        }
        return null;
    }

    /**
     * 删除购买须知里的"图文详情"，样式展示有问题
     * @param htmlStr
     * @return
     */
    private static String format(String htmlStr){
        Document document = Jsoup.parse(htmlStr);
        Elements elements0 = document.getElementsContainingOwnText("图文详情");
        if(elements0 == null){
            return htmlStr;
        }
        Elements elements1 = elements0.parents();
        if(elements1 == null){
            return htmlStr;
        }
        Elements elements2 = elements1.parents();
        if(elements2 == null){
            return htmlStr;
        }
        Element element = elements2.first();
        if(element == null){
            return htmlStr;
        }
        if(StringUtils.equals(element.tagName(), "div")){
            element.remove();
        }
        return document.toString();
    }

    /**
     * 转换成产品项目数据库对象
     * @param productItem
     * @return
     */
    public static ScenicSpotMPO convertToScenicSpotMPO(YcfProductItem productItem){
        ScenicSpotMPO scenicSpotMPO = new ScenicSpotMPO();
        scenicSpotMPO.setName(productItem.getPoiName());
        if(productItem.getLevel() != null){
            switch (productItem.getLevel()){
                case 11:
                    scenicSpotMPO.setLevel("A");
                    break;
                case 12:
                    scenicSpotMPO.setLevel("AA");
                    break;
                case 13:
                    scenicSpotMPO.setLevel("AAA");
                    break;
                case 14:
                    scenicSpotMPO.setLevel("AAAA");
                    break;
                case 15:
                    scenicSpotMPO.setLevel("AAAAA");
                    break;
            }
        }
        if(ListUtils.isNotEmpty(productItem.getTags())){
            scenicSpotMPO.setTheme(productItem.getTags().stream().collect(Collectors.joining(",")));
        }
        scenicSpotMPO.setCountry(productItem.getCountry());
        scenicSpotMPO.setProvince(productItem.getProvince());
        scenicSpotMPO.setCity(productItem.getCity());
        if (StringUtils.isBlank(productItem.getCity()) && StringUtils.isNotBlank(productItem.getAddress())){
            int strStartIndex = productItem.getAddress().indexOf("省");
            int strEndIndex = productItem.getAddress().indexOf("市");
            if (strStartIndex >= 0 && strEndIndex >= 0) {
                scenicSpotMPO.setCity(productItem.getAddress().substring(strStartIndex, strEndIndex).substring(1).trim());
            }
        }
        scenicSpotMPO.setAddress(productItem.getAddress());
        scenicSpotMPO.setPhone(productItem.getPhone());
        if(StringUtils.isNotBlank(productItem.getLongitude()) && StringUtils.isNotBlank(productItem.getLatitude())){
            double[] coordinateArr = CoordinateUtil.bd09_To_Gcj02(Double.parseDouble(productItem.getLatitude()), Double.parseDouble(productItem.getLongitude()));
            if(coordinateArr != null && coordinateArr.length == 2){
                Coordinate coordinate = new Coordinate();
                coordinate.setLongitude(coordinateArr[1]);
                coordinate.setLatitude(coordinateArr[0]);
                scenicSpotMPO.setCoordinate(coordinate);
            }
        }
        scenicSpotMPO.setDetailDesc(productItem.getDescription());
        List<String> images = Lists.newArrayList();
        if(ListUtils.isNotEmpty(productItem.getImageList())){
            images.addAll(productItem.getImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
        } else {
            if(ListUtils.isNotEmpty(productItem.getMainImageList())){
                images.addAll(productItem.getMainImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
            }
        }
        scenicSpotMPO.setImages(images);
        // 把购买须知加到产品动态说明，需要加字段
//        if(ListUtils.isNotEmpty(productItem.getCharacterrList())){
//            scenicSpotMPO.setNotices(productItem.getCharacterrList().stream().map(c -> {
//                Notice notice = new Notice();
//                if(c.getType() == 1){
//                    notice.setTitle("购买须知");
//                } else if(c.getType() == 2){
//                    notice.setTitle("交通指南");
//                } else if(c.getType() == 3){
//                    notice.setTitle("酒景图文");
//                }
//                notice.setContent(c.getDetail());
//                return notice;
//            }).collect(Collectors.toList()));
//        }
        return scenicSpotMPO;
    }

    public static HotelMPO convertToHotelMPO(YcfProductItem productItem){
        HotelMPO hotelMPO = new HotelMPO();
        hotelMPO.setName(productItem.getPoiName());
        if(productItem.getLevel() != null){
            switch (productItem.getLevel()){
                case 0:
                    hotelMPO.setStar("0");
                    break;
                case 1:
                    hotelMPO.setStar("1");
                    break;
                case 2:
                    hotelMPO.setStar("2");
                    break;
                case 3:
                    hotelMPO.setStar("3");
                    break;
                case 4:
                    hotelMPO.setStar("4");
                    break;
                case 5:
                    hotelMPO.setStar("5");
                    break;
            }
        }
        hotelMPO.setCountyName(productItem.getCountry());
        hotelMPO.setProvinceName(productItem.getProvince());
        hotelMPO.setCity(productItem.getCity());
        hotelMPO.setAddress(productItem.getAddress());
        hotelMPO.setTel(productItem.getPhone());
        hotelMPO.setDescription(productItem.getDescription());
        List<HotelPic> images = Lists.newArrayList();
        if(ListUtils.isNotEmpty(productItem.getImageList())){
            images.addAll(productItem.getImageList().stream().map(i -> {
                HotelPic hotelPic = new HotelPic();
                hotelPic.setName(i.getImageName());
                hotelPic.setUrl(i.getImageUrl());
                return hotelPic;
            }).collect(Collectors.toList()));
        } else {
            if(ListUtils.isNotEmpty(productItem.getMainImageList())){
                images.addAll(productItem.getMainImageList().stream().map(i -> {
                    HotelPic hotelPic = new HotelPic();
                    hotelPic.setName(i.getImageName());
                    hotelPic.setUrl(i.getImageUrl());
                    return hotelPic;
                }).collect(Collectors.toList()));
            }
        }
        hotelMPO.setHotelPics(images);
        return hotelMPO;
    }
}
