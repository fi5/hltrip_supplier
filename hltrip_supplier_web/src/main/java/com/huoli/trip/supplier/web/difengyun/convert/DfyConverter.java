package com.huoli.trip.supplier.web.difengyun.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
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
        productItemPO.setStatus(1);
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
        TicketPO ticketPO = new TicketPO();
        if(ticketDetail.getDrawType() != null){
            switch (ticketDetail.getDrawType()){
                case 1:
                    ticketPO.setObtainTicketMode("实体票");
                    break;
                case 8:
                    ticketPO.setObtainTicketMode("预付电子票");
                    break;
                default:
                    break;
            }
        }
        ticketPO.setDrawAddress(ticketDetail.getDrawAddress());
        Integer type = null;
        if(ticketDetail.getSubType() != null){
            switch (ticketDetail.getSubType()){
                case 1:
                    type = 1;
                    break;
                case 2:
                    type = 16;
                    break;
                case 3:
                    type = 17;
                    break;
                case 4:
                    type = 18;
                    break;
                default:
                    break;
            }
        }
        ticketPO.setTicketType(type);
        // todo indate  advanceDay advanceHour 这几个东西是不是可以拼到哪个说明里
        // todo admissionVoucher 入园方式现在没有。怎么处理

        TicketInfoPO ticketInfoPO = new TicketInfoPO();
        ticketInfoPO.setBaseNum(1);
        if(ticketDetail.getCustInfoLimit() != null){
            BookRulePO contactPhone = convertBookRulePO("0", false, null, 1);
            BookRulePO contactPhoneAndID = convertBookRulePO("0", true, ticketDetail.getCertificateType(), 1);
            BookRulePO passengerPhone = convertBookRulePO("1", false, null, 0);
            BookRulePO passengerPhoneAndID = convertBookRulePO("1", true, ticketDetail.getCertificateType(), 0);
            List<BookRulePO> bookRules = Lists.newArrayList();
            switch (ticketDetail.getCustInfoLimit()){
                case 1:
                    bookRules.add(contactPhone);
                    break;
                case 2:
                    bookRules.add(contactPhone);
                    bookRules.add(passengerPhone);
                    break;
                case 3:
                    bookRules.add(contactPhone);
                    bookRules.add(passengerPhoneAndID);
                    break;
                case 4:
                    bookRules.add(contactPhoneAndID);
                    break;
                case 6:
                    bookRules.add(contactPhoneAndID);
                    bookRules.add(passengerPhone);
                    break;
                case 7:
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

        return null;
    }

    public static PricePO convertToPricePO(List<DfyPriceCalendar> dfyPriceCalendars){
        PricePO pricePO = new PricePO();
        if(ListUtils.isNotEmpty(dfyPriceCalendars)){
            List<PriceInfoPO> priceInfoPOs = dfyPriceCalendars.stream().map(p -> {
                PriceInfoPO priceInfoPO = new PriceInfoPO();
                if(StringUtils.isBlank(p.getDepartDate())){
                    return null;
                }
                priceInfoPO.setSaleDate(DateTimeUtil.parseDate(p.getDepartDate()));
                if(StringUtils.isNotBlank(p.getSalePrice())){
                    priceInfoPO.setSalePrice(new BigDecimal(p.getSalePrice()));
                    // TODO 这里没有结算价，可能也会有问题
                    priceInfoPO.setSettlePrice(priceInfoPO.getSalePrice());
                }
                // todo 笛风云没有库存，怎么处理
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
                    case 1:
                        return Certificate.ID_CARD.getCode();
                    case 2:
                        return Certificate.PASSPORT.getCode();
                    case 3:
                        return Certificate.OFFICER.getCode();
                    case 4:
                        return Certificate.HKM_PASS.getCode();
                    case 7:
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
