package com.huoli.trip.supplier.web.lvmama.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.ItemFeaturePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.lvmama.vo.LmmScenic;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
        // todo 主题是文字，跟本地code不能对应
//        productItemPO.setTopic();
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
        // todo 不清楚接口返回什么格式，文档看不出来
//        productItemPO.setImages();
//        productItemPO.setMainImages();
        productItemPO.setAddress(lmmScenic.getPlaceToAddr());
        if(ListUtils.isNotEmpty(lmmScenic.getOpenTimes())){
            StringBuffer sb = new StringBuffer();
            for (LmmScenic.OpenTime openTime : lmmScenic.getOpenTimes()) {
                sb.append(openTime.getOpenTimeInfo()).append(" : ")
                        .append(openTime.getSightStart()).append("-")
                        .append(openTime.getSightEnd()).append("<br>");
            }
            productItemPO.setBusinessHours(sb.toString());
        }
        productItemPO.setTown(lmmScenic.getPlaceTown());
        productItemPO.setDistrict(lmmScenic.getPlaceXian());
        productItemPO.setCity(lmmScenic.getPlaceCity());
        productItemPO.setProvince(lmmScenic.getPlaceProvince());
        productItemPO.setCountry(lmmScenic.getPlaceCountry());
        if(lmmScenic.getBaiduData() != null){
            List<Double> coordinate = Lists.newArrayList(lmmScenic.getBaiduData().getLongitude(), lmmScenic.getBaiduData().getLatitude());
            productItemPO.setItemCoordinate(coordinate.toArray(new Double[]{}));
        } else if(lmmScenic.getGoogleData() != null){
            List<Double> coordinate = Lists.newArrayList(lmmScenic.getGoogleData().getLongitude(), lmmScenic.getGoogleData().getLatitude());
            productItemPO.setItemCoordinate(coordinate.toArray(new Double[]{}));
        }
        return productItemPO;
    }
}
