package com.huoli.trip.supplier.web.tag.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.caissa.util.RedisQueue;
import com.huoli.trip.supplier.web.dao.ScenicSpotDao;
import com.huoli.trip.supplier.web.tag.bean.CityMap;
import com.huoli.trip.supplier.web.tag.constant.Const;
import com.huoli.trip.supplier.web.tag.service.TagService;
import com.huoli.trip.supplier.web.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Override
    public void getCity() {
        String req = "{\"head\":{\"pageInfo\":{\"page\":\"\",\"hybrid\":\"\",\"prevpage\":\"\",\"sid\":\"\",\"pvid\":\"\",\"clientcode\":\"\",\"vid\":\"\"},\"cid\":\"09031168416425734161\",\"vid\":\"1628580369777.49xcd1\",\"union\":{\"sid\":\"155952\",\"aid\":\"4897\",\"ouid\":\"index\"},\"awakenUnion\":{\"isDoingGetUnion\":true},\"url\":\"https://huodong.ctrip.com/things-to-do/list?pagetype=city&citytype=dt&id=7&name=%E8%A5%BF%E5%AE%89&pshowcode=Ticket2\",\"referrer\":\"https://huodong.ctrip.com/things-to-do/list?pagetype=city&citytype=dt&id=1&name=%E5%8C%97%E4%BA%AC&pshowcode=Ticket2\",\"language\":\"zh-CN\",\"currency\":\"CNY\",\"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36\",\"cookie\":\"Session=smartlinkcode=U130026&smartlinklanguage=zh&SmartLinkKeyWord=&SmartLinkQuary=&SmartLinkHost=; MKT_CKID=1628580322104.y4eu6.hkbp; _ga=GA1.2.673153062.1628580323; _gid=GA1.2.332479040.1628580323; GUID=09031168416425734161; _RF1=111.204.31.58; _RSG=cP4jEEiTp33CMsRGvLEqdA; _RDG=285fb3c3167a712c433988d9e94988abab; _RGUID=30f376c3-8eb4-4666-a9f4-395e506f658c; ibulanguage=CN; ibulocale=zh_cn; cookiePricesDisplayed=CNY; nfes_isSupportWebP=1; appFloatCnt=5; FlightIntl=Search=[%22BJS|%E5%8C%97%E4%BA%AC(BJS)|1|BJS|480%22%2C%22BKK|%E6%9B%BC%E8%B0%B7(BKK)|359|BKK|420%22%2C%222021-08-13%22]; MKT_CKID_LMT=1628674546830; __zpspc=9.4.1628674546.1628674546.1%232%7Cwww.baidu.com%7C%7C%7C%25E6%2590%25BA%25E7%25A8%258B%7C%23; _jzqco=%7C%7C%7C%7C1628674547060%7C1.430884893.1628580322097.1628650440604.1628674546828.1628650440604.1628674546828.undefined.0.0.8.8; Corp_ResLang=zh-cn; ctm_ref=ppc005; _bfi=p1%3D10650038368%26p2%3D10650038368%26v1%3D44%26v2%3D43; MKT_Pagesource=H5; Union=OUID=index&AllianceID=4897&SID=155952&SourceID=&AppID=&OpenID=&exmktID=&createtime=1628740362&Expires=1629345162141; MKT_OrderClick=ASID=4897155952&AID=4897&CSID=155952&OUID=index&CT=1628740362143&CURL=https%3A%2F%2Fm.ctrip.com%2Fwebapp%2Fthings-to-do%2Flist%3Fpagetype%3Dcity%26citytype%3Ddt%26pshowcode%3DTicket2%26id%3D2%26name%3D%25E4%25B8%258A%25E6%25B5%25B7%26ctm_ref%3Dvactang_page_8349%26sid%3D155952%26allianceid%3D4897%26ouid%3Dindex&VAL={\\\\\\\"h5_vid\\\\\\\":\\\\\\\"1628580369777.49xcd1\\\\\\\"}; U_TICKET_SELECTED_DISTRICT_CITY=%7B%22value%22%3A%7B%22districtid%22%3A%227%22%2C%22districtname%22%3A%22%E8%A5%BF%E5%AE%89%22%2C%22isOversea%22%3Anull%7D%2C%22createTime%22%3A1628745182221%2C%22updateDate%22%3A1628745182221%7D; _bfa=1.1628580369777.49xcd1.1.1628740362606.1628745182284.8.51.214062; _bfs=1.1\",\"isMiniProgram\":false,\"isInMini\":false,\"width\":1440,\"height\":789,\"traceId\":\"16287451804608098\",\"offset\":{\"height\":789,\"width\":652},\"isBot\":false,\"isInApp\":false,\"platform\":\"Online\",\"enviroment\":\"PROD\",\"channel\":\"ctrip\",\"log_version\":\"2021-04-15 07:53:08\",\"syscode\":\"09\",\"pageId\":\"10650038368\",\"netState\":\"4G\",\"randomId\":2},\"source\":0,\"cityId\":\"7\",\"type\":0}\n";
        String res = HttpUtil.doPost("https://m.ctrip.com/restapi/soa2/14580/json/cityList", req);
        if (StringUtils.isNotEmpty(res)) {
            JSONObject body = (JSONObject) JSONObject.parse(res);
            JSONObject data = body.getJSONObject("data");
            if (data != null) {
                JSONObject domesticcity = data.getJSONObject("domesticcity");
                if (domesticcity != null) {
                    JSONArray outerCities = domesticcity.getJSONArray("cities");
                    if (outerCities != null && outerCities.size() > 0) {
                        for (Object o : outerCities) {
                            JSONObject city = (JSONObject) o;
                            JSONArray innerCities = city.getJSONArray("cities");
                            if (innerCities != null && innerCities.size() > 0) {
                                for (Object object : innerCities) {
                                    JSONObject innerCity = (JSONObject) object;
                                    CityMap cityMap = new CityMap();
                                    cityMap.setCityName(innerCity.getString("name"));
                                    cityMap.setCityId(innerCity.getInteger("id"));
                                    RedisQueue.lLeftPush(Const.CTRIP_CITY_TAG, JSONObject.toJSONString(cityMap));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void getTag() {
        while (true) {
            CityMap cityMap = JSONObject.parseObject(RedisQueue.lLeftPop(Const.CTRIP_CITY_TAG), CityMap.class);
            log.info("cityMap:{}", JSONObject.toJSONString(cityMap));
            if (cityMap == null) {
                break;
            }
            int total = doGetTag(1, cityMap);
            if (total != 0) {
                int page = total / 20 + 1;
                if (page == 1) {
                    continue;
                }
                for (int i = 2; i <= page; i++) {
                    doGetTag(i, cityMap);
                }
            }
        }
    }

    private int doGetTag(int page, CityMap cityMap) {
        log.info("更新景点tags中，cityId:{}，cityName:{}，page:{}", cityMap.getCityId(), cityMap.getCityName(), page);
        String req = buildTagReq(page, String.valueOf(cityMap.getCityId()));
        String res = HttpUtil.doPost("https://m.ctrip.com/restapi/soa2/20684/json/productSearch", req);
        if (StringUtils.isNotEmpty(res)) {
            JSONObject body = (JSONObject) JSONObject.parse(res);
            if (body != null) {
                Integer total = body.getInteger("total");
                parseTag(body, cityMap.getCityName());
                return total;
            }
        }
        return 0;
    }

    private void parseTag(JSONObject body, String cityName) {
        JSONArray products = body.getJSONArray("products");
        if (products == null || products.size() == 0) {
            return;
        }
        for (Object pro : products) {
            JSONObject product = (JSONObject) pro;
            JSONObject basicInfo = product.getJSONObject("basicInfo");
            if (basicInfo == null) {
                continue;
            }
            String name = basicInfo.getString("name");
            JSONArray tagGroups = product.getJSONArray("tagGroups");
            List<String> tagNames = new ArrayList<>();
            if (tagGroups != null && tagGroups.size() != 0) {
                for (Object tagGroup : tagGroups) {
                    JSONObject tag = (JSONObject) tagGroup;
                    if (tag.getString("groupCode").equals("CATEGORY_THEME_TAG")) {
                        JSONArray tags = tag.getJSONArray("tags");
                        for (Object t : tags) {
                            JSONObject jsonObject = (JSONObject) t;
                            tagNames.add(jsonObject.getString("tagName"));
                        }
                    }
                }
            }
            //获取库中的景点
            ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getByCityAndName(cityName, name);
            log.info("更新景点tags失败，库中没有该景点，cityName:{}，name:{}", cityName, name);
            if (scenicSpotMPO != null && tagNames.size() > 0) {
                if (ListUtils.isEmpty(scenicSpotMPO.getTages())) {
                    scenicSpotMPO.setTages(tagNames);
                    scenicSpotDao.updateTagsById(scenicSpotMPO.getTages(), scenicSpotMPO.getId());
                    log.info("更新景点tags成功，id:{}，cityName:{}，name:{}，tags:{}", scenicSpotMPO.getId(), cityName, name, JSONObject.toJSONString(tagNames));
                    RedisQueue.incrBy(Const.MATCH_COUNT, 1);
                } else {
                    RedisQueue.incrBy(Const.HAD_COUNT, 1);
                }
            }
        }
    }

    private String buildTagReq(Integer page, String cityId) {
        return String.format(Const.req, page, cityId);
    }
}
