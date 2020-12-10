package com.huoli.trip.supplier.web.difengyun.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
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
        productItemPO.setDescription(scenicDetail.getScenicDescription());
        // todo  recommend
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
        productPO.setSupplierId(Constants.SUPPLIER_CODE_DFY);
        productPO.setSupplierName(Constants.SUPPLIER_NAME_DFY);
        productPO.setSupplierProductId(ticketDetail.getProductId());
        productPO.setCode(CommonUtils.genCodeBySupplier(productPO.getSupplierId(), ticketDetail.getProductId()));
        productPO.setName(ticketDetail.getProductName());
        productPO.setPrice(StringUtils.isBlank(ticketDetail.getWebPrice()) ? null : new BigDecimal(ticketDetail.getWebPrice()));
        productPO.setSalePrice(StringUtils.isBlank(ticketDetail.getSalePrice()) ? null : new BigDecimal(ticketDetail.getSalePrice()));
        // todo 票信息要创建个ticketpo
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
        return null;
    }
}
