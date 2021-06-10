package com.huoli.trip.supplier.web.difengyun.convert;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TripModuleTypeEnum;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.ImageBase;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyBookNotice;
import com.huoli.trip.supplier.self.difengyun.vo.DfyImage;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursDetailResponse;
import com.huoli.trip.supplier.self.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
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

    public static ProductItemPO convertToProductItemPO(DfyToursDetailResponse dfyToursDetail, String productId) {
        ProductItemPO productItemPO = new ProductItemPO();
        productItemPO.setItemType(Constants.PRODUCT_ITEM_TYPE_TRIP);
        productItemPO.setStatus(Constants.PRODUCT_STATUS_VALID);
        productItemPO.setSupplierId(Constants.SUPPLIER_CODE_DFY_TOURS);
        productItemPO.setSupplierItemId(productId);
        productItemPO.setCode(CommonUtils.genCodeBySupplier(productItemPO.getSupplierId(), productId));
        productItemPO.setName(dfyToursDetail.getProductName());
        if (ListUtils.isNotEmpty(dfyToursDetail.getDepartCitys())) {
            String city = dfyToursDetail.getDepartCitys().stream().map(c ->
                    c.getName()).distinct().collect(Collectors.joining(","));
            String cityCode = dfyToursDetail.getDepartCitys().stream().map(c ->
                    c.getCode()).distinct().collect(Collectors.joining(","));
            productItemPO.setOriCity(CommonUtil.getCity(city));
            productItemPO.setOriCityCode(cityCode);
        }
        if (ListUtils.isNotEmpty(dfyToursDetail.getDesPoiNameList())) {
            String city = dfyToursDetail.getDesPoiNameList().stream().filter(c ->
                    StringUtils.isNotBlank(c.getDesCityName())).map(c ->
                    CommonUtil.getCity(c.getDesCityName())).distinct().collect(Collectors.joining(","));
            String province = dfyToursDetail.getDesPoiNameList().stream().filter(c ->
                    StringUtils.isNotBlank(c.getDesProvinceName())).map(c ->
                    c.getDesProvinceName()).distinct().collect(Collectors.joining(","));
            // 直辖市名字有可能会放在省字段上的
            if (StringUtils.isBlank(city)) {
                city = CommonUtil.getCity(province);
            }
            productItemPO.setCity(city);
            productItemPO.setDesCity(city);
            productItemPO.setProvince(province);
        }

        if (ListUtils.isNotEmpty(dfyToursDetail.getProductPicList())) {
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

    public static ProductPO convertToProductPO(DfyToursDetailResponse dfyToursDetail, String productId, String city) {
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
        productPO.setBuyMin(1);
        productPO.setBuyMax(99);
        productPO.setMinProfitRate(dfyToursDetail.getMinProfitRate());
        productPO.setAvgProfitRate(dfyToursDetail.getAvgProfitRate());
        productPO.setMaxProfitRate(dfyToursDetail.getMaxProfitRate());
        productPO.setTripDays(dfyToursDetail.getDuration());
        productPO.setTripNights(dfyToursDetail.getProductNight());
        if (dfyToursDetail.getTrafficGo() != null) {
            productPO.setGoTraffic(convertToTraffic(dfyToursDetail.getTrafficGo()));
        }
        if (dfyToursDetail.getTrafficBack() != null) {
            productPO.setReturnTraffic(convertToTraffic(dfyToursDetail.getTrafficBack()));
        }
        if(dfyToursDetail.getTeamType() != null){
            if(dfyToursDetail.getTeamType() == 0){
                productPO.setSite("1");
            } else if(dfyToursDetail.getTeamType() == 1){
                productPO.setSite("2");
            } else if(dfyToursDetail.getTeamType() == 2){
                productPO.setSite("3");
            }
        }
        DfyJourneyInfo journeyInfo = dfyToursDetail.getJourneyInfo();
        if (ListUtils.isNotEmpty(journeyInfo.getTourRecommend())) {
            productPO.setRecommendDesc(journeyInfo.getTourRecommend().stream().map(r -> {
                ToursRecommendPO toursRecommend = new ToursRecommendPO();
                toursRecommend.setDescription(r.getDescription());
                toursRecommend.setType(r.getType());
                toursRecommend.setSort(r.getSort());
                return toursRecommend;
            }).collect(Collectors.toList()));
        }
        productPO.setIncludeDesc(journeyInfo.getCostInclude());
        productPO.setExcludeDesc(journeyInfo.getCostExclude());
        if (journeyInfo.getBookNotice() != null) {
//            String bookNotice = buildBookNotice(journeyInfo);
//            if(StringUtils.isNotBlank(bookNotice)){
//                productPO.setBookDesc(bookNotice);
//            }
            List<DescriptionPO> descriptionPOs = buildBookNoticeList(journeyInfo);
            productPO.setBookNoticeList(descriptionPOs);
            if (ListUtils.isNotEmpty(journeyInfo.getBookNotice().getDiffPrice())) {
                productPO.setDiffPriceDesc(String.join("<br>", journeyInfo.getBookNotice().getDiffPrice()));
            }
        }
        if (ListUtils.isNotEmpty(journeyInfo.getImportantAddition())) {
            productPO.setRemark(String.join("<br>", journeyInfo.getImportantAddition()));
        }
        if (StringUtils.isNotBlank(journeyInfo.getPeopleLimitDesc())) {
            StringBuffer sb = new StringBuffer();
            if (StringUtils.isNotBlank(productPO.getRemark())) {
                sb.append(productPO.getRemark()).append("<br>");
            }
            sb.append("特殊人群：").append("<br>").append(journeyInfo.getPeopleLimitDesc());
            productPO.setRemark(sb.toString());
        }

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
        if (journeyInfo.getIndependentTeam() != null) {
            if (journeyInfo.getIndependentTeam() == 1) {
                productPO.setProductFrom(String.valueOf(Constants.PRODUCT_FROM_SELF));
            } else if (journeyInfo.getIndependentTeam() == 0) {
                productPO.setProductFrom(String.valueOf(Constants.PRODUCT_FROM_OUT));
            }
        }
        productPO.setSiteDesc(journeyInfo.getBeginPlaceDesc());
        productPO.setJoinGroup(journeyInfo.getJoinGroupItem());
        productPO.setSafeNoticeUrl(journeyInfo.getSafeNoticeUrl());
        productPO.setCivilizedLedge(journeyInfo.getCivilizedLedge());
        if (ListUtils.isNotEmpty(journeyInfo.getRiskContents())) {
            List<RiskContentPO> riskContents = journeyInfo.getRiskContents().stream().map(r -> {
                RiskContentPO riskContentPO = new RiskContentPO();
                riskContentPO.setTitle(r.getRiskTitle());
                if (ListUtils.isNotEmpty(r.getRiskDetails())) {
                    riskContentPO.setContent(String.join("<br>", r.getRiskDetails()));
                }
                return riskContentPO;
            }).collect(Collectors.toList());
            productPO.setRiskContents(riskContents);
        }
        return productPO;
    }

    public static HodometerPO convertToHodometerPO(DfyJourneyInfo journeyInfo, String code) {
        HodometerPO hodometerPO = new HodometerPO();
        hodometerPO.setCode(code);
        hodometerPO.setType(2);
        List<Hodometer> hodometers = Lists.newArrayList();
        journeyInfo.getJourneyDescJson().getData().getData().sort(Comparator.comparing(d -> d.getDay(), Integer::compareTo));
        for (DfyJourneyDetail.Journey data : journeyInfo.getJourneyDescJson().getData().getData()) {
            Hodometer hodometer = new Hodometer();
            if (data.getTraffic() != null) {
                DfyJourneyDetail.JourneyTraffic journeyTraffic = data.getTraffic();
                hodometer.setDepartureCity(journeyTraffic.getFrom());
                if (ListUtils.isNotEmpty(journeyTraffic.getToList())) {
                    hodometer.setUrbanTraffics(journeyTraffic.getToList().stream().map(t -> {
                        UrbanTraffic urbanTraffic = new UrbanTraffic();
                        urbanTraffic.setArrivalCity(t.getTo());
                        if ("火车".equals(t.getMeans())) {
                            urbanTraffic.setTransportation(Constants.TRIP_TRAFFIC_TRAIN);
                        } else if ("飞机".equals(t.getMeans())) {
                            urbanTraffic.setTransportation(Constants.TRIP_TRAFFIC_AIRPLANE);
                        } else if ("轮船".equals(t.getMeans())) {
                            urbanTraffic.setTransportation(Constants.TRIP_TRAFFIC_SHIP);
                        } else if ("汽车".equals(t.getMeans())) {
                            urbanTraffic.setTransportation(Constants.TRIP_TRAFFIC_CAR);
                        } else if ("自行安排".equals(t.getMeans())) {
                            urbanTraffic.setTransportation(Constants.TRIP_TRAFFIC_CUSTOM);
                        }
                        return urbanTraffic;
                    }).collect(Collectors.toList()));
                }
            }
            List<Route> routes = Lists.newArrayList();
            for (DfyJourneyDetail.JourneyModule journeyModule : data.getModuleList()) {
                int type = journeyModule.getModuleTypeValue();
                if (type == DfyConstants.MODULE_TYPE_SCENIC &&
                        ListUtils.isNotEmpty(journeyModule.getScenicList())) {
                    for (DfyJourneyDetail.ModuleScenic scenic : journeyModule.getScenicList()) {
                        Route route = new Route();
                        route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_SCENIC.getCode());
                        route.setName(scenic.getTitle());
                        route.setTitle(scenic.getTitle());
                        route.setDuration(scenic.getTimes() == null || scenic.getTimes() <= 0 ? null : scenic.getTimes().toString());
                        if (ListUtils.isNotEmpty(scenic.getPicture())) {
                            route.setImages(convertToImageBase(scenic.getPicture()));
                        }
                        if (StringUtils.isNotBlank(scenic.getContent())) {
                            route.setDescribe(scenic.getContent().replace("<pre>", "").replace("</pre>", ""));
                        }
                        routes.add(route);
                    }
                }
                if (type == DfyConstants.MODULE_TYPE_HOTEL
                        && ListUtils.isNotEmpty(journeyModule.getHotelList())) {
                    String name = journeyModule.getHotelList().stream().map(m -> m.getTitle()).collect(Collectors.joining(","));
                    Route route = new Route();
                    route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_HOTEL.getCode());
                    route.setName(name);
                    route.setTitle(name);
                    if (StringUtils.isNotBlank(journeyModule.getDescription())) {
                        route.setDescribe(journeyModule.getDescription().replace("<pre>", "").replace("</pre>", ""));
                    }
                    routes.add(route);
                }
                if (type == DfyConstants.MODULE_TYPE_TRAFFIC && journeyModule.getTraffic() != null) {
                    Route route = new Route();
                    route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_TRAFFIC.getCode());
                    route.setDeparture(journeyModule.getTraffic().getFrom());
                    route.setArrival(journeyModule.getTraffic().getTo());
                    route.setDuration(journeyModule.getTraffic().getTimes() == null
                            || journeyModule.getTraffic().getTimes() <= 0 ? null : journeyModule.getTraffic().getTimes().toString());
                    routes.add(route);
                }
                if (type == DfyConstants.MODULE_TYPE_FOOD && journeyModule.getFood() != null) {
                    Route route = new Route();
                    route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_FOOD.getCode());
                    route.setTitle(journeyModule.getFood().getTitle());
                    route.setName(journeyModule.getFood().getTitle());
                    route.setDuration(journeyModule.getFood().getTimes() == null
                            || journeyModule.getFood().getTimes() <= 0 ? null : journeyModule.getFood().getTimes().toString());
                    routes.add(route);
                }
                if (type == DfyConstants.MODULE_TYPE_SHOPPING && ListUtils.isNotEmpty(journeyModule.getShopList())) {
                    for (DfyJourneyDetail.ModuleShop shop : journeyModule.getShopList()) {
                        Route route = new Route();
                        route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_SHOPPING.getCode());
                        route.setDuration(shop.getTimes() == null || shop.getTimes() <= 0 ? null : shop.getTimes().toString());
                        route.setTitle(shop.getTitle());
                        route.setBusinessProducts(shop.getProduct());
                        if (StringUtils.isNotBlank(shop.getInstruction())) {
                            route.setDescribe(shop.getInstruction().replace("<pre>", "").replace("</pre>", ""));
                        }
                        routes.add(route);
                    }
                }
                if (type == DfyConstants.MODULE_TYPE_ACTIVITY && journeyModule.getActivity() != null) {
                    Route route = new Route();
                    route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_ACTIVITY.getCode());
                    route.setTitle(journeyModule.getActivity().getTitle());
                    route.setDuration(journeyModule.getActivity().getTimes() == null
                            || journeyModule.getActivity().getTimes() <= 0 ? null : journeyModule.getActivity().getTimes().toString());
                    routes.add(route);
                }
                if (type == DfyConstants.MODULE_TYPE_REMINDER && journeyModule.getRemind() != null) {
                    Route route = new Route();
                    route.setMduleType(TripModuleTypeEnum.MODULE_TYPE_REMINDER.getCode());
                    route.setTitle(journeyModule.getRemind().getType());
                    if (StringUtils.isNotBlank(journeyModule.getRemind().getContent())) {
                        route.setDescribe(journeyModule.getRemind().getContent().replace("<pre>", "").replace("</pre>", ""));
                    }
                    routes.add(route);
                }
            }
            hodometer.setRoutes(routes);
            hodometers.add(hodometer);
        }
        hodometerPO.setHodometers(hodometers);
        return hodometerPO;
    }

    public static List<ImageBase> convertToImageBase(List<DfyJourneyDetail.JourneyPicture> pics) {
        return pics.stream().map(p -> {
            ImageBase imageBase = new ImageBase();
            imageBase.setUrl(p.getUrl());
            imageBase.setDesc(p.getTitle());
            return imageBase;
        }).collect(Collectors.toList());
    }

    public static String buildBookNotice(DfyJourneyInfo journeyInfo) {
        DfyBookNotice dfyBookNotice = journeyInfo.getBookNotice();
        StringBuffer sb = new StringBuffer();
        /*
        交通（标题：交通）、住宿（标题：住宿）、游览（标题：游览）、
        购物（标题：购物）、出团通知（标题：出团）、意见反馈（标题：意见）、
        活动说明（标题：说明）、附加预订须知（标题：附加）、温馨提示（标题：提示）、
        特殊信息+childStdInfo（标题：特殊信息）、注意事项（标题：注意事项）、
        手动须知（标题：其他）、团队用餐（标题：用餐）
         */
        if (ListUtils.isNotEmpty(dfyBookNotice.getTrafficInfos())) {
            sb.append("交通：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getTrafficInfos()))
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getAccInfos())) {
            sb.append("住宿：").append("<br>")
                    .append(dfyBookNotice.getAccInfos())
                    .append("<br>");
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getTour())) {
            sb.append("游览：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getTour()))
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getShopping())) {
            sb.append("购物：").append("<br>")
                    .append(dfyBookNotice.getShopping())
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getDepartureNotice())) {
            sb.append("出团：").append("<br>")
                    .append(dfyBookNotice.getDepartureNotice())
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getSuggestionFeedback())) {
            sb.append("意见：").append("<br>")
                    .append(dfyBookNotice.getSuggestionFeedback())
                    .append("<br>");
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getActivityArrangment())) {
            sb.append("说明：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getActivityArrangment()))
                    .append("<br>");
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getOrderAttentions())) {
            sb.append("附加：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getOrderAttentions()))
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getWarmAttention())) {
            sb.append("提示：").append("<br>")
                    .append(dfyBookNotice.getWarmAttention())
                    .append("<br>");
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getSpecialTerms())) {
            sb.append("特殊信息：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getSpecialTerms()))
                    .append("<br>");
            if (StringUtils.isNotBlank(journeyInfo.getChildStdInfo())) {
                sb.append(journeyInfo.getChildStdInfo()).append("<br>");
            }
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getAbroadNotice())) {
            sb.append("注意事项：").append("<br>")
                    .append(String.join("<br>", dfyBookNotice.getAbroadNotice()))
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getMealInfos())) {
            sb.append("用餐：").append("<br>")
                    .append(dfyBookNotice.getMealInfos())
                    .append("<br>");
        }
        if (StringUtils.isNotBlank(dfyBookNotice.getManualAttention())) {
            sb.append("其他：").append("<br>")
                    .append(dfyBookNotice.getManualAttention())
                    .append("<br>");
        }
        return sb.toString();
    }

    public static List<DescriptionPO> buildBookNoticeList(DfyJourneyInfo journeyInfo) {
        DfyBookNotice dfyBookNotice = journeyInfo.getBookNotice();
        List<DescriptionPO> descList = Lists.newArrayList();
        /*
        交通（标题：交通）、住宿（标题：住宿）、游览（标题：游览）、
        购物（标题：购物）、出团通知（标题：出团）、意见反馈（标题：意见）、
        活动说明（标题：说明）、附加预订须知（标题：附加）、温馨提示（标题：提示）、
        特殊信息+childStdInfo（标题：特殊信息）、注意事项（标题：注意事项）、
        手动须知（标题：其他）、团队用餐（标题：用餐）
         */
        // 这些是固定的，空的也要添加
        DescriptionPO bookDesc = new DescriptionPO();
        bookDesc.setTitle("预定须知");
        descList.add(bookDesc);

        DescriptionPO descriptionPO1 = new DescriptionPO();
        descriptionPO1.setTitle("交通须知");
        if (ListUtils.isNotEmpty(dfyBookNotice.getTrafficInfos())) {
            descriptionPO1.setContent(String.join("<br>", dfyBookNotice.getTrafficInfos()));
        }
        descList.add(descriptionPO1);

        DescriptionPO descriptionPO2 = new DescriptionPO();
        descriptionPO2.setTitle("住宿须知");
        if (StringUtils.isNotBlank(dfyBookNotice.getAccInfos())) {
            descriptionPO2.setContent(dfyBookNotice.getAccInfos());
        }
        descList.add(descriptionPO2);

        DescriptionPO descriptionPO3 = new DescriptionPO();
        descriptionPO3.setTitle("用餐须知");
        if (StringUtils.isNotBlank(dfyBookNotice.getMealInfos())) {
            descriptionPO3.setContent(dfyBookNotice.getMealInfos());
        }
        descList.add(descriptionPO3);

        DescriptionPO descriptionPO4 = new DescriptionPO();
        descriptionPO4.setTitle("购物须知");
        if (StringUtils.isNotBlank(dfyBookNotice.getShopping())) {
            descriptionPO4.setContent(dfyBookNotice.getShopping());
        }
        descList.add(descriptionPO4);

        DescriptionPO descriptionPO5 = new DescriptionPO();
        descriptionPO5.setTitle("游览须知");
        if (ListUtils.isNotEmpty(dfyBookNotice.getTour())) {
            descriptionPO5.setContent(String.join("<br>", dfyBookNotice.getTour()));
        }
        descList.add(descriptionPO5);

        DescriptionPO descriptionPO6 = new DescriptionPO();
        descriptionPO6.setTitle("出行通知");
        if (StringUtils.isNotBlank(dfyBookNotice.getDepartureNotice())) {
            descriptionPO6.setContent(String.join("<br>", dfyBookNotice.getDepartureNotice()));
        }
        descList.add(descriptionPO6);

        DescriptionPO descriptionPO7 = new DescriptionPO();
        descriptionPO7.setTitle("特殊信息");
        if (ListUtils.isNotEmpty(dfyBookNotice.getSpecialTerms())) {
            descriptionPO7.setContent(String.join("<br>", dfyBookNotice.getSpecialTerms()));
        }
        descList.add(descriptionPO7);

        DescriptionPO descriptionPO8 = new DescriptionPO();
        descriptionPO8.setTitle("注意事项");
        if (ListUtils.isNotEmpty(dfyBookNotice.getAbroadNotice())) {
            descriptionPO8.setContent(String.join("<br>", dfyBookNotice.getAbroadNotice()));
        }
        descList.add(descriptionPO8);

        DescriptionPO descriptionPO9 = new DescriptionPO();
        descriptionPO9.setTitle("温馨提示");
        if (StringUtils.isNotBlank(dfyBookNotice.getWarmAttention())) {
            descriptionPO9.setContent(dfyBookNotice.getWarmAttention());
        }
        descList.add(descriptionPO9);

        DescriptionPO descriptionPO10 = new DescriptionPO();
        descriptionPO10.setTitle("其他说明");
        if (StringUtils.isNotBlank(dfyBookNotice.getManualAttention())) {
            descriptionPO10.setContent(dfyBookNotice.getManualAttention());
        }
        descList.add(descriptionPO10);

        // 这些是动态的，没有就不加
        if (StringUtils.isNotBlank(dfyBookNotice.getSuggestionFeedback())) {
            DescriptionPO descriptionPO = new DescriptionPO();
            descriptionPO.setTitle("意见反馈");
            descriptionPO.setContent(dfyBookNotice.getSuggestionFeedback());
            descList.add(descriptionPO);
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getActivityArrangment())) {
            DescriptionPO descriptionPO = new DescriptionPO();
            descriptionPO.setTitle("活动说明");
            descriptionPO.setContent(String.join("<br>", dfyBookNotice.getActivityArrangment()));
            descList.add(descriptionPO);
        }
        if (ListUtils.isNotEmpty(dfyBookNotice.getOrderAttentions())) {
            DescriptionPO descriptionPO = new DescriptionPO();
            descriptionPO.setTitle("附加预订须知");
            descriptionPO.setContent(String.join("<br>", dfyBookNotice.getOrderAttentions()));
            descList.add(descriptionPO);
        }
        return descList;
    }

    public static Integer convertToTraffic(Integer dfyTraffic) {
        switch (dfyTraffic) {
            case DfyConstants.TRAFFIC_TYPE_1:
                return Constants.TRIP_TRAFFIC_1;
            case DfyConstants.TRAFFIC_TYPE_2:
                return Constants.TRIP_TRAFFIC_14;
            case DfyConstants.TRAFFIC_TYPE_3:
                return Constants.TRIP_TRAFFIC_4;
            case DfyConstants.TRAFFIC_TYPE_4:
                return Constants.TRIP_TRAFFIC_6;
            case DfyConstants.TRAFFIC_TYPE_5:
                return Constants.TRIP_TRAFFIC_5;
            case DfyConstants.TRAFFIC_TYPE_6:
                return Constants.TRIP_TRAFFIC_2;
            case DfyConstants.TRAFFIC_TYPE_7:
                return Constants.TRIP_TRAFFIC_11;
            case DfyConstants.TRAFFIC_TYPE_8:
                return Constants.TRIP_TRAFFIC_3;
            case DfyConstants.TRAFFIC_TYPE_9:
                return Constants.TRIP_TRAFFIC_7;
            case DfyConstants.TRAFFIC_TYPE_10:
                return Constants.TRIP_TRAFFIC_12;
            case DfyConstants.TRAFFIC_TYPE_11:
                return Constants.TRIP_TRAFFIC_8;
            case DfyConstants.TRAFFIC_TYPE_12:
                return Constants.TRIP_TRAFFIC_9;
            case DfyConstants.TRAFFIC_TYPE_13:
                return Constants.TRIP_TRAFFIC_10;
            case DfyConstants.TRAFFIC_TYPE_14:
                return Constants.TRIP_TRAFFIC_13;
        }
        return null;
    }

    public static List<DescInfo> buildBookNoticeListV2(DfyJourneyInfo journeyInfo) {
        DfyBookNotice dfyBookNotice = journeyInfo.getBookNotice();
        List<DescInfo> descList = Lists.newArrayList();

        if (ListUtils.isNotEmpty(dfyBookNotice.getTrafficInfos())) {
            DescInfo descriptionPO1 = new DescInfo();
            descriptionPO1.setTitle("交通须知");
            descriptionPO1.setContent(String.join("<br>", dfyBookNotice.getTrafficInfos()));
            descList.add(descriptionPO1);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getAccInfos())) {
            DescInfo descriptionPO2 = new DescInfo();
            descriptionPO2.setTitle("住宿须知");
            descriptionPO2.setContent(dfyBookNotice.getAccInfos());
            descList.add(descriptionPO2);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getMealInfos())) {
            DescInfo descriptionPO3 = new DescInfo();
            descriptionPO3.setTitle("用餐须知");
            descriptionPO3.setContent(dfyBookNotice.getMealInfos());
            descList.add(descriptionPO3);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getShopping())) {
            DescInfo descriptionPO4 = new DescInfo();
            descriptionPO4.setTitle("购物须知");
            descriptionPO4.setContent(dfyBookNotice.getShopping());
            descList.add(descriptionPO4);
        }

        if (ListUtils.isNotEmpty(dfyBookNotice.getTour())) {
            DescInfo descriptionPO5 = new DescInfo();
            descriptionPO5.setTitle("游览须知");
            descriptionPO5.setContent(String.join("<br>", dfyBookNotice.getTour()));
            descList.add(descriptionPO5);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getDepartureNotice())) {
            DescInfo descriptionPO6 = new DescInfo();
            descriptionPO6.setTitle("出行通知");
            descriptionPO6.setContent(String.join("<br>", dfyBookNotice.getDepartureNotice()));
            descList.add(descriptionPO6);
        }

        if (ListUtils.isNotEmpty(dfyBookNotice.getSpecialTerms())) {
            DescInfo descriptionPO7 = new DescInfo();
            descriptionPO7.setTitle("特殊信息");
            descriptionPO7.setContent(String.join("<br>", dfyBookNotice.getSpecialTerms()));
            descList.add(descriptionPO7);
        }

        if (ListUtils.isNotEmpty(dfyBookNotice.getAbroadNotice())) {
            DescInfo descriptionPO8 = new DescInfo();
            descriptionPO8.setTitle("注意事项");
            descriptionPO8.setContent(String.join("<br>", dfyBookNotice.getAbroadNotice()));
            descList.add(descriptionPO8);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getWarmAttention())) {
            DescInfo descriptionPO9 = new DescInfo();
            descriptionPO9.setTitle("温馨提示");
            descriptionPO9.setContent(dfyBookNotice.getWarmAttention());
            descList.add(descriptionPO9);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getManualAttention())) {
            DescInfo descriptionPO10 = new DescInfo();
            descriptionPO10.setTitle("其他说明");
            descriptionPO10.setContent(dfyBookNotice.getManualAttention());
            descList.add(descriptionPO10);
        }

        if (StringUtils.isNotBlank(dfyBookNotice.getSuggestionFeedback())) {
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("意见反馈");
            descriptionPO.setContent(dfyBookNotice.getSuggestionFeedback());
            descList.add(descriptionPO);
        }

        if (ListUtils.isNotEmpty(dfyBookNotice.getActivityArrangment())) {
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("活动说明");
            descriptionPO.setContent(String.join("<br>", dfyBookNotice.getActivityArrangment()));
            descList.add(descriptionPO);
        }

        if (ListUtils.isNotEmpty(dfyBookNotice.getOrderAttentions())) {
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("附加预订须知");
            descriptionPO.setContent(String.join("<br>", dfyBookNotice.getOrderAttentions()));
            descList.add(descriptionPO);
        }

        if(ListUtils.isNotEmpty(journeyInfo.getImportantAddition())){
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("附加说明");
            descriptionPO.setContent(String.join("<br>", journeyInfo.getImportantAddition()));
            descList.add(descriptionPO);
        }

        if(StringUtils.isNotBlank(journeyInfo.getPeopleLimitDesc())){
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("特殊人群限制");
            descriptionPO.setContent(journeyInfo.getPeopleLimitDesc());
            descList.add(descriptionPO);
        }

        if(StringUtils.isNotBlank(journeyInfo.getLegalRemark())){
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("补充条款");
            descriptionPO.setContent(journeyInfo.getLegalRemark());
            descList.add(descriptionPO);
        }

        if(ListUtils.isNotEmpty(journeyInfo.getRiskContents())){
            DescInfo descriptionPO = new DescInfo();
            descriptionPO.setTitle("高危项目安全须知");
            List<String> contents = journeyInfo.getRiskContents().stream().map(r ->
                    String.format("%s<br>%s", r.getRiskTitle(), String.join("<br>", r.getRiskDetails())))
                    .collect(Collectors.toList());
            descriptionPO.setContent(String.join("<br>", contents));
            descList.add(descriptionPO);
        }
        return descList;
    }
}
