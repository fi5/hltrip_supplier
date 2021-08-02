package com.huoli.trip.supplier.web.caissa.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.groupTour.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.data.api.ProductDataService;
import com.huoli.trip.supplier.web.caissa.constant.Constant;
import com.huoli.trip.supplier.web.caissa.enmu.TrafficEnum;
import com.huoli.trip.supplier.web.caissa.service.ParseService;
import com.huoli.trip.supplier.web.caissa.util.TimeUtil;
import com.huoli.trip.supplier.web.caissa.util.UnicodeConvertUtil;
import com.huoli.trip.supplier.web.dao.GroupTourProductDao;
import com.huoli.trip.supplier.web.dao.GroupTourProductSetMealDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotDao;
import com.huoli.trip.supplier.web.mapper.PassengerTemplateMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.huoli.trip.supplier.web.caissa.util.RgexUtil.getNumber;
import static com.huoli.trip.supplier.web.caissa.util.RgexUtil.replaceBr;
import static java.lang.String.format;

@Service
@Slf4j
public class ParseServiceImpl implements ParseService {

    @Autowired
    private GroupTourProductDao groupTourProductDao;

    @Autowired
    private GroupTourProductSetMealDao mealDao;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private PassengerTemplateMapper passengerTemplateMapper;

    private static class innerWebClient{
        private static final WebClient webClient = new WebClient();
    }

    @Override
    public void getList(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        String body = UnicodeConvertUtil.unicodeDecode(document.body().text());
        JSONObject jsonObject = JSONObject.parseObject(UnicodeConvertUtil.jsonString(body));
        if (jsonObject == null) {
            return;
        }
        JSONArray matches = jsonObject.getJSONArray("matches");
        if (matches == null || matches.size() < 1) {
            return;
        }
        log.info("matches: " + matches.size());
        for (Object o : matches) {
            GroupTourProductMPO mpo = new GroupTourProductMPO();
            GroupTourProductSetMealMPO mealMPO = new GroupTourProductSetMealMPO();

            JSONObject matchJson = (JSONObject) o;
            String sourceName = matchJson.getString("source_name");
            //不是自营的就跳过
            if (!sourceName.equals("team_list")) {
                continue;
            }
            String productDbId = matchJson.getString("product_db_id");
//            GroupTourProductMPO product = groupTourProductDao.getTourProduct(productDbId, Constant.CHANNEL);
//            if (product != null) {
//                continue;
//            }
            String dbId = matchJson.getString("db_id");
            String productCode = matchJson.getString("product_code");
            String productName = matchJson.getString("product_name");
            String teamName = matchJson.getString("team_name");
            String pattern = "\\[[^]]+]";    //中括号内
            teamName = teamName.replaceAll(pattern, "");
            String subTitle = matchJson.getString("sub_title");
            String departureName = matchJson.getString("departure_name");
            String departure = matchJson.getString("departure");
            String departureCode = commonService.queryCityCodeByName(departureName);
            String scheduleDays = matchJson.getString("schedule_days");
            String scheduleNights = matchJson.getString("schedule_nights");
            String allTripDate = matchJson.getJSONObject("stats_attrs").getString("all_trip_date");
            String activeDay = matchJson.getString("trip_date");
            activeDay = TimeUtil.splitStr(activeDay);
            JSONArray pictures = matchJson.getJSONObject("attributes").getJSONArray("picture");
            JSONArray wayCity = matchJson.getJSONArray("way_city_new");
            List<AddressInfo> arrInfos = new ArrayList<>();
            for (Object w : wayCity) {
                JSONObject jsonObject1 = (JSONObject) w;
                if (wayCity.size() != 1 && StringUtils.equals(jsonObject1.getString("name"), departureName)) {
                    continue;
                }
                AddressInfo addressInfo = new AddressInfo();
                addressInfo.setCityName(jsonObject1.getString("name"));
                addressInfo.setCityCode(commonService.queryCityCodeByName(addressInfo.getCityName()));
                addressInfo.setDestinationName(jsonObject1.getString("name"));
                addressInfo.setDestinationCode(commonService.queryCityCodeByName(addressInfo.getDestinationName()));
                arrInfos.add(addressInfo);
            }
            mpo.setArrInfos(arrInfos);
            List<String> picturesList = new ArrayList<>();
            String outerPictureLink = Constant.DEFAULT_PICTURE;
            if (pictures != null && pictures.size() > 0) {
                JSONObject o1;
                if (pictures.size() > 3) {
                    o1 = (JSONObject) pictures.get(2);
                } else {
                    o1 = (JSONObject) pictures.get(0);
                }
                outerPictureLink = format(Constant.LIST_PICTURE_LINK, o1.getString("file_code"));
                for (Object jsonObject1 : pictures) {
                    JSONObject jsonObject2 = (JSONObject) jsonObject1;
                    picturesList.add(format(Constant.DETAIL_PICTURE_LINK, jsonObject2.getString("file_code")));
                }
            }
            JSONArray applicablePeople = matchJson.getJSONArray("applicable_people");
            if (applicablePeople != null && applicablePeople.size() > 0) {
                JSONObject o1 = (JSONObject) applicablePeople.get(0);
                String name = o1.getString("name");
                switch (name) {
                    case "亲子":
                        mpo.setTravelCrowd("1");
                        break;
                    case "情侣":
                        mpo.setTravelCrowd("2");
                        break;
                    case "家庭":
                        mpo.setTravelCrowd("4");
                        break;
                    default:
                        mpo.setTravelCrowd("3");
                        break;
                }
            } else {
                mpo.setTravelCrowd("3");
            }
            mpo.setId(commonService.getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
            mpo.setSupplierProductId(productDbId);
            mpo.setProductName(teamName);
            mpo.setImages(picturesList);
            mpo.setMainImage(outerPictureLink);
            mpo.setMerchantCode(productCode);
            mpo.setCategory("group_tour");
            mpo.setChannel(Constant.CHANNEL);
            mpo.setStatus(0);
            mpo.setGroupTourType("1");
            mpo.setTravelerTemplateId(passengerTemplateMapper.getIdByName("凯撒跟团游"));
            mpo.setTravelerTemplateName("凯撒跟团游");
            GroupTourProductPayInfo groupTourProductPayInfo = new GroupTourProductPayInfo();
            String[] allDate = allTripDate.split(",");
            groupTourProductPayInfo.setGoDate(allDate[0]);
            groupTourProductPayInfo.setEndDate(allDate[allDate.length - 1]);
            groupTourProductPayInfo.setConfirmUploadDay(2);
            groupTourProductPayInfo.setSellType(1);
            GroupTourProductBaseSetting baseSetting = new GroupTourProductBaseSetting();
            baseSetting.setStockCount(0);
            baseSetting.setLaunchType(1);
            mpo.setGroupTourProductBaseSetting(baseSetting);
            mpo.setGroupTourProductPayInfo(groupTourProductPayInfo);
            mpo.setNonGroupAgreement("2");

            List<AddressInfo> depInfos = new ArrayList<>();
            AddressInfo fromCity = new AddressInfo();
            fromCity.setCityName(departureName);
            fromCity.setCityCode(departureCode);
            fromCity.setDestinationCode(departureCode);
            fromCity.setDestinationName(departureName);
            depInfos.add(fromCity);
            mpo.setDepInfos(depInfos);

            mealMPO.setId(commonService.getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT_MEAL));
            mealMPO.setDepCode(departureCode);
            mealMPO.setDepName(departureName);
            mealMPO.setName(productName);
            mealMPO.setTripDay(Integer.parseInt(scheduleDays));
            mealMPO.setTripNight(Integer.parseInt(scheduleNights));

            try {
                //获取详情
                String chdPrice = getDetail(format(Constant.CAISSA_APP_DETAIL_URL, dbId), dbId, mpo, mealMPO, activeDay);
                log.info("outer-chdPrice:{}", chdPrice);
                //获取价格日历
                getWebCalendars(format(Constant.CAISSA_WEB_SELF_CALENDARS, System.currentTimeMillis(), productDbId, scheduleDays, scheduleNights, departure), mealMPO, chdPrice);
                //获取费用说明
                getFee(dbId, mpo, mealMPO);
                //入库
                mpo.setCreateTime(new Date());
                mpo.setUpdateTime(new Date());
                mealMPO.setCreateTime(new Date());
                mealMPO.setUpdateTime(new Date());
                mpo = groupTourProductDao.updateProduct(mpo);
                mealMPO.setGroupTourProductId(mpo.getId());
                mealDao.updateSetMeals(mealMPO);
                commonService.refreshList(1, mpo.getId(), 1, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

//                RedisQueue.addForSet(Constant.DETAIL_PAGE_TO_VISIT, format(Constant.CAISSA_APP_DETAIL_URL, dbId));
//                if (StringUtils.isNotEmpty(scheduleDays)) {
//                    //添加到要访问的价格日历URL队列中
//                    RedisQueue.addForSet(Constant.CALENDAR_PAGE_TO_VISIT, format(Constant.CAISSA_WEB_SELF_CALENDARS, System.currentTimeMillis(), productDbId, scheduleDays, scheduleNights, departure));
//                }

        }
    }

    @Override
    public void getWebCalendars(String url, GroupTourProductSetMealMPO mealMPO, String chdPrice) throws IOException {
        Document document = Jsoup.connect(url).get();
        List<GroupTourPrice> groupTourPrices = new ArrayList<>();
        if (null != document) {
            String text = document.body().text();
            JSONObject jsonObject = JSONObject.parseObject(text);
            if (jsonObject != null) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data != null) {
                    Collection<Object> values = data.values();
                    if (!CollectionUtils.isEmpty(values)) {
                        for (Object o : values) {
                            JSONArray v = (JSONArray) o;
                            for (Object o1 : v) {
                                JSONObject object = (JSONObject) o1;
                                String minPrice = object.getString("minPrice");
                                String date = object.getString("tripdate");
                                String surplusNum = object.getString("surplus_num");
                                GroupTourPrice groupTourPrice = new GroupTourPrice();
                                groupTourPrice.setDate(date);
                                groupTourPrice.setAdtPrice(new BigDecimal(minPrice));
                                groupTourPrice.setAdtSellPrice(new BigDecimal(minPrice));
                                groupTourPrice.setAdtStock(Integer.parseInt(surplusNum));
                                if (StringUtils.isNotEmpty(chdPrice)) {
                                    if (chdPrice.equals("同价")) {
                                        groupTourPrice.setChdPrice(new BigDecimal(minPrice));
                                        groupTourPrice.setChdSellPrice(new BigDecimal(minPrice));
                                    } else {
                                        groupTourPrice.setChdPrice(new BigDecimal(chdPrice));
                                        groupTourPrice.setChdSellPrice(new BigDecimal(chdPrice));
                                    }
                                    groupTourPrice.setChdStock(Integer.parseInt(surplusNum));
                                }
                                groupTourPrices.add(groupTourPrice);
                            }
                        }
                    }
                }
            }
        }
        mealMPO.setGroupTourPrices(groupTourPrices);
    }

