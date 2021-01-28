package com.huoli.trip.supplier.web.difengyun.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.vo.DfyBookNotice;
import com.huoli.trip.supplier.self.difengyun.vo.DfyImage;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/26<br>
 */
@Slf4j
public class DfyToursConverter {

    public static ProductItemPO convertToProductItemPO(DfyToursDetailResponse dfyToursDetail, String productId){
        ProductItemPO productItemPO = new ProductItemPO();
        productItemPO.setItemType(ProductType.TRIP_GROUP.getCode());
        productItemPO.setStatus(Constants.PRODUCT_STATUS_VALID);
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_DFY_TOURS);
        productItemPO.setSupplierItemId(productId);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productId));
        productItemPO.setName(dfyToursDetail.getProductName());
        if(ListUtils.isNotEmpty(dfyToursDetail.getDepartCitys())){
            String city = dfyToursDetail.getDepartCitys().stream().map(c ->
                    c.getDepartCityName()).distinct().collect(Collectors.joining(","));
            String cityCode = dfyToursDetail.getDepartCitys().stream().map(c ->
                    c.getDepartCityCode()).distinct().collect(Collectors.joining(","));
            productItemPO.setOriCity(city);
            productItemPO.setOriCityCode(cityCode);
        }
        if(ListUtils.isNotEmpty(dfyToursDetail.getDesPoiNameList())){
            String city = dfyToursDetail.getDesPoiNameList().stream().filter(c ->
                    StringUtils.isNotBlank(c.getDesCityName())).map(c ->
                    c.getDesCityName()).distinct().collect(Collectors.joining(","));
            productItemPO.setCity(city);
            productItemPO.setDesCity(city);
            String province = dfyToursDetail.getDesPoiNameList().stream().filter(c ->
                    StringUtils.isNotBlank(c.getDesProvinceName())).map(c ->
                    c.getDesProvinceName()).distinct().collect(Collectors.joining(","));
            productItemPO.setProvince(province);
        }

        if(ListUtils.isNotEmpty(dfyToursDetail.getProductPicList())){
            List<DfyImage> dfyImages = dfyToursDetail.getProductPicList();
            List<ImageBasePO> images = dfyImages.stream().map(i -> {
                ImageBasePO imageBasePO = new ImageBasePO();
                imageBasePO.setUrl(i.getPath());
                imageBasePO.setDesc(i.getName());
                imageBasePO.setSequence(StringUtils.isBlank(i.getSeqNum()) ? null : Integer.valueOf(i.getSeqNum().trim()));
                return imageBasePO;
            }).collect(Collectors.toList());
            productItemPO.setImages(images);
            productItemPO.setMainImages(images);
        }
        productItemPO.setAppMainTitle(productItemPO.getName());
        return productItemPO;
    }

    public static ProductPO convertToProductPO(DfyToursDetailResponse dfyToursDetail, String productId, String city){
        ProductPO productPO = new ProductPO();
        // 默认条件退
        productPO.setRefundType(3);
        productPO.setStatus(1);
        productPO.setProductType(ProductType.TRIP_GROUP.getCode());
        productPO.setSupplierId(Constants.SUPPLIER_CODE_DFY_TOURS);
        productPO.setSupplierName(Constants.SUPPLIER_NAME_DFY_TOURS);
        productPO.setSupplierProductId(productId);
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), productId, city));
        productPO.setName(dfyToursDetail.getProductName());
        productPO.setBuyMin(99);
        productPO.setBuyMax(99);
        DfyJourneyInfo journeyInfo = dfyToursDetail.getJourneyInfo();
        if(journeyInfo.getBookNotice() != null){
            String bookNotice = buildBookNotice(journeyInfo);
            if(StringUtils.isNotBlank(bookNotice)){
                productPO.setBookDesc(bookNotice);
            }
        }
        if(ListUtils.isNotEmpty(journeyInfo.getImportantAddition())){
            productPO.setRemark(String.join("<br>", journeyInfo.getImportantAddition()));
        }
        if(StringUtils.isNotBlank(journeyInfo.getPeopleLimitDesc())){
            productPO.setRemark(String.join("<br>", productPO.getBookDesc(), journeyInfo.getPeopleLimitDesc()));
        }
        // todo 没有整体退改说明
