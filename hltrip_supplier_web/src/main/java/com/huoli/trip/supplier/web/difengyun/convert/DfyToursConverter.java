package com.huoli.trip.supplier.web.difengyun.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.ImageBasePO;
import com.huoli.trip.common.entity.ItemFeaturePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.vo.DfyImage;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursDetailResponse;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
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
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
        productItemPO.setSupplierItemId(productId);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productId));
        productItemPO.setName(dfyToursDetail.getProductName());
        if(ListUtils.isNotEmpty(dfyToursDetail.getDepartCitys())){
            String city = dfyToursDetail.getDepartCitys().stream().map(c ->
                    c.getDepartCityName()).distinct().collect(Collectors.joining(","));
            productItemPO.setOriCity(city);
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
        List<ItemFeaturePO> featurePOs = Lists.newArrayList();
        if(dfyToursDetail.getJourneyInfo().getBookNotice() != null){
//            try {
//                ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
//                JSONArray jsonArray = JSON.parseArray(scenicDetail.getBookNotice());
//                StringBuilder sb = new StringBuilder();
//                for (Object o : jsonArray) {
//                    JSONObject obj = (JSONObject) o;
//                    sb.append(obj.get("name")).append("<br>")
//                            .append(obj.get("value")).append("<br>");
//                }
//                itemFeaturePO.setDetail(sb.toString());
//                itemFeaturePO.setType(YcfConstants.POI_FEATURE_BOOK_NOTE);
//                featurePOs.add(itemFeaturePO);
//            } catch (Exception e){
//                log.error("笛风云转换特色列表（购买须知）异常，不影响正常流程。。", e);
//            }
        }
//        if(StringUtils.isNotBlank(dfyToursDetail.getJourneyInfo().getJourneyDescJson().getTourTrafficInfo())){
//            ItemFeaturePO itemFeaturePO = new ItemFeaturePO();
//            itemFeaturePO.setDetail(scenicDetail.getTrafficBus());
//            itemFeaturePO.setType(YcfConstants.POI_FEATURE_TRAFFIC_NOTE);
//            featurePOs.add(itemFeaturePO);
//        }
//        if(ListUtils.isNotEmpty(featurePOs)){
//            productItemPO.setFeatures(featurePOs);
//        }
        return productItemPO;
    }
}