    @Override
    public String getDetail(String url, String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO, String activeDay) throws IOException {
        String chdPrice = "";
        Document document = Jsoup.connect(url).get();
        Elements highlightsEles = document.select("body > div.wrap_main > section > div.detail_main > div.pro_info.mar_bot10 > ul > li:nth-child(5) > p > strong");
        Element subTitle = document.selectFirst("span.sma_title");
        List<String> highlightsList = new ArrayList<>();
        if (subTitle != null && StringUtils.isNotEmpty(subTitle.text())) {
            highlightsList.add(subTitle.text().trim());
        }
        for (Element h : highlightsEles) {
            String text = h.text();
            if (StringUtils.isNotEmpty(text) && !text.equals("凯撒自营")) {
                highlightsList.add(text);
            }
        }
        if (highlightsList.size() > 4) {
            highlightsList.subList(0, 4);
        }
        mpo.setHighlights(highlightsList);
        Element oldPrice = document.selectFirst("#old_price");
        if (oldPrice != null) {
            if (oldPrice.text().contains("同价")) {
                chdPrice = "同价";
            } else {
                chdPrice = getNumber(oldPrice.text().trim());
            }
        }
        log.info("chdPrice:{}", chdPrice);
        Element element1 = document.selectFirst("body > div.wrap_main > section > div.detail_main > div.dataBox.mar_bot10 > div.dataListBox > ul > a > li.active > div");
        if (element1 != null && StringUtils.isEmpty(activeDay)) {
            activeDay = element1.text().split(" ")[0].trim();
        }
        log.info("activeDay:{}", activeDay);
        String endDay = document.selectFirst("li:has(span.endDate) > p > em").text().split(" ")[0].trim();
        int between = TimeUtil.daysBetween(TimeUtil.getDateByStr(endDay), TimeUtil.getDateByStr(activeDay));
        GroupTourProductPayInfo groupTourProductPayInfo = mpo.getGroupTourProductPayInfo();
        groupTourProductPayInfo.setBeforeBookDay(between);
        groupTourProductPayInfo.setConfirmType(1);
        groupTourProductPayInfo.setLatestBookTime("23:59");
        mpo.setGroupTourProductPayInfo(groupTourProductPayInfo);
        List<GroupTourTripInfo> groupTourTripInfos = new ArrayList<>();
        Elements lineInfo = document.select("body > div.wrap_main > section > div.detail_main > div.detail_infobox > div.detail_info > div.nav_cpts.tabsContent > div.info_linebox.mar_bot10");
        for (Element l : lineInfo) {
            String title = l.selectFirst("div.info_tjly > p.title > strong").text().trim();
            //详细行程
            Elements dayEles = l.select("div.info_tjly > div.eveyday_trip");
            if (dayEles != null && dayEles.size() > 0) {
                for (int i = 0; i < dayEles.size(); i++) {
                    Element d = dayEles.get(i);
                    String day = d.selectFirst("div.method > span.number_icon").text().trim();
                    String text = d.select("div.method > p > span").text().trim();
                    Element trafficEle = d.selectFirst("div.method > p > i");
                    if (i == 0) {
                        if (trafficEle != null && StringUtils.isEmpty(trafficEle.attr("class"))) {
                            mpo.setGoTraffic(TrafficEnum.AIR.getId());
                        } else if (trafficEle != null && StringUtils.isNotEmpty(trafficEle.attr("class"))) {
                            mpo.setGoTraffic(TrafficEnum.getCodeByName(trafficEle.attr("class")));
                        } else {
                            mpo.setGoTraffic(TrafficEnum.SELF.getId());
                        }
                        if (mpo.getGoTraffic().equals("")) {
                            mpo.setGoTraffic(TrafficEnum.SELF.getId());
                        }
                    }
                    if (trafficEle != null) {
                        String className = trafficEle.attr("class");
                        if (StringUtils.isEmpty(className)) {
                            mpo.setBackTraffice(TrafficEnum.AIR.getId());
                        } else {
                            mpo.setBackTraffice(TrafficEnum.getCodeByName(className));
                        }
                    }
                    Element jt = d.selectFirst("div.everyday_info > dl > dt.jt");
                    if (jt != null && mpo.getProductName().contains("专列")) {
                        mpo.setGoTraffic(TrafficEnum.TRAIN.getId());
                        mpo.setBackTraffice(TrafficEnum.TRAIN.getId());
                    }
                    Element titleTraffic = d.selectFirst("div.method > p > i");
                    GroupTourProductTripItem tripItem = new GroupTourProductTripItem();
                    if (titleTraffic != null) {
                        if (StringUtils.isEmpty(titleTraffic.attr("class"))) {
                            tripItem.setType("7");
                            tripItem.setSeetLevel("经济舱");
                        } else if (StringUtils.equals(titleTraffic.attr("class"), "train")) {
                            tripItem.setType("6");
                        } else if (StringUtils.equals(titleTraffic.attr("class"), "bus")) {
                            tripItem.setType("8");
                            tripItem.setCarType("汽车");
                            Elements elements = d.select("div.method > p > span");
                            tripItem.setPoiDesc("乘坐汽车从" + elements.get(0).text().trim() + "至" + elements.get(1).text().trim());
                        }
                        Elements cities = d.select("div.method > p > span");
                        String fromCity = cities.get(0).text().trim();
                        String toCity = cities.get(1).text().trim();
                        String fromCode = commonService.queryCityCodeByName(fromCity);
                        String toCode = commonService.queryCityCodeByName(toCity);
                        tripItem.setCityName(fromCity);
                        if (StringUtils.isNotEmpty(fromCode)) {
                            tripItem.setCityCode(fromCode);
                        }
                        tripItem.setArrCityName(toCity);
                        if (StringUtils.isNotEmpty(toCode)) {
                            tripItem.setArrCityCode(toCode);
                        }
                    } else {
                        if (jt != null && mpo.getProductName().contains("专列")) {
                            tripItem.setType("6");
                            tripItem.setTrafficNo(jt.nextElementSibling().selectFirst("span").text());
                        }
                    }
                    GroupTourTripInfo tourTripInfo = new GroupTourTripInfo();
                    tourTripInfo.setDay(Integer.parseInt(day));
                    tourTripInfo.setTitle(text.replaceAll(" ", "-"));
                    Elements infoEles = d.select("div.everyday_info > dl");
                    List<GroupTourProductTripItem> groupTourProductTripItems = new ArrayList<>();
                    if (StringUtils.isNotEmpty(tripItem.getType())) {
                        groupTourProductTripItems.add(tripItem);
                    }
                    for (Element info : infoEles) {
                        tripItem = new GroupTourProductTripItem();
                        String infoTitle = info.select("dt").text().trim();
                        String infoContent = info.select("dd > span").text().trim();
                        log.info("infoTitle:{}", infoTitle);
                        log.info("infoContent:{}", infoContent);
                        switch (infoTitle) {
                            case "餐食":
                                StringBuilder builder = new StringBuilder();
                                tripItem.setType("3");
                                tripItem.setCityName(getLastTitleCity(d));
                                String code = commonService.queryCityCodeByName(tripItem.getCityName());
                                if (StringUtils.isNotEmpty(code)) {
                                    tripItem.setCityCode(code);
                                }
                                Elements elements = info.select("dd > span");
                                int count = 0;
                                for (Element element : elements) {
                                    builder.append(element.text().trim()).append("\r\n").append("<br>");
                                    if (element.text().contains("自理") || element.text().contains("-")) {
                                        count++;
                                    }
                                }
                                if (count == 3) {
                                    tripItem.setCostInclude(0);
                                }
                                tripItem.setPoiDesc(String.valueOf(builder));
                                tripItem.setSubType("20");
                                groupTourProductTripItems.add(tripItem);
                                break;
                            case "住宿":
                                tripItem.setType("5");
                                if (infoContent.equals("火车上") || infoContent.equals("飞机上")) {
                                    tripItem.setSubType("2");
                                    tripItem.setPoiName("住在交通工具上");
                                } else if (infoContent.contains("自理")) {
                                    tripItem.setSubType("3");
                                } else {
                                    tripItem.setSubType("1");
                                    GroupTourHotel hotel = new GroupTourHotel();
                                    hotel.setHotelName(infoContent);
                                    hotel.setHotelId(hotel.getHotelName());
                                    hotel.setCityName(getLastTitleCity(d));
                                    tripItem.setCityName(hotel.getCityName());
                                    String c = commonService.queryCityCodeByName(hotel.getCityName());
                                    if (StringUtils.isNotEmpty(c)) {
                                        tripItem.setCityCode(c);
                                        hotel.setCity(c);
                                    }
                                    hotel.setRoomName("标间");
                                    tripItem.setGroupTourHotels(Lists.newArrayList(hotel));
                                }
                                groupTourProductTripItems.add(tripItem);
                                break;
                            case "游览景点":
                                String[] s = infoContent.split(" ");
                                for (String value : s) {
                                    tripItem = new GroupTourProductTripItem();
                                    tripItem.setType("1");
                                    tripItem.setPoiName(value);
                                    tripItem.setCityName(getLastTitleCity(d));
                                    String c = commonService.queryCityCodeByName(tripItem.getCityName());
                                    if (StringUtils.isNotEmpty(c)) {
                                        tripItem.setCityCode(c);
                                    }
                                    ScenicSpotMPO ssMpo = scenicSpotDao.getScenicSpotByNameAndAddress(value, "");
                                    if (ssMpo != null) {
                                        tripItem.setPoiId(ssMpo.getId());
                                        tripItem.setImages(ssMpo.getImages());
                                    } else {
                                        tripItem.setPoiId(tripItem.getPoiName());
                                    }
                                    groupTourProductTripItems.add(tripItem);
                                }
                                break;
                            case "行程安排：":
                                tripItem.setType("15");
                                tripItem.setPoiName("行程安排");
                                infoContent = infoContent.replaceAll("；", "；<br>")
                                        .replaceAll("。", "。<br>")
                                        .replaceAll("！", "！<br>")
                                        .replaceAll("？", "？<br>");
                                tripItem.setPoiDesc(infoContent);
                                groupTourProductTripItems.add(tripItem);
                                break;
                        }
                    }
                    tourTripInfo.setGroupTourProductTripItems(groupTourProductTripItems);
                    groupTourTripInfos.add(tourTripInfo);
                }
            }
            //线路特色
            Element element = l.selectFirst("div.info_tjly > div.des_text > pre > p");
            if (element != null) {
                String feature = element.text();
                if (StringUtils.isNotEmpty(feature)) {
                    String t = l.selectFirst("div.info_tjly > p.title").toString().replaceAll("<([a-zA-Z]+)[^>]*>", "<$1>");
                    String c = l.selectFirst("div.info_tjly > div.des_text").toString().replaceAll("<([a-zA-Z]+)[^>]*>", "<$1>").replaceAll("<pre>", "").replaceAll("</pre>", "");
                    mpo.setComputerDesc(t + replaceBr(c));
                }
            }
        }
        if (StringUtils.isEmpty(mpo.getBackTraffice())) {
            mpo.setBackTraffice(TrafficEnum.SELF.getId());
        }
        mealMPO.setGroupTourTripInfos(groupTourTripInfos);

        //自费项目
        Element element = document.selectFirst("body > div.wrap_main > section > div.detail_main > div.detail_infobox > div.detail_info > div.nav_fysm.tabsContent > div.info_linebox.mar_tb10 > div.info_zfxm");
        if (element != null) {
            String selfCost = element.select("ul > li > a").text().trim();
            if (StringUtils.isNotEmpty(selfCost)) {
                mealMPO.setSelfCost(selfCost);
            }
        }

        //价格说明
        /*
        String priceInfo = document.select("#model > div > div.price_des > ul.text_con > li").text();
        String text = document.select("#li_price_one > p > span").text();
        log.info("text:{}", text);
        log.info("priceInfo:{}", priceInfo);

         */

        //获取价格日历
        /*
        Elements dateList = document.select("body > div.wrap_main > section > div.detail_main > div.dataBox.mar_bot10 > div.dataListBox > ul > a");
        String more = dateList.get(dateList.size() - 1).selectFirst("li").text();
        if (StringUtils.equals(more, "更多")) {
            //获取价格日历
            getCalendars(dbId, mpo, mealMPO);
        } else {
            List<GroupTourPrice> groupTourPrices = new ArrayList<>();
            String chdPrice = document.select("#old_price").text().trim();
            chdPrice = getNumber(chdPrice);
            log.info("chdPrice:{}", chdPrice);
            for (Element element : dateList) {
                String date = element.select("li > div:nth-child(1)").text().trim();
                String price = element.select("li > div.priceBox").text().trim();
                price = getNumber(price);
                log.info("adtPrice:{}", price);
                GroupTourPrice groupTourPrice = new GroupTourPrice();
                groupTourPrice.setDate(date);
                if (StringUtils.isNotEmpty(price)) {
                    groupTourPrice.setAdtPrice(new BigDecimal(price));
                    groupTourPrice.setAdtSellPrice(new BigDecimal(price));
                }
                if (StringUtils.isNotEmpty(chdPrice)) {
                    groupTourPrice.setChdPrice(new BigDecimal(chdPrice));
                    groupTourPrice.setChdSellPrice(new BigDecimal(chdPrice));
                }
                groupTourPrices.add(groupTourPrice);
            }
            mealMPO.setGroupTourPrices(groupTourPrices);
        }

         */

        return chdPrice;
    }

