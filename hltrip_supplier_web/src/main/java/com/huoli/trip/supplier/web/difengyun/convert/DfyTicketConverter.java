package com.huoli.trip.supplier.web.difengyun.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.Coordinate;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.Notice;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotOpenTime;
import com.huoli.trip.common.util.*;
import com.huoli.trip.common.util.*;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyAdmissionVoucher;
import com.huoli.trip.supplier.self.difengyun.vo.DfyPriceCalendar;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.lvmama.vo.LmmScenic;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/9<br>
 */
@Slf4j
public class DfyTicketConverter {

    public static ProductItemPO convertToProductItemPO(DfyScenicDetail scenicDetail){
        ProductItemPO productItemPO = new ProductItemPO();
        productItemPO.setItemType(ProductType.SCENIC_TICKET.getCode());
        productItemPO.setStatus(Constants.PRODUCT_STATUS_VALID);
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
        productItemPO.setSupplierItemId(scenicDetail.getScenicId());
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), scenicDetail.getScenicId()));
        productItemPO.setName(scenicDetail.getScenicName());
        List<ItemFeaturePO> featurePOs = Lists.newArrayList();
        if(StringUtils.isNotBlank(scenicDetail.getBookNotice())){
            try {
                ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
                JSONArray jsonArray = JSON.parseArray(scenicDetail.getBookNotice());
                StringBuilder sb = new StringBuilder();
                for (Object o : jsonArray) {
                    JSONObject obj = (JSONObject) o;
                    sb.append(obj.get("name")).append("<br>")
                            .append(obj.get("value")).append("<br>");
                }
                itemFeaturePO.setDetail(sb.toString());
                itemFeaturePO.setType(YcfConstants.POI_FEATURE_BOOK_NOTE);
                featurePOs.add(itemFeaturePO);
            } catch (Exception e){
                log.error("笛风云转换特色列表（购买须知）异常，不影响正常流程。。", e);
            }
        }
        productItemPO.setCity(scenicDetail.getCityName());
        productItemPO.setDesCity(scenicDetail.getCityName());
        productItemPO.setOriCity(scenicDetail.getCityName());
        productItemPO.setProvince(scenicDetail.getProvinceName());
        if(StringUtils.isNotBlank(scenicDetail.getBlocation())){
            try {
                String[] baiduArr = scenicDetail.getBlocation().split(",");
                if(baiduArr.length == 2){
                    double[] coordinateArr = CoordinateUtil.bd09_To_Gcj02(Double.valueOf(baiduArr[0]), Double.valueOf(baiduArr[1]));
                    if(coordinateArr != null && coordinateArr.length == 2){
                        Double[] coordinate = new Double[]{coordinateArr[1], coordinateArr[0]};
                        productItemPO.setItemCoordinate(coordinate);
                    }
                }
            } catch (Exception e) {
                log.error("转换坐标失败，不影响继续执行，", e);
            }
        } else if(StringUtils.isNotBlank(scenicDetail.getGlocation())){
            try {
                String[] gaodeArr = scenicDetail.getGlocation().split(",");
                if(gaodeArr.length == 2){
                    Double[] coordinate = new Double[]{Double.valueOf(gaodeArr[1]), Double.valueOf(gaodeArr[0])};
                    productItemPO.setItemCoordinate(coordinate);
                }
            } catch (Exception e) {
                log.error("转换坐标失败，不影响继续执行，", e);
            }
        }
        productItemPO.setBusinessHours(scenicDetail.getOpenTime());
        productItemPO.setAddress(scenicDetail.getScenicAddress());
        if(StringUtils.isNotBlank(scenicDetail.getDefaultPic())){
            ImageBasePO imageBasePO = new ImageBasePO();
            imageBasePO.setUrl(scenicDetail.getDefaultPic());
            // 列表图不用说明
            productItemPO.setMainImages(Lists.newArrayList(imageBasePO));
            // 详情图需要说明
            productItemPO.setImages(Lists.newArrayList(imageBasePO));
        }
        // 放到features就可以了。不用重复存
//        if(StringUtils.isNotBlank(scenicDetail.getScenicDescription())){
//            ImageBasePO imageBasePO = new ImageBasePO();
//            imageBasePO.setUrl(scenicDetail.getDefaultPic());
//            imageBasePO.setDesc(scenicDetail.getScenicDescription());
//            productItemPO.setImageDetails(Lists.newArrayList(imageBasePO));
//        }
        // 现在展示图文详情了，所以还放回到这里
        if(StringUtils.isNotBlank(scenicDetail.getScenicDescription())){
            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
            itemFeaturePO.setDetail(scenicDetail.getScenicDescription());
            itemFeaturePO.setType(YcfConstants.POI_FEATURE_DETAIL);
            featurePOs.add(itemFeaturePO);
        }
        // 这个不是按富文本处理的
