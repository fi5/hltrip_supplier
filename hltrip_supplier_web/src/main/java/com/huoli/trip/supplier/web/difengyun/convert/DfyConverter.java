package com.huoli.trip.supplier.web.difengyun.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyAdmissionVoucher;
import com.huoli.trip.supplier.self.difengyun.vo.DfyPriceCalendar;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
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
public class DfyConverter {

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
            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
            itemFeaturePO.setDetail(scenicDetail.getBookNotice());
            itemFeaturePO.setType(YcfConstants.POI_FEATURE_BOOK_NOTE);
            featurePOs.add(itemFeaturePO);
        }
        productItemPO.setCity(scenicDetail.getCityName());
        productItemPO.setDesCity(scenicDetail.getCityName());
        productItemPO.setOriCity(scenicDetail.getCityName());
        productItemPO.setProvince(scenicDetail.getProvinceName());
        if(StringUtils.isNotBlank(scenicDetail.getBlocation())){
            productItemPO.setItemCoordinate(Arrays.asList(scenicDetail.getBlocation().split(",")).stream().map(l ->
                    Double.valueOf(l)).collect(Collectors.toList()).toArray(new Double[]{}));
        }
        productItemPO.setBusinessHours(scenicDetail.getOpenTime());
        productItemPO.setAddress(scenicDetail.getScenicAddress());
        if(StringUtils.isNotBlank(scenicDetail.getDefaultPic())){
            ImageBasePO imageBasePO = new ImageBasePO();
            imageBasePO.setUrl(scenicDetail.getDefaultPic());
            productItemPO.setMainImages(Lists.newArrayList(imageBasePO));
        }
        // 这个是说明是富文本，所以放到特色说明里
        if(StringUtils.isNotBlank(scenicDetail.getScenicDescription())){
            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
            itemFeaturePO.setDetail(scenicDetail.getScenicDescription());
            itemFeaturePO.setType(YcfConstants.POI_FEATURE_DETAIL);
            featurePOs.add(itemFeaturePO);
        }
        // 这个不是按富文本处理的
//        productItemPO.setDescription(scenicDetail.getScenicDescription());
        productItemPO.setAppMainTitle(scenicDetail.getRecommend());
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
            switch (Integer.valueOf(ticketDetail.getMpType())){
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
        ticketPO.setTickets(Lists.newArrayList(ticketInfoPO));
        productPO.setTicket(ticketPO);
        if(ticketDetail.getCustInfoLimit() != null){
            BookRulePO contactPhone = convertBookRulePO("0", false, null, 1);
            BookRulePO contactPhoneAndID = convertBookRulePO("0", true, ticketDetail.getCertificateType(), 1);
            BookRulePO passengerPhone = convertBookRulePO("1", false, null, 0);
            BookRulePO passengerPhoneAndID = convertBookRulePO("1", true, ticketDetail.getCertificateType(), 0);
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
                    bookRules.add(passengerPhone);
                    break;
                case DfyConstants.BOOK_RULE_7:
                    bookRules.add(contactPhoneAndID);
                    bookRules.add(passengerPhoneAndID);
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
                priceInfoPO.setSaleDate(DateTimeUtil.parseDate(p.getDepartDate()));
                if(StringUtils.isNotBlank(p.getSalePrice())){
                    // todo 加价计算公式
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

    private static BookRulePO convertBookRulePO(String ruleType, boolean credential, String credentialList, int limit){
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
                        return Certificate.SOLDIERS.getCode();
                    default:
                        return Certificate.ID_CARD.getCode();
                }
            }).collect(Collectors.toList());
            bookRulePO.setCredentials(creds);
        }
        bookRulePO.setEmail(false);
        bookRulePO.setEnName(false);
        bookRulePO.setPeopleLimit(limit);
//        bookRulePO.setPeopleNum(1);
        return bookRulePO;
    }
}