    private String getLastTitleCity(Element e) {
        Elements elements = e.select("div.method > p > span");
        String[] split = elements.get(elements.size() - 1).text().split("-");
        return split[split.length - 1].trim();
    }

    @Override
    public void getFee(String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO) throws IOException {
        String url = format(Constant.FEE_URL, dbId);

        Document document = Jsoup.connect(url).get();
        String title = document.select("body > div.wrap_main > div.hear_all > span").text();
        List<DescInfo> bookNotices = new ArrayList<>();
        Elements feeInfoEles = document.select("div.detail_fysminfo");
        StringBuilder bookNotice = new StringBuilder();
        for (Element f : feeInfoEles) {
            String feeInfoTitle = f.select("p.title").text().trim();
            if (feeInfoTitle.contains("变更说明")) {
                Element descEle = f.selectFirst("div.fysm_con > dl > dd > pre > p");
                if (descEle != null) {
                    if (StringUtils.isNotEmpty(descEle.text().trim())) {
                        mpo.setRefundDesc(descEle.text().trim());
                    }
                }
            }
            Elements dl = f.select("div.fysm_con > dl");
            for (Element element : dl) {
                String subTitle = element.select("dt").text().trim();
                String subContent = element.select("dd > pre > p").text().trim();
                if (StringUtils.isNotEmpty(subTitle)) {
                    if (feeInfoTitle.equals("费用说明")) {
                        if (subTitle.contains("费用包含")) {
                            mealMPO.setConstInclude(subContent.replaceAll("\r\n","\r\n<br>"));
                        }
                        if (subTitle.contains("费用不包含")) {
                            mealMPO.setCostExclude(subContent.replaceAll("\r\n","\r\n<br>"));
                        }
                        DescInfo descInfo = new DescInfo();
                        descInfo.setTitle(feeInfoTitle);
                        descInfo.setContent(subContent);
                        bookNotices.add(descInfo);
                    }
                }
                if (StringUtils.isNotEmpty(subContent) && !feeInfoTitle.contains("费用说明")) {
                    bookNotice.append(feeInfoTitle).append("\r\n").append("<br>").append(subContent);
                }
            }
        }
        /*
        if (StringUtils.isNotEmpty(mpo.getRefundDesc())) {
            List<String> descList = getSubUtil(mpo.getRefundDesc(), "出发前(.*?)％；", 0);
            List<GroupTourRefundRule> groupTourRefundRules = new ArrayList<>();
            for (String s : descList) {
                List<String> day = getSubUtil(s, "前(.*?)日", 1);
                List<String> percent = getSubUtil(s, "的(.*?)％", 1);
                GroupTourRefundRule rule = new GroupTourRefundRule();
                rule.setType(0);
                if (ListUtils.isNotEmpty(day)) {
                    rule.setMinDay(Integer.parseInt(day.get(0)));
                }
                if (ListUtils.isNotEmpty(percent)) {
                    rule.setBuyersFee(Integer.parseInt(percent.get(0)));
                    groupTourRefundRules.add(rule);
                }
            }
            if (ListUtils.isNotEmpty(groupTourRefundRules)) {
                mpo.setGroupTourRefundRules(groupTourRefundRules);
            }
        }

         */
        mealMPO.setBookNotice(String.valueOf(bookNotice));
        mealMPO.setBookNotices(bookNotices);
    }

