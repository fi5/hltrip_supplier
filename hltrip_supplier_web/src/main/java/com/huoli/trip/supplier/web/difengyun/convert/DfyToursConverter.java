package com.huoli.trip.supplier.web.difengyun.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyAdmissionVoucher;
import com.huoli.trip.supplier.self.difengyun.vo.DfyImage;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursDetailResponse;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
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
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
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

    public static ProductPO convertToProductPO(DfyToursDetailResponse dfyToursDetail, String productId){
        ProductPO productPO = new ProductPO();
        // 默认条件退
        productPO.setRefundType(3);
        productPO.setStatus(1);
        productPO.setProductType(ProductType.TRIP_GROUP.getCode());
        productPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
        productPO.setSupplierName(Constants.SUPPLIER_NAME_DFY);
        productPO.setSupplierProductId(productId);
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), productId));
        productPO.setName(dfyToursDetail.getProductName());
        // todo 跟团游没有价格
//        productPO.setPrice(StringUtils.isBlank(ticketDetail.getWebPrice()) ? null : new BigDecimal(ticketDetail.getWebPrice()));
//        productPO.setSalePrice(StringUtils.isBlank(ticketDetail.getSalePrice()) ? null : new BigDecimal(ticketDetail.getSalePrice()));
        // todo 预定须知拼接
        productPO.setBookDesc(ticketDetail.getBookNotice());
        productPO.setBuyMin(0);
        productPO.setBuyMax(0);
        DfyJourneyInfo journeyInfo = dfyToursDetail.getJourneyInfo();
        if(ListUtils.isNotEmpty(journeyInfo.getImportantAddition())){
            productPO.setRemark(String.join("<br>", journeyInfo.getImportantAddition()));
        }
        if(StringUtils.isNotBlank(journeyInfo.getPeopleLimitDesc())){
            productPO.setRemark(String.join("<br>", productPO.getBookDesc(), journeyInfo.getPeopleLimitDesc()));
        }
        // todo 没有整体退改说明
        productPO.setRefundDesc(ticketDetail.getMpLossInfo());
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
        productPO.set;
        return productPO;
    }
}