//        productItemPO.setDescription(scenicDetail.getScenicDescription());
        productItemPO.setAppMainTitle(productItemPO.getName());
        productItemPO.setAppSubTitle(scenicDetail.getRecommend());
        if(StringUtils.isNotBlank(scenicDetail.getTrafficBus())){
            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
            itemFeaturePO.setDetail(scenicDetail.getTrafficBus());
            itemFeaturePO.setType(YcfConstants.POI_FEATURE_TRAFFIC_NOTE);
            featurePOs.add(itemFeaturePO);
        }
        if(ListUtils.isNotEmpty(featurePOs)){
            productItemPO.setFeatures(featurePOs);
        }
        return productItemPO;
    }

    public static ProductPO convertToProductPO(DfyTicketDetail ticketDetail){
        ProductPO productPO = new ProductPO();
        // 默认条件退
        productPO.setRefundType(3);
        productPO.setStatus(1);
        productPO.setProductType(ProductType.SCENIC_TICKET.getCode());
        productPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
        productPO.setSupplierName(Constants.SUPPLIER_NAME_DFY);
        productPO.setSupplierProductId(ticketDetail.getProductId());
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), ticketDetail.getProductId()));
        productPO.setName(ticketDetail.getProductName());
        productPO.setPrice(StringUtils.isBlank(ticketDetail.getWebPrice()) ? null : new BigDecimal(ticketDetail.getWebPrice()));
        productPO.setSalePrice(StringUtils.isBlank(ticketDetail.getSalePrice()) ? null : new BigDecimal(ticketDetail.getSalePrice()));
        productPO.setBookDesc(ticketDetail.getBookNotice());
        productPO.setBuyMin(ticketDetail.getLimitNumLow());
        productPO.setBuyMax(ticketDetail.getLimitNumHigh());
        productPO.setRemark(ticketDetail.getInfo());
        productPO.setRefundDesc(ticketDetail.getMpLossInfo());
        productPO.setValidTimeDesc(ticketDetail.getIndate());
        productPO.setAdvanceDay(ticketDetail.getAdvanceDay());
        productPO.setAdvanceHour(ticketDetail.getAdvanceHour());
        TicketPO ticketPO = new TicketPO();
        if(ticketDetail.getDrawType() != null){
            switch (ticketDetail.getDrawType()){
                case DfyConstants.DRAW_TYPE_PAPER:
                    ticketPO.setObtainTicketMode("实体票");
                    break;
                case DfyConstants.DRAW_TYPE_ELECT:
                    ticketPO.setObtainTicketMode("预付电子票");
                    break;
                default:
                    break;
            }
        }
        ticketPO.setDrawAddress(ticketDetail.getDrawAddress());
        Integer type = null;
        if(StringUtils.isNotBlank(ticketDetail.getMpType())){
            switch (Integer.parseInt(ticketDetail.getMpType())){
                case DfyConstants.TICKET_TYPE_0:
                    type = TicketType.TICKET_TYPE_19.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_1:
                    type = TicketType.TICKET_TYPE_2.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_2:
                    type = TicketType.TICKET_TYPE_4.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_3:
                    type = TicketType.TICKET_TYPE_9.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_4:
                    type = TicketType.TICKET_TYPE_7.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_5:
                    type = TicketType.TICKET_TYPE_8.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_6:
                    type = TicketType.TICKET_TYPE_14.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_7:
                    type = TicketType.TICKET_TYPE_17.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_8:
                    type = TicketType.TICKET_TYPE_16.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_9:
                    type = TicketType.TICKET_TYPE_20.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_10:
                    type = TicketType.TICKET_TYPE_21.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_11:
                    type = TicketType.TICKET_TYPE_23.getCode();
                    break;
                case DfyConstants.TICKET_TYPE_12:
                    type = TicketType.TICKET_TYPE_22.getCode();
                    break;
                default:
                    break;
            }
        }
        ticketPO.setTicketType(type);
        DfyAdmissionVoucher dfyAdmissionVoucher = ticketDetail.getAdmissionVoucher();
        ticketPO.setAdmissionVoucherCode(dfyAdmissionVoucher.getAdmissionVoucherCode());
        ticketPO.setAdmissionVoucherDesc(dfyAdmissionVoucher.getAdmissionVoucherDesc());
        TicketInfoPO ticketInfoPO = new TicketInfoPO();
        ticketInfoPO.setBaseNum(1);
        ticketInfoPO.setSupplierItemId(ticketDetail.getScenicId());
        ticketInfoPO.setTitle(ticketDetail.getProductName());
        ticketInfoPO.setSupplierResourceId(ticketDetail.getResourceId());
        ticketInfoPO.setTicketType(type);
        ticketPO.setTickets(Lists.newArrayList(ticketInfoPO));
        productPO.setTicket(ticketPO);
        if(ticketDetail.getCustInfoLimit() != null){
            BookRulePO contactPhone = convertBookRulePO("0", false, null, 1, dfyAdmissionVoucher.getAdmissionVoucherCode());
            BookRulePO contactPhoneAndID = convertBookRulePO("0", true, ticketDetail.getCertificateType(), 1, dfyAdmissionVoucher.getAdmissionVoucherCode());
            BookRulePO passengerPhone = convertBookRulePO("1", false, null, 0, dfyAdmissionVoucher.getAdmissionVoucherCode());
            BookRulePO passengerPhoneAndID = convertBookRulePO("1", true, ticketDetail.getCertificateType(), 0, dfyAdmissionVoucher.getAdmissionVoucherCode());
            List<BookRulePO> bookRules = Lists.newArrayList();
            switch (ticketDetail.getCustInfoLimit()){
                case DfyConstants.BOOK_RULE_1:
                    bookRules.add(contactPhone);
                    break;
                case DfyConstants.BOOK_RULE_2:
                    bookRules.add(contactPhone);
                    bookRules.add(passengerPhone);
                    break;
                case DfyConstants.BOOK_RULE_3:
                    bookRules.add(contactPhone);
                    bookRules.add(passengerPhoneAndID);
                    break;
                case DfyConstants.BOOK_RULE_4:
                    bookRules.add(contactPhoneAndID);
                    break;
                case DfyConstants.BOOK_RULE_6:
                    bookRules.add(contactPhoneAndID);
                    bookRules.add(passengerPhoneAndID);
                    break;
                case DfyConstants.BOOK_RULE_7:
                    bookRules.add(contactPhoneAndID);
                    bookRules.add(passengerPhone);
                    break;
                default:
                    break;
            }
            if(ListUtils.isNotEmpty(bookRules)){
                productPO.setBookRules(bookRules);
            }
        }
        return productPO;
    }

    public static PricePO convertToPricePO(List<DfyPriceCalendar> dfyPriceCalendars){
        PricePO pricePO = new PricePO();
        if(ListUtils.isNotEmpty(dfyPriceCalendars)){
            List<PriceInfoPO> priceInfoPOs = dfyPriceCalendars.stream().map(p -> {
                PriceInfoPO priceInfoPO = new PriceInfoPO();
                // 笛风云没有库存，默认99
                priceInfoPO.setStock(99);
                if(StringUtils.isBlank(p.getDepartDate())){
                    return null;
                }
                priceInfoPO.setSaleDate(MongoDateUtils.handleTimezoneInput(DateTimeUtil.parseDate(p.getDepartDate())));
                if(StringUtils.isNotBlank(p.getSalePrice())){
                    priceInfoPO.setSalePrice(new BigDecimal(p.getSalePrice()));
                    priceInfoPO.setSettlePrice(priceInfoPO.getSalePrice());
                }
                return priceInfoPO;
            }).filter(p -> p.getSaleDate() != null).collect(Collectors.toList());
            if(ListUtils.isNotEmpty(priceInfoPOs)){
                pricePO.setPriceInfos(priceInfoPOs);
            }
        }
        return pricePO;
    }

    private static BookRulePO convertBookRulePO(String ruleType, boolean credential, String credentialList, int limit, String admissionVoucherCode){
        BookRulePO bookRulePO = new BookRulePO();
        bookRulePO.setRuleType(ruleType);
        bookRulePO.setCnName(true);
        bookRulePO.setPhone(true);
        bookRulePO.setCredential(credential);
        if(StringUtils.isNotBlank(credentialList)){
            List<Integer> creds = Lists.newArrayList(credentialList.split(",")).stream().map(c -> {
                switch (Integer.parseInt(c)){
                    case DfyConstants.CRED_TYPE_ID:
                        return Certificate.ID_CARD.getCode();
                    case DfyConstants.CRED_TYPE_PP:
                        return Certificate.PASSPORT.getCode();
                    case DfyConstants.CRED_TYPE_OF:
                        return Certificate.OFFICER.getCode();
                    case DfyConstants.CRED_TYPE_HK:
                        return Certificate.HKM_PASS.getCode();
                    case DfyConstants.CRED_TYPE_TW:
                        return Certificate.TW_CARD.getCode();
                    default:
                        // 其它类型直接舍弃（笛风云建议这样操作）
                        return Integer.MIN_VALUE;
                }
            }).distinct().filter(c -> c.intValue() != Integer.MIN_VALUE).collect(Collectors.toList());
            bookRulePO.setCredentials(creds);
        } else {
            // 如果空的只支持身份证
            bookRulePO.setCredentials(Lists.newArrayList(Certificate.ID_CARD.getCode()));
        }
        bookRulePO.setEmail(false);
        // 需要邮箱的入园方式，只联系人
        if("0".equals(ruleType) && Arrays.asList("205", "302").contains(admissionVoucherCode)){
            bookRulePO.setEmail(true);
        }
        bookRulePO.setEnName(false);
        bookRulePO.setPeopleLimit(limit);
//        bookRulePO.setPeopleNum(1);
        return bookRulePO;
    }

    public static ScenicSpotMPO convertToScenicSpotMPO(DfyScenicDetail scenicDetail){
        ScenicSpotMPO scenicSpotMPO = new ScenicSpotMPO();
        // 默认待审核
        scenicSpotMPO.setStatus(0);
        scenicSpotMPO.setAddress(scenicDetail.getScenicAddress());
        scenicSpotMPO.setCity(scenicDetail.getCityName());
        if(StringUtils.isNotBlank(scenicDetail.getDefaultPic())){
            scenicSpotMPO.setImages(Lists.newArrayList(scenicDetail.getDefaultPic()));
        }

        scenicSpotMPO.setName(scenicDetail.getScenicName());
        scenicSpotMPO.setDetailDesc(scenicDetail.getScenicDescription());
        scenicSpotMPO.setCoordinate(convertToCoordinate(scenicDetail.getBlocation(), scenicDetail.getGlocation()));
        scenicSpotMPO.setProvince(scenicDetail.getProvinceName());
        if(StringUtils.isNotBlank(scenicDetail.getOpenTime())){
            ScenicSpotOpenTime scenicSpotOpenTime = new ScenicSpotOpenTime();
            scenicSpotOpenTime.setTimeDesc(scenicDetail.getOpenTime());
            scenicSpotMPO.setScenicSpotOpenTimes(Lists.newArrayList(scenicSpotOpenTime));
        }
        Notice notice = new Notice();
        notice.setContent(scenicDetail.getBookNotice());
        notice.setContent("预定须知");
//        scenicSpotMPO.setNotices(Lists.newArrayList(notice));
        scenicSpotMPO.setTraffic(scenicDetail.getTrafficBus());
        return scenicSpotMPO;
    }

    public static Coordinate convertToCoordinate(String baidu, String google){
        Coordinate coordinate = null;
        if(StringUtils.isNotBlank(baidu)){
            try {
                String[] baiduArr = baidu.split(",");
                if(baiduArr.length == 2){
                    double[] coordinateArr = CoordinateUtil.bd09_To_Gcj02(Double.valueOf(baiduArr[0]), Double.valueOf(baiduArr[1]));
                    if(coordinateArr != null && coordinateArr.length == 2){
                        coordinate = new Coordinate();
                        coordinate.setLongitude(coordinateArr[1]);
                        coordinate.setLatitude(coordinateArr[0]);
                    }
                }
            } catch (Exception e) {
                log.error("转换坐标失败，不影响继续执行，", e);
            }
        } else if(StringUtils.isNotBlank(google)){
            try {
                String[] googleArr = google.split(",");
                if(googleArr.length == 2){
                    double[] coordinateArr = CoordinateUtil.bd09_To_Gcj02(Double.valueOf(googleArr[0]), Double.valueOf(googleArr[1]));
                    if(coordinateArr != null && coordinateArr.length == 2){
                        coordinate = new Coordinate();
                        coordinate.setLongitude(coordinateArr[1]);
                        coordinate.setLatitude(coordinateArr[0]);
                    }
                }
            } catch (Exception e) {
                log.error("转换坐标失败，不影响继续执行，", e);
            }
        }
        return coordinate;
    }
}
