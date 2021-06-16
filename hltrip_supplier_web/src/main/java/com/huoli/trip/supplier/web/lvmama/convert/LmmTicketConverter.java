package com.huoli.trip.supplier.web.lvmama.convert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.Coordinate;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotOpenTime;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.CoordinateUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.lvmama.vo.LmmGoods;
import com.huoli.trip.supplier.self.lvmama.vo.LmmOpenTime;
import com.huoli.trip.supplier.self.lvmama.vo.LmmProduct;
import com.huoli.trip.supplier.self.lvmama.vo.LmmScenic;
import org.apache.commons.lang3.StringUtils;
import sun.awt.motif.X11CNS11643;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
 */
public class LmmTicketConverter {

    public static ProductItemPO convertToProductItemPO(LmmScenic lmmScenic){
        ProductItemPO productItemPO = new ProductItemPO();
        productItemPO.setItemType(ProductType.SCENIC_TICKET.getCode());
        productItemPO.setStatus(Constants.PRODUCT_STATUS_VALID);
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_LMM_TICKET);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, lmmScenic.getScenicId().toString()));
        productItemPO.setSupplierItemId(lmmScenic.getScenicId().toString());
        productItemPO.setName(lmmScenic.getScenicName());
        if(StringUtils.isNotBlank(lmmScenic.getPlaceInfo())){
            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
            itemFeaturePO.setType(3);
            itemFeaturePO.setDetail(lmmScenic.getPlaceInfo());
        }
        if(StringUtils.isNotBlank(lmmScenic.getPlaceAct())){
            productItemPO.setTopic(Arrays.stream(lmmScenic.getPlaceAct().split(",")).map(s -> {
                BaseCode baseCode = new BaseCode();
                // 没有code,只能先赋个name
                baseCode.setName(s);
                return baseCode;
            }).collect(Collectors.toList()));
        }

        if(StringUtils.isNotBlank(lmmScenic.getPlaceLevel())){
            Integer level = null;
            switch (lmmScenic.getPlaceLevel()){
                case "0":
                    level = 0;
                    break;
                case "1":
                    level = 11;
                    break;
                case "2":
                    level = 12;
                    break;
                case "3":
                    level = 13;
                    break;
                case "4":
                    level = 14;
                    break;
                case "5":
                    level = 15;
                    break;
            }
            productItemPO.setLevel(level);
        }
        if(ListUtils.isNotEmpty(lmmScenic.getPlaceImage())){
            List<ImageBasePO> imageBasePOs = lmmScenic.getPlaceImage().stream().map(url ->
                    convertToImageBasePO(url)).collect(Collectors.toList());
            productItemPO.setImages(imageBasePOs);
            productItemPO.setMainImages(imageBasePOs.subList(0, 1));
        }

        productItemPO.setAddress(lmmScenic.getPlaceToAddr());
        if(ListUtils.isNotEmpty(lmmScenic.getOpenTimes())){
            StringBuffer sb = new StringBuffer();
            for (LmmOpenTime openTime : lmmScenic.getOpenTimes()) {
                sb.append(openTime.getOpenTimeInfo()).append(" : ")
                        .append(openTime.getSightStart()).append("-")
                        .append(openTime.getSightEnd()).append("<br>");
            }
            productItemPO.setBusinessHours(sb.toString());
        }
        productItemPO.setTown(lmmScenic.getPlaceTown());
        productItemPO.setDistrict(lmmScenic.getPlaceXian());
        productItemPO.setCity(lmmScenic.getPlaceCity());
        productItemPO.setDesCity(lmmScenic.getPlaceCity());
        productItemPO.setOriCity(lmmScenic.getPlaceCity());
        productItemPO.setProvince(lmmScenic.getPlaceProvince());
        productItemPO.setCountry(lmmScenic.getPlaceCountry());
        if(lmmScenic.getBaiduData() != null){
            try {
                double[] coordinateArr = CoordinateUtil.bd09_To_Gcj02(lmmScenic.getBaiduData().getLatitude(), lmmScenic.getBaiduData().getLongitude());
                if(coordinateArr != null && coordinateArr.length == 2){
                    Double[] coordinate = new Double[]{coordinateArr[1], coordinateArr[0]};
                    productItemPO.setItemCoordinate(coordinate);
                }
            } catch (Exception e) {
            }
        } else if(lmmScenic.getGoogleData() != null){
            try {
                double[] coordinateArr = CoordinateUtil.gps84_To_Gcj02(lmmScenic.getGoogleData().getLatitude(), lmmScenic.getGoogleData().getLongitude());
                if(coordinateArr != null && coordinateArr.length == 2){
                    Double[] coordinate = new Double[]{coordinateArr[1], coordinateArr[0]};
                    productItemPO.setItemCoordinate(coordinate);
                }
            } catch (Exception e) {
            }
        }
        return productItemPO;
    }

    private static ImageBasePO convertToImageBasePO(String url){
        ImageBasePO imageBasePO = new ImageBasePO();
        imageBasePO.setUrl(url);
        return imageBasePO;
    }

    public static ProductPO convertToProductPO(LmmProduct lmmProduct, LmmGoods goods){
        ProductPO productPO = new ProductPO();
        Map<String, String> extend = Maps.newHashMap();
        extend.put("productId", lmmProduct.getProductId());
        productPO.setExtendParams(extend);
        productPO.setName(String.format("%s %s", lmmProduct.getProductName(), goods.getGoodsName()));
        productPO.setCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, goods.getGoodsId()));
        productPO.setSupplierId(Constants.SUPPLIER_CODE_LMM_TICKET);
        productPO.setSupplierProductId(goods.getGoodsId());
        productPO.setProductType(ProductType.SCENIC_TICKET.getCode());
        if(StringUtils.equals(goods.getStatus(), "true")
                && StringUtils.equals(lmmProduct.getProductStatus(), "true")){
            productPO.setStatus(Constants.PRODUCT_STATUS_VALID);
        } else {
            productPO.setStatus(Constants.PRODUCT_STATUS_INVALID);
        }
        productPO.setIncludeDesc(goods.getCostInclude());
        productPO.setExcludeDesc(goods.getCostNoinclude());
        productPO.setRefundDesc(goods.getRefundRuleNotice());
        List<DescriptionPO> descriptionPOs = Lists.newArrayList();
        if(StringUtils.isNotBlank(goods.getImportantNotice())){
            DescriptionPO impDesc = new DescriptionPO();
            impDesc.setTitle("重要提示");
            impDesc.setContent(goods.getImportantNotice());
            descriptionPOs.add(impDesc);
        }
        if(lmmProduct.getBookingInfo() != null){
            productPO.setRemark(lmmProduct.getBookingInfo().getExplanation());
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getExplanation())){
                DescriptionPO freeDesc = new DescriptionPO();
                freeDesc.setTitle("免票政策");
                freeDesc.setContent(lmmProduct.getBookingInfo().getFreePolicy());
                descriptionPOs.add(freeDesc);
            }
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getOfferCrowd())){
                DescriptionPO offerDesc = new DescriptionPO();
                offerDesc.setTitle("优惠人群");
                offerDesc.setContent(lmmProduct.getBookingInfo().getOfferCrowd());
                descriptionPOs.add(offerDesc);
            }
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getOldPeople())){
                DescriptionPO oldDesc = new DescriptionPO();
                oldDesc.setTitle("关于老人");
                oldDesc.setContent(lmmProduct.getBookingInfo().getOldPeople());
                descriptionPOs.add(oldDesc);
            }
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getAge())){
                DescriptionPO ageDesc = new DescriptionPO();
                ageDesc.setTitle("关于年龄");
                ageDesc.setContent(lmmProduct.getBookingInfo().getAge());
                descriptionPOs.add(ageDesc);
            }
        }
        productPO.setBookDescList(descriptionPOs);
        productPO.setDescription(lmmProduct.getIntrodution());
        if(ListUtils.isNotEmpty(lmmProduct.getPlayAttractions())){
            List<ImageBasePO> descs = lmmProduct.getPlayAttractions().stream().map(a -> {
                ImageBasePO desc = new ImageBasePO();
                desc.setLabel(a.getPlayName());
                desc.setDesc(a.getPlayInfo());
                if(ListUtils.isNotEmpty(a.getPlayImages())){
                    desc.setUrl(a.getPlayImages().get(0));
                }
                return desc;
            }).collect(Collectors.toList());
            productPO.setDescriptions(descs);
        }
        if(ListUtils.isNotEmpty(lmmProduct.getImages())){
            List<ImageBasePO> images = lmmProduct.getImages().stream().map(i -> {
                ImageBasePO imageBasePO = new ImageBasePO();
                imageBasePO.setUrl(i);
                return imageBasePO;
            }).collect(Collectors.toList());
            productPO.setImages(images);
        }
        productPO.setBuyMax(goods.getMaximum());
        productPO.setBuyMin(goods.getMinimum());
        if(ListUtils.isNotEmpty(goods.getRules())){
            if(goods.getRules().stream().anyMatch(r -> r.isChange())){
                productPO.setRefundType(1);
                if(goods.getRules().stream().anyMatch(r -> !r.isChange())){
                    productPO.setRefundType(3);
                }
            } else {
                productPO.setRefundType(2);
            }
        }
        if(goods.getNotice() != null){
            TicketInfoPO ticketInfoPO = new TicketInfoPO();
            ticketInfoPO.setObtainTicketTime(goods.getNotice().getGetTicketTime());
            ticketInfoPO.setDrawAddress(goods.getNotice().getGetTicketPlace());
            ticketInfoPO.setAdmissionVoucherDesc(goods.getNotice().getWays());
            if(goods.getNotice().getEnterLimit() != null){
                ticketInfoPO.setAdmissionTime(goods.getNotice().getEnterLimit().getLimitTime());
            }
            DescriptionPO descriptionPO = new DescriptionPO();
            descriptionPO.setTitle("入园须知");
            StringBuffer sb = new StringBuffer();
            sb.append("取票时间：").append(ticketInfoPO.getObtainTicketTime()).append("<br>")
                    .append("取票地址：").append(ticketInfoPO.getDrawAddress()).append("<br>")
                    .append("取票方式：").append(ticketInfoPO.getAdmissionVoucherDesc()).append("<br>")
                    .append("入园时间：").append(ticketInfoPO.getAdmissionTime()).append("<br>");
            descriptionPO.setContent(sb.toString());
            if(ListUtils.isNotEmpty(productPO.getBookDescList())){
                productPO.getBookDescList().add(descriptionPO);
            } else {
                productPO.setBookDescList(Lists.newArrayList(descriptionPO));
            }
            Integer ticketType = null;
            switch (goods.getTicketType()){
                case "PARENTAGE":
                    ticketType = TicketType.TICKET_TYPE_3.getCode();
                    break;
                case "FAMILY":
                    ticketType = TicketType.TICKET_TYPE_4.getCode();
                    break;
                case "LOVER":
                    ticketType = TicketType.TICKET_TYPE_5.getCode();
                    break;
                case "COUPE":
                    ticketType = TicketType.TICKET_TYPE_6.getCode();
                    break;
                case "ADULT":
                case "MAN":
                case "WOMAN":
                    ticketType = TicketType.TICKET_TYPE_2.getCode();
                    break;
                case "CHILDREN":
                    ticketType = TicketType.TICKET_TYPE_7.getCode();
                    break;
                case "OLDMAN":
                    ticketType = TicketType.TICKET_TYPE_8.getCode();
                    break;
                case "STUDENT":
                    ticketType = TicketType.TICKET_TYPE_9.getCode();
                    break;
                case "ACTIVITY":
                    ticketType = TicketType.TICKET_TYPE_19.getCode();
                    break;
                case "SOLDIER":
                    ticketType = TicketType.TICKET_TYPE_10.getCode();
                    break;
                case "TEACHER":
                    ticketType = TicketType.TICKET_TYPE_11.getCode();
                    break;
                case "DISABILITY":
                    ticketType = TicketType.TICKET_TYPE_12.getCode();
                    break;
                case "GROUP":
                    ticketType = TicketType.TICKET_TYPE_13.getCode();
                    break;
                case "FREE":
                    ticketType = TicketType.TICKET_TYPE_1.getCode();
                    break;
            }
            ticketInfoPO.setTicketType(ticketType);
            ticketInfoPO.setBaseNum(1);
            TicketPO ticketPO = new TicketPO();
            ticketPO.setTickets(Lists.newArrayList(ticketInfoPO));
            productPO.setTicket(ticketPO);
        }
        // todo 入园地点、票种说明、通关时间、限购说明 现在没有
        BookRulePO booker = new BookRulePO();
        booker.setCredential(false);
        booker.setEmail(false);
        booker.setPhone(false);
        booker.setEnName(false);
        booker.setCnName(false);
        booker.setRuleType("0");
        if(goods.getBooker() != null){
            if(goods.getBooker().isEmail()){
                booker.setEmail(true);
            }
            if(goods.getBooker().isMobile()){
                booker.setPhone(true);
            }
            if(goods.getBooker().isName()){
                booker.setCnName(true);
            }
        }
        BookRulePO passenger = new BookRulePO();
        passenger.setCredential(false);
        passenger.setEmail(false);
        passenger.setPhone(false);
        passenger.setEnName(false);
        passenger.setCnName(false);
        passenger.setRuleType("1");
        passenger.setPeopleLimit(2);
        if(goods.getTraveller() != null){
            // 按最大范围取
            if(StringUtils.equals(goods.getTraveller().getName(), "TRAV_NUM_ALL")
                    || StringUtils.equals(goods.getTraveller().getMobile(), "TRAV_NUM_ALL")
                    || StringUtils.equals(goods.getTraveller().getEmail(), "TRAV_NUM_ALL")
                    || StringUtils.equals(goods.getTraveller().getCredentials(), "TRAV_NUM_ALL")
                    || StringUtils.equals(goods.getTraveller().getEnName(), "TRAV_NUM_ALL")){
                passenger.setPeopleLimit(0);
            } else if(StringUtils.equals(goods.getTraveller().getName(), "TRAV_NUM_ONE")
                    || StringUtils.equals(goods.getTraveller().getMobile(), "TRAV_NUM_ONE")
                    || StringUtils.equals(goods.getTraveller().getEmail(), "TRAV_NUM_ONE")
                    || StringUtils.equals(goods.getTraveller().getCredentials(), "TRAV_NUM_ONE")
                    || StringUtils.equals(goods.getTraveller().getEnName(), "TRAV_NUM_ONE")){
                passenger.setPeopleLimit(1);
            }
            if(!StringUtils.equals(goods.getTraveller().getName(), "TRAV_NUM_NO")){
                passenger.setCnName(true);
            }
            if(!StringUtils.equals(goods.getTraveller().getEnName(), "TRAV_NUM_NO")){
                passenger.setEnName(true);
            }
            if(!StringUtils.equals(goods.getTraveller().getEmail(), "TRAV_NUM_NO")){
                passenger.setEmail(true);
            }
            if(!StringUtils.equals(goods.getTraveller().getMobile(), "TRAV_NUM_NO")){
                passenger.setPhone(true);
            }
            if(!StringUtils.equals(goods.getTraveller().getCredentials(), "TRAV_NUM_NO")){
                passenger.setCredential(true);
            }

            if(StringUtils.isNotBlank(goods.getTraveller().getCredentialsType())){
                passenger.setCredentials(Arrays.asList(goods.getTraveller().getCredentialsType().split("-")).stream().map(t -> {
                    switch (t) {
                        case "ID_CARD":
                          return Certificate.ID_CARD.getCode();
                        case "HUZHAO":
                            return Certificate.PASSPORT.getCode();
                        case "GANGAO":
                            return Certificate.HKM_PASS.getCode();
                        case "TAIBAO":
                            return Certificate.TW_PASS.getCode();
                        case "TAIBAOZHENG":
                            return Certificate.TW_CARD.getCode();
                        case "CHUSHENGZHENGMING":
                        case "HUKOUBO":
                            return Certificate.OTHER.getCode();
                        case "SHIBING":
                            return Certificate.SOLDIERS.getCode();
                        case "JUNGUAN":
                            return Certificate.OFFICER.getCode();
                        case "HUIXIANG":
                            return Certificate.HOME_CARD.getCode();
                        default:
                            return Certificate.ID_CARD.getCode();
                    }
                }).collect(Collectors.toList()));
            }
        }
        List<BookRulePO> bookRulePOs = Lists.newArrayList(booker, passenger);
        productPO.setBookRules(bookRulePOs);
        return productPO;
    }


    // ==================================↓↓↓新结构↓↓↓===============================


    public static ScenicSpotMPO convertToScenicSpotMPO(LmmScenic lmmScenic){
        ScenicSpotMPO scenicSpotMPO = new ScenicSpotMPO();
        scenicSpotMPO.setAddress(lmmScenic.getPlaceToAddr());
        scenicSpotMPO.setCity(lmmScenic.getPlaceCity());
        scenicSpotMPO.setImages(lmmScenic.getPlaceImage());
        scenicSpotMPO.setName(lmmScenic.getScenicName());
        scenicSpotMPO.setDetailDesc(lmmScenic.getPlaceInfo());
        if(lmmScenic.getBaiduData() != null){
            scenicSpotMPO.setCoordinate(convertToCoordinate(lmmScenic.getBaiduData(), "bd"));
        } else if (lmmScenic.getGoogleData() != null){
            scenicSpotMPO.setCoordinate(convertToCoordinate(lmmScenic.getGoogleData(), "gg"));
        }
        scenicSpotMPO.setCountry(lmmScenic.getPlaceCountry());
        scenicSpotMPO.setProvince(lmmScenic.getPlaceProvince());
        scenicSpotMPO.setLevel(lmmScenic.getPlaceLevel());
        scenicSpotMPO.setTheme(lmmScenic.getPlaceAct());
        if(ListUtils.isNotEmpty(lmmScenic.getOpenTimes())){
            List<ScenicSpotOpenTime> openTimes = lmmScenic.getOpenTimes().stream().map(o -> convertToScenicSpotOpenTime(o)).collect(Collectors.toList());
            scenicSpotMPO.setScenicSpotOpenTimes(openTimes);
        }
        return scenicSpotMPO;
    }

    public static Coordinate convertToCoordinate(LmmScenic.Coordinate lmmCoordinate, String map){
        if(lmmCoordinate != null){
            try {
                Coordinate coordinate = new Coordinate();
                double[] coordinateArr = null;
                if(StringUtils.equals("bd", map)){
                    coordinateArr = CoordinateUtil.bd09_To_Gcj02(lmmCoordinate.getLatitude(), lmmCoordinate.getLongitude());
                } else if(StringUtils.equals("gg", map)){
                    coordinateArr = CoordinateUtil.gps84_To_Gcj02(lmmCoordinate.getLatitude(), lmmCoordinate.getLongitude());
                }
                if(coordinateArr != null && coordinateArr.length == 2){
                    coordinate.setLatitude(coordinateArr[0]);
                    coordinate.setLongitude(coordinateArr[1]);
                    return coordinate;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static ScenicSpotOpenTime convertToScenicSpotOpenTime(LmmOpenTime lmmOpenTime){
        ScenicSpotOpenTime scenicSpotOpenTime = new ScenicSpotOpenTime();
        scenicSpotOpenTime.setTimeDesc(lmmOpenTime.getOpenTimeInfo());
        scenicSpotOpenTime.setDateDesc(String.format("%s-%s", lmmOpenTime.getSightStart(), lmmOpenTime.getSightEnd()));
        return scenicSpotOpenTime;
    }

}