    @Override
    public void getCalendars(String dbId, GroupTourProductMPO mpo, GroupTourProductSetMealMPO mealMPO) throws IOException {
        List<GroupTourPrice> groupTourPrices = getCalendars(dbId);
        mealMPO.setGroupTourPrices(groupTourPrices);
    }





    @Override
    public void getDetail(String defaultUrl, String dbId) {
        String url = format(defaultUrl, dbId);
        try {
            Document document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<GroupTourPrice> getCalendars(String dbId) throws IOException {
        String url = format(Constant.FEE_DATE_URL, dbId);
        //屏蔽日志信息
//        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        //调用此方法时加载WebClient
        WebClient webClient = innerWebClient.webClient;

        //设置代理
//        ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
//        proxyConfig.setProxyHost("47.114.122.64");
//        proxyConfig.setProxyPort(8101);

        // 取消 JS 支持
        webClient.getOptions().setJavaScriptEnabled(true);
        // 取消 CSS 支持
        webClient.getOptions().setCssEnabled(false);
        HtmlPage page = null;
        List<GroupTourPrice> groupTourPrices = new ArrayList<>();
        Set<GroupTourPrice> groupTourPricesSet = new HashSet<>();
        try {
            // 获取指定网页实体
            page = (HtmlPage) webClient.getPage(url);
            //curMonth
            getCalendars(groupTourPricesSet, page, null);
            //nextMonth
            getCalendars(groupTourPricesSet, page, "/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3/div[2]");
            //prevMonth
            getCalendars(groupTourPricesSet, page, "/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3/div[1]");
            log.info("my-set: " + JSONObject.toJSONString(groupTourPrices));
            groupTourPrices.addAll(groupTourPricesSet);
            return groupTourPrices;
        } catch (IOException e) {
            throw e;
        }finally {
            webClient.close();
        }
    }

    private void getCalendars(Set<GroupTourPrice> resultSet, HtmlPage page, String monthXPath) throws IOException {
        if (monthXPath == null) {
            parseCalendars(page, resultSet);
            return;
        }
        while (true) {
            List<HtmlElement> nextMonth = page.getByXPath(monthXPath);
            HtmlElement element = nextMonth.get(0);
            String style = element.getAttribute("style");
            if (style.contains("display:none")) {
                break;
            }
            page = nextMonth.get(0).click();
            parseCalendars(page, resultSet);
        }
    }

    private void parseCalendars(HtmlPage page, Set<GroupTourPrice> resultSet) {
        HtmlElement month = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3").get(0);
        HtmlElement date = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/ul").get(0);
        List<HtmlElement> dates = date.getElementsByAttribute("li", "class", "hasData");
        for (HtmlElement d : dates) {
            HtmlElement myDate = d.getFirstByXPath("a/div[1]");
            HtmlElement myPrice = d.getFirstByXPath("a/div[3]");
            HtmlElement count = d.getFirstByXPath("a/div[2]");
            if (null == myPrice) {
                continue;
            }
            GroupTourPrice groupTourPrice = new GroupTourPrice();
            if (count != null) {
                String textContent = count.getTextContent();
                log.info(textContent);
                groupTourPrice.setAdtStock(Integer.parseInt(getNumber(count.getTextContent())));
            }
            groupTourPrice.setDate(month.getTextContent() + "月" + myDate.getTextContent());
            groupTourPrice.setAdtPrice(new BigDecimal(getNumber(myPrice.getTextContent())));
            resultSet.add(groupTourPrice);
            log.info("my-date: " + month.getTextContent() + "-" + myDate.getTextContent() + " my-price: " + myPrice.getTextContent());
        }
    }

    public static void main(String[] args) throws IOException {
        //todo list
/*
        try {
            Document document = Jsoup.connect(format(Constant.CAISSA_APP_LIST, 1)).get();
            String body = UnicodeConvertUtil.unicodeDecode(document.body().text());
            JSONObject jsonObject = JSONObject.parseObject(UnicodeConvertUtil.jsonString(body));
            if (jsonObject == null) {
                return;
            }
            JSONArray matches = jsonObject.getJSONArray("matches");
            if (matches == null || matches.size() < 1) {
                return;
            }
            log.info("matches: " + matches.size());
            for (Object o : matches) {
                JSONObject matchJson = (JSONObject) o;
                String sourceName = matchJson.getString("source_name");
                //不是自营的就跳过
                if (!sourceName.equals("team_list")) {
                    continue;
                }
                String productDbId = matchJson.getString("product_db_id");
                log.info(productDbId);
                String dbId = matchJson.getString("db_id");
                log.info(dbId);
                String productCode = matchJson.getString("product_code");
                log.info(productCode);

                String productName = matchJson.getString("product_name");
                log.info(productName);

                String teamName = matchJson.getString("team_name");
                log.info(teamName);

                String subTitle = matchJson.getString("sub_title");
                log.info(subTitle);

                String departureName = matchJson.getString("departure_name");
                log.info(departureName);

                String departure = matchJson.getString("departure");
                log.info(departure);

                String scheduleDays = matchJson.getString("schedule_days");
                log.info(scheduleDays);

                String scheduleNights = matchJson.getString("schedule_nights");
                log.info(scheduleNights);

                String allTripDate = matchJson.getJSONObject("stats_attrs").getString("all_trip_date");
                log.info(allTripDate);

                JSONArray pictures = matchJson.getJSONObject("attributes").getJSONArray("picture");
                String pictureLink = Constant.DEFAULT_PICTURE;
                if (pictures != null && pictures.size() > 0) {
                    JSONObject o1;
                    if (pictures.size() > 3) {
                        o1 = (JSONObject) pictures.get(2);
                    } else {
                        o1 = (JSONObject) pictures.get(0);
                    }
                    pictureLink = format(Constant.LIST_PICTURE_LINK, o1.getString("file_code"));
                }
                log.info(pictureLink);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

 */



        //todo detail


        GroupTourProductMPO mpo = new GroupTourProductMPO();
        mpo.setGroupTourProductPayInfo(new GroupTourProductPayInfo());
        GroupTourProductSetMealMPO mealMPO = new GroupTourProductSetMealMPO();

        //
        //c058196168854920b38cf404d5cef72a
        Document document = Jsoup.connect("http://m.caissa.com.cn/group/index/details?id=99015263219740aaaece22125a80900a").get();
        String chdPrice = "";
        Elements highlightsEles = document.select("body > div.wrap_main > section > div.detail_main > div.pro_info.mar_bot10 > ul > li:nth-child(5) > p > strong");
        Element subTitle = document.selectFirst("span.sma_title");
        List<String> highlightsList = new ArrayList<>();
        if (subTitle != null && StringUtils.isNotEmpty(subTitle.text())) {
            highlightsList.add(subTitle.text().trim());
        }
        for (Element h : highlightsEles) {
            String text = h.text();
            if (StringUtils.isNotEmpty(text) && !text.equals("凯撒自营")) {
                highlightsList.add(text);
            }
        }
        if (highlightsList.size() > 4) {
            highlightsList.subList(0, 4);
        }
        mpo.setHighlights(highlightsList);
        Element oldPrice = document.selectFirst("#old_price");
        if (oldPrice != null) {
            chdPrice = getNumber(oldPrice.text().trim());
        }
        log.info("chdPrice:{}", chdPrice);
        String activeDay = document.select("body > div.wrap_main > section > div.detail_main > div.dataBox.mar_bot10 > div.dataListBox > ul > a > li.active > div").text().split(" ")[0].trim();
        log.info("activeDay:{}", activeDay);
        if (StringUtils.isEmpty(activeDay)) {
            activeDay = "08-19";
        }
        String endDay = document.selectFirst("li:has(span.endDate) > p > em").text().split(" ")[0].trim();
        log.info("endDay:{}", endDay);
        int between = TimeUtil.daysBetween(TimeUtil.getDateByStr(endDay), TimeUtil.getDateByStr(activeDay));
        GroupTourProductPayInfo groupTourProductPayInfo = mpo.getGroupTourProductPayInfo();
        groupTourProductPayInfo.setBeforeBookDay(between);
        groupTourProductPayInfo.setConfirmType(1);
        groupTourProductPayInfo.setLatestBookTime("23:59");
        mpo.setGroupTourProductPayInfo(groupTourProductPayInfo);
        List<GroupTourTripInfo> groupTourTripInfos = new ArrayList<>();
        Elements lineInfo = document.select("body > div.wrap_main > section > div.detail_main > div.detail_infobox > div.detail_info > div.nav_cpts.tabsContent > div.info_linebox.mar_bot10");
        for (Element l : lineInfo) {
            String title = l.selectFirst("div.info_tjly > p.title > strong").text().trim();
            log.info("title:{}", title);
            //详细行程
            Elements dayEles = l.select("div.info_tjly > div.eveyday_trip");
            if (dayEles != null && dayEles.size() > 0) {
                for (int i = 0; i < dayEles.size(); i++) {
                    Element d = dayEles.get(i);
                    String day = d.selectFirst("div.method > span.number_icon").text().trim();
                    String text = d.select("div.method > p > span").text().trim();
                    Element trafficEle = d.selectFirst("div.method > p > i");
                    if (i == 0) {
                        if (trafficEle != null && StringUtils.isEmpty(trafficEle.attr("class"))) {
                            mpo.setGoTraffic(TrafficEnum.AIR.getId());
                        } else if (trafficEle != null && StringUtils.isNotEmpty(trafficEle.attr("class"))) {
                            mpo.setGoTraffic(TrafficEnum.getCodeByName(trafficEle.attr("class")));
                        } else {
                            mpo.setGoTraffic(TrafficEnum.SELF.getId());
                        }
                        if (mpo.getGoTraffic().equals("")) {
                            mpo.setGoTraffic(TrafficEnum.SELF.getId());
                        }
                    }
                    if (trafficEle != null) {
                        String className = trafficEle.attr("class");
                        if (StringUtils.isEmpty(className)) {
                            mpo.setBackTraffice(TrafficEnum.AIR.getId());
                        } else {
                            mpo.setBackTraffice(TrafficEnum.getCodeByName(className));
                        }
                    }
                    Element jt = d.selectFirst("div.everyday_info > dl > dt.jt");
                    if (jt != null && mpo.getProductName().contains("专列")) {
                        mpo.setGoTraffic(TrafficEnum.TRAIN.getId());
                        mpo.setBackTraffice(TrafficEnum.TRAIN.getId());
                    }
                    Element titleTraffic = d.selectFirst("div.method > p > i");
                    GroupTourProductTripItem tripItem = new GroupTourProductTripItem();
                    if (titleTraffic != null) {
                        if (StringUtils.isEmpty(titleTraffic.attr("class"))) {
                            tripItem.setType("7");
                            tripItem.setSeetLevel("经济舱");
                        } else if (StringUtils.equals(titleTraffic.attr("class"), "train")) {
                            tripItem.setType("6");
                        } else if (StringUtils.equals(titleTraffic.attr("class"), "bus")) {
                            tripItem.setType("8");
                            tripItem.setCarType("汽车");
                            Elements elements = d.select("div.method > p > span");
                            tripItem.setPoiDesc("乘坐汽车从" + elements.get(0).text().trim() + "至" + elements.get(1).text().trim());
                        }
                        Elements cities = d.select("div.method > p > span");
                        String fromCity = cities.get(0).text().trim();
                        String toCity = cities.get(1).text().trim();
                        String fromCode = "北京";
                        String toCode = "北京";
                        tripItem.setCityName(fromCity);
                        if (StringUtils.isNotEmpty(fromCode)) {
                            tripItem.setCityCode(fromCode);
                        }
                        tripItem.setArrCityName(toCity);
                        if (StringUtils.isNotEmpty(toCode)) {
                            tripItem.setArrCityCode(toCode);
                        }
                    } else {
                        if (jt != null && mpo.getProductName().contains("专列")) {
                            tripItem.setType("6");
                            tripItem.setTrafficNo(jt.nextElementSibling().selectFirst("span").text());
                        }
                    }
                    log.info("day:{}", day);
                    log.info("text:{}", text);
                    GroupTourTripInfo tourTripInfo = new GroupTourTripInfo();
                    tourTripInfo.setDay(Integer.parseInt(day));
                    tourTripInfo.setTitle(text);
                    Elements infoEles = d.select("div.everyday_info > dl");
                    List<GroupTourProductTripItem> groupTourProductTripItems = new ArrayList<>();
                    if (StringUtils.isNotEmpty(tripItem.getType())) {
                        groupTourProductTripItems.add(tripItem);
                    }
                    for (Element info : infoEles) {
                        tripItem = new GroupTourProductTripItem();
                        String infoTitle = info.select("dt").text().trim();
                        String infoContent = info.select("dd > span").text().trim();
                        log.info("infoTitle:{}", infoTitle);
                        log.info("infoContent:{}", infoContent.replaceAll("；", "；<br>")
                                .replaceAll("。", "。<br>")
                                .replaceAll("！", "！<>")
                                .replaceAll("？", "<>"));
                        switch (infoTitle) {
                            case "餐食":
                                StringBuilder builder = new StringBuilder();
                                tripItem.setType("3");
                                tripItem.setCityName("bj");
                                String code = "1";
                                if (StringUtils.isNotEmpty(code)) {
                                    tripItem.setCityCode(code);
                                }
                                Elements elements = info.select("dd > span");
                                int count = 0;
                                for (Element element : elements) {
                                    builder.append(element.text().trim()).append("\r\n");
                                    if (element.text().contains("自理") || element.text().contains("-")) {
                                        count++;
                                    }
                                }
                                if (count == 3) {
                                    tripItem.setCostInclude(0);
                                }
                                tripItem.setPoiDesc(String.valueOf(builder));
                                tripItem.setSubType("20");
                                groupTourProductTripItems.add(tripItem);
                                break;
                            case "住宿":
                                tripItem.setType("5");
                                if (infoContent.equals("火车上") || infoContent.equals("飞机上")) {
                                    tripItem.setSubType("2");
                                } else if (infoContent.contains("自理")) {
                                    tripItem.setSubType("3");
                                } else {
                                    tripItem.setSubType("1");
                                    GroupTourHotel hotel = new GroupTourHotel();
                                    hotel.setHotelName(infoContent);
                                    hotel.setHotelId(hotel.getHotelName());
                                    hotel.setCityName("bj");
                                    tripItem.setCityName(hotel.getCityName());
                                    String c = "1";
                                    if (StringUtils.isNotEmpty(c)) {
                                        tripItem.setCityCode(c);
                                        hotel.setCity(c);
                                    }
                                    hotel.setRoomName("标间");
                                    tripItem.setGroupTourHotels(Lists.newArrayList(hotel));
                                }
                                groupTourProductTripItems.add(tripItem);
                                break;
                            case "游览景点":
                                String[] s = infoContent.split(" ");
                                for (String value : s) {
                                    tripItem = new GroupTourProductTripItem();
                                    tripItem.setType("1");
                                    tripItem.setPoiName(value);
                                    tripItem.setCityName("bj");
                                    String c = "1";
                                    if (StringUtils.isNotEmpty(c)) {
                                        tripItem.setCityCode(c);
                                    }
//                                    ScenicSpotMPO ssMpo = scenicSpotDao.getScenicSpotByNameAndAddress(value, "");
//                                    if (ssMpo != null) {
//                                        tripItem.setPoiId(ssMpo.getId());
//                                        tripItem.setImages(ssMpo.getImages());
//                                    } else {
//                                        tripItem.setPoiId(tripItem.getPoiName());
//                                    }
                                    groupTourProductTripItems.add(tripItem);
                                }
                                break;
                            case "行程安排：":
                                tripItem.setType("15");
                                tripItem.setPoiDesc(infoContent);
                                groupTourProductTripItems.add(tripItem);
                                break;
                        }
                    }
                    tourTripInfo.setGroupTourProductTripItems(groupTourProductTripItems);
                    groupTourTripInfos.add(tourTripInfo);
                }
            }
            //线路特色
            Element element = l.selectFirst("div.info_tjly > div.des_text > pre > p");
            if (element != null) {
                String feature = element.text();
                if (StringUtils.isNotEmpty(feature)) {
                    String t = l.selectFirst("div.info_tjly > p.title").toString().replaceAll("<([a-zA-Z]+)[^>]*>", "<$1>");
                    log.info("elemetnt:{}", t);
                    String c = l.selectFirst("div.info_tjly > div.des_text").toString().replaceAll("<([a-zA-Z]+)[^>]*>", "<$1>");
                    log.info("feature:{}", c);
                    mpo.setComputerDesc(t + replaceBr(c));
                }
            }
        }
        if (StringUtils.isEmpty(mpo.getBackTraffice())) {
            mpo.setBackTraffice(TrafficEnum.SELF.getId());
        }
        mealMPO.setGroupTourTripInfos(groupTourTripInfos);
        Element element = document.selectFirst("body > div.wrap_main > section > div.detail_main > div.detail_infobox > div.detail_info > div.nav_fysm.tabsContent > div.info_linebox.mar_tb10 > div.info_zfxm");
        if (element != null) {
            String trim = element.select("ul > li > a").text().trim();
            System.out.println("selfCost: " + trim.replaceAll(" ", "<br>"));
        }

        //todo fee
        /*
        GroupTourProductMPO mpo = new GroupTourProductMPO();
        GroupTourProductSetMealMPO mealMPO = new GroupTourProductSetMealMPO();

        Document document = Jsoup.connect("http://m.caissa.com.cn/group/Index/details_fyyd?id=c058196168854920b38cf404d5cef72a&is_sale=0").get();
        String title = document.select("body > div.wrap_main > div.hear_all > span").text();
        log.info("title: " + title);
        List<DescInfo> bookNotices = new ArrayList<>();
        Elements feeInfoEles = document.select("div.detail_fysminfo");
        StringBuilder bookNotice = new StringBuilder();
        for (Element f : feeInfoEles) {
            String feeInfoTitle = f.select("p.title").text().trim();
            log.info("feeInfoTitle: " + feeInfoTitle);
            if (feeInfoTitle.contains("变更说明")) {
                Element descEle = f.selectFirst("div.fysm_con > dl > dd > pre > p");
                if (descEle != null) {
                    log.info("refundDesc:{}", descEle.text().trim());
                    if (StringUtils.isNotEmpty(descEle.text().trim())) {
                        mpo.setRefundDesc(descEle.text().trim());
                    }
                }
            }
            Elements dl = f.select("div.fysm_con > dl");
            for (Element element : dl) {
                String subTitle = element.select("dt").text().trim();
                String subContent = element.select("dd > pre > p").text().trim();
                if (StringUtils.isNotEmpty(subTitle)) {
                    log.info("subTitle: " + subTitle);
                    if (feeInfoTitle.equals("费用说明")) {
                        if (subTitle.contains("费用包含")) {
                            mealMPO.setConstInclude(subContent);
                        }
                        if (subTitle.contains("费用不包含")) {
                            mealMPO.setCostExclude(subContent);
                        }
                        DescInfo descInfo = new DescInfo();
                        descInfo.setTitle(feeInfoTitle);
                        descInfo.setContent(subContent);
                        bookNotices.add(descInfo);
                    }
                }
                if (StringUtils.isNotEmpty(subContent) && !feeInfoTitle.contains("费用说明")) {
                    bookNotice.append(feeInfoTitle).append("\r\n").append(subContent);
                }
                log.info("subContent: " + subContent);
            }
        }
        if (StringUtils.isNotEmpty(mpo.getRefundDesc())) {
            List<String> descList = getSubUtil(mpo.getRefundDesc(), "出发前(.*?)％；", 0);
            List<GroupTourRefundRule> groupTourRefundRules = new ArrayList<>();
            for (String s : descList) {
                List<String> day = getSubUtil(s, "前(.*?)日", 1);
                List<String> percent = getSubUtil(s, "的(.*?)％", 1);
                GroupTourRefundRule rule = new GroupTourRefundRule();
                rule.setType(0);
                if (ListUtils.isNotEmpty(day)) {
                    System.out.println(Integer.parseInt(day.get(0)));
                    rule.setMinDay(Integer.parseInt(day.get(0)));
                }
                if (ListUtils.isNotEmpty(percent)) {
                    System.out.println(Integer.parseInt(percent.get(0)));
                    rule.setBuyersFee(Integer.parseInt(percent.get(0)));
                    groupTourRefundRules.add(rule);
                }
            }
            if (ListUtils.isNotEmpty(groupTourRefundRules)) {
                mpo.setGroupTourRefundRules(groupTourRefundRules);
            }
            log.info("groupTourRefundRules:{}", mpo.getGroupTourRefundRules());
        }
        mealMPO.setBookNotice(String.valueOf(bookNotice));
        mealMPO.setBookNotices(bookNotices);

         */



        //todo getCalendars
        /*
        Document document = Jsoup.connect("http://group.caissa.com.cn/detailInfo/ajaxCalendar/?v=1626850407146&tdate=2021-07-26+00%3A00%3A00&product_db_id=1eb2c20baf114365a3f02bcf33e9cbc9&schedule_days=5&schedule_nights=4&departure=101001000&salechannel=").get();
        List<PriceCalendarDto> result = new ArrayList<>();
        if (null != document) {
            String text = document.body().text();
            JSONObject jsonObject = JSONObject.parseObject(text);
            if (jsonObject != null) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data != null) {
                    Collection<Object> values = data.values();
                    if (!CollectionUtils.isEmpty(values)) {
                        for (Object o : values) {
                            JSONArray v = (JSONArray) o;
                            for (Object o1 : v) {
                                JSONObject object = (JSONObject) o1;
                                String minPrice = object.getString("minPrice");
                                String date = object.getString("tripdate");
                                String surplusNum = object.getString("surplus_num");
                                PriceCalendarDto priceCalendarDto = new PriceCalendarDto();
                                priceCalendarDto.setPrice(minPrice);
                                priceCalendarDto.setDate(date);
                                priceCalendarDto.setSurplusNum(surplusNum);
                                result.add(priceCalendarDto);
                            }
                        }
                    }
                }
            }
        }
        log.info(JSONObject.toJSONString(result));
         */

        /*
        String url = format(Constant.FEE_DATE_URL, "86602e437c754b4182c27ac1023eacdb");
        //屏蔽日志信息
//        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        //调用此方法时加载WebClient
        WebClient webClient = new WebClient();

        //设置代理
//        ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
//        proxyConfig.setProxyHost("47.114.122.64");
//        proxyConfig.setProxyPort(8101);

        // 取消 JS 支持
        webClient.getOptions().setJavaScriptEnabled(true);
        // 取消 CSS 支持
        webClient.getOptions().setCssEnabled(false);
        HtmlPage page = null;
        HashSet<Map<String, Map<String, String>>> set = new HashSet<>();

        try {
            // 获取指定网页实体
            page = (HtmlPage) webClient.getPage(url);
            //nextMonth
            while (true) {
                List<HtmlElement> nextMonth = page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3/div[2]");
                HtmlElement element = nextMonth.get(0);
                String style = element.getAttribute("style");
                if (style.contains("display:none")) {
                    break;
                }
                Map<String, Map<String, String>> outerMap = new HashMap<>();
                Map<String, String> map = new HashMap<>(32);
                page = nextMonth.get(0).click();
                HtmlElement month = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3").get(0);
                HtmlElement date = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/ul").get(0);
                List<HtmlElement> dates = date.getElementsByAttribute("li", "class", "hasData");
                for (HtmlElement d : dates) {
                    HtmlElement myDate = d.getFirstByXPath("a/div[1]");
                    HtmlElement myPrice = d.getFirstByXPath("a/div[3]");
                    if (null == myPrice) {
                        continue;
                    }
                    map.put(myDate.getTextContent(), myPrice.getTextContent());
//                log.info("my-date: " + month.getTextContent() + "-" + myDate.getTextContent() + " my-price: " + myPrice.getTextContent());
                }
                if (map.size() > 0) {
                    outerMap.put(month.getTextContent(), map);
                }
                set.add(outerMap);
            }
            //prevMonth
            while (true) {
                List<HtmlElement> nextMonth = page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3/div[1]");
                HtmlElement element = nextMonth.get(0);
                String style = element.getAttribute("style");
                if (style.contains("display:none")) {
                    break;
                }
                Map<String, Map<String, String>> outerMap = new HashMap<>();
                Map<String, String> map = new HashMap<>(32);
                page = nextMonth.get(0).click();
                HtmlElement month = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/h3").get(0);
                HtmlElement date = (HtmlElement) page.getByXPath("/html/body/div[1]/section/div[1]/div[1]/div/div[2]/ul").get(0);
                List<HtmlElement> dates = date.getElementsByAttribute("li", "class", "hasData");
                for (HtmlElement d : dates) {
                    HtmlElement myDate = d.getFirstByXPath("a/div[1]");
                    HtmlElement myPrice = d.getFirstByXPath("a/div[3]");
                    if (null == myPrice) {
                        continue;
                    }
                    map.put(myDate.getTextContent(), myPrice.getTextContent());
//                log.info("my-date: " + month.getTextContent() + "-" + myDate.getTextContent() + " my-price: " + myPrice.getTextContent());
                }
                if (map.size() > 0) {
                    outerMap.put(month.getTextContent(), map);
                }
                set.add(outerMap);
            }
            log.info("my-set: " + JSONObject.toJSONString(set));
        } catch (IOException e) {
            e.printStackTrace();
        }
        webClient.close();

         */
    }
}