//        productPO.setRefundDesc(ticketDetail.getMpLossInfo());
        // 预定规则根据下单证件规则
        List<BookRulePO> bookRules = Lists.newArrayList();
        BookRulePO contact = new BookRulePO();
        contact.setRuleType("0");
        contact.setCnName(true);
        contact.setPhone(true);
        contact.setEmail(true);
        BookRulePO passenger = new BookRulePO();
        passenger.setRuleType("1");
        passenger.setCnName(true);
        passenger.setPhone(true);
        passenger.setCredential(true);
        bookRules.add(contact);
        bookRules.add(passenger);
        productPO.setBookRules(bookRules);
        if(journeyInfo.getIndependentTeam() != null){
            if(journeyInfo.getIndependentTeam() == 1){
                productPO.setProductFrom(String.valueOf(Constants.PRODUCT_FROM_SELF));
            } else if(journeyInfo.getIndependentTeam() == 0){
                productPO.setProductFrom(String.valueOf(Constants.PRODUCT_FROM_OUT));
            }
        }
        productPO.setSiteDesc(journeyInfo.getBeginPlaceDesc());
        productPO.setJoinGroup(journeyInfo.getJoinGroupItem());
        productPO.setSafeNoticeUrl(journeyInfo.getSafeNoticeUrl());
        productPO.setCivilizedLedge(journeyInfo.getCivilizedLedge());
        if(ListUtils.isNotEmpty(journeyInfo.getRiskContents())){
            List<RiskContentPO> riskContents = journeyInfo.getRiskContents().stream().map(r -> {
                RiskContentPO riskContentPO = new RiskContentPO();
                riskContentPO.setTitle(r.getRiskTitle());
                if(ListUtils.isNotEmpty(r.getRiskDetails())){
                    riskContentPO.setContent(String.join("<br>", r.getRiskDetails()));
                }
                return riskContentPO;
            }).collect(Collectors.toList());
            productPO.setRiskContents(riskContents);
        }
        return productPO;
    }

    public static String buildBookNotice(DfyJourneyInfo journeyInfo){
        DfyBookNotice dfyBookNotice = journeyInfo.getBookNotice();
        StringBuffer sb = new StringBuffer();
        /*
        交通（标题：交通）、住宿（标题：住宿）、游览（标题：游览）、
        购物（标题：购物）、出团通知（标题：出团）、意见反馈（标题：意见）、
        活动说明（标题：说明）、附加预订须知（标题：附加）、温馨提示（标题：提示）、
        特殊信息+childStdInfo（标题：特殊信息）、注意事项（标题：注意事项）、
        手动须知（标题：其他）、团队用餐（标题：用餐）
         */
        if(ListUtils.isNotEmpty(dfyBookNotice.getTrafficInfos())){
            sb.append("交通：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getTrafficInfos()))
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getAccInfos())){
            sb.append("住宿：").append("<br>")
                    .append(dfyBookNotice.getAccInfos())
                    .append("<br>");
        }
        if(ListUtils.isNotEmpty(dfyBookNotice.getTour())){
            sb.append("游览：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getTour()))
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getShopping())){
            sb.append("购物：").append("<br>")
                    .append(dfyBookNotice.getShopping())
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getDepartureNotice())){
            sb.append("出团：").append("<br>")
                    .append(dfyBookNotice.getDepartureNotice())
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getSuggestionFeedback())){
            sb.append("意见：").append("<br>")
                    .append(dfyBookNotice.getSuggestionFeedback())
                    .append("<br>");
        }
        if(ListUtils.isNotEmpty(dfyBookNotice.getActivityArrangment())){
            sb.append("说明：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getActivityArrangment()))
                    .append("<br>");
        }
        if(ListUtils.isNotEmpty(dfyBookNotice.getOrderAttentions())){
            sb.append("附加：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getOrderAttentions()))
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getWarmAttention())){
            sb.append("提示：").append("<br>")
                    .append(dfyBookNotice.getWarmAttention())
                    .append("<br>");
        }
        if(ListUtils.isNotEmpty(dfyBookNotice.getSpecialTerms())){
            sb.append("特殊信息：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getSpecialTerms()))
                    .append("<br>");
            if(StringUtils.isNotBlank(journeyInfo.getChildStdInfo())){
                sb.append(journeyInfo.getChildStdInfo()).append("<br>");
            }
        }
        if(ListUtils.isNotEmpty(dfyBookNotice.getAbroadNotice())){
            sb.append("注意事项：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getAbroadNotice()))
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getMealInfos())){
            sb.append("用餐：").append("<br>")
                    .append(dfyBookNotice.getMealInfos())
                    .append("<br>");
        }
        if(StringUtils.isNotBlank(dfyBookNotice.getManualAttention())){
            sb.append("其他：").append("<br>")
                    .append(dfyBookNotice.getManualAttention())
                    .append("<br>");
        }
        return sb.toString();
    }
}
