package com.huoli.trip.supplier.web.tag.controller;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.web.caissa.util.RedisQueue;
import com.huoli.trip.supplier.web.tag.constant.Const;
import com.huoli.trip.supplier.web.tag.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/run/{key}")
    public BaseResponse start(@PathVariable String key) {
        if (StringUtils.isNotEmpty(RedisQueue.getValueByKey(Const.TAG_RUN))) {
            log.info("景点tags抓取正在进行，请勿重复进行");
            return BaseResponse.withFail(-1, "景点tags抓取正在进行，请勿重复进行");
        }
        try {
            RedisQueue.set(Const.TAG_RUN, "1");
            switch (key) {
                case "tag":
                    tagService.getTag();
                    break;
                case "search":
                    tagService.getSearch();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.withFail(-1, "景点tags抓取报错");
        } finally {
            RedisQueue.deleteKey(Const.TAG_RUN);
        }
        return BaseResponse.withSuccess();
    }

    @GetMapping("/city/run/{key}")
    public BaseResponse cityStart(@PathVariable String key) {
        if (StringUtils.isNotEmpty(RedisQueue.getValueByKey(Const.CITY_RUN))) {
            log.info("景点tagsCity抓取正在进行，请勿重复进行");
            return BaseResponse.withFail(-1, "景点tagsCity抓取正在进行，请勿重复进行");
        }
        try {
            RedisQueue.set(Const.CITY_RUN, "1");
            switch (key) {
                case "cTrip-city-tag":
                    tagService.getCity(Const.CTRIP_CITY_TAG);
                    break;
                case "cTrip-city-tag-map":
                    tagService.getCity(Const.CTRIP_CITY_TAG_MAP);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.withFail(-1, "景点tagsCity抓取报错");
        } finally {
            RedisQueue.deleteKey(Const.CITY_RUN);
        }
        return BaseResponse.withSuccess();
    }

    @GetMapping("/delKey/{key}")
    public BaseResponse delCityRun(@PathVariable String key) {
        switch (key) {
            case "cTrip-tag-city-run":
                RedisQueue.deleteKey(Const.CITY_RUN);
                break;
            case "cTrip-tag-run":
                RedisQueue.deleteKey(Const.TAG_RUN);
                break;
            case "cTrip-city-tag":
                RedisQueue.deleteKey(Const.CTRIP_CITY_TAG);
                break;
        }
        return BaseResponse.withSuccess();
    }

    @GetMapping("/len/{key}")
    public BaseResponse len(@PathVariable String key ) {
        Long len = -1L;
        switch (key) {
            case "cTrip-city-ta":
                len = RedisQueue.lLen(Const.CTRIP_CITY_TAG);
                break;
            case "cTrip-matchLikeList":
                len = RedisQueue.lLen(Const.MATCH_LIKE_LIST);
                break;
            case "cTrip-noMatchList":
                len = RedisQueue.lLen(Const.NO_MATCH_LIST);
                break;
            case "cTrip-noMatchCityList":
                len = RedisQueue.lLen(Const.NO_MATCH_CITY);
                break;
        }
        return BaseResponse.withSuccess(len);
    }

    @GetMapping("/count/{key}")
    public BaseResponse count(@PathVariable String key) {
        String count = "-1";
        switch (key) {
            case "cTrip-matchCount":
                count = RedisQueue.getValueByKey(Const.MATCH_COUNT);
                break;
            case "cTrip-matchLikeCount":
                count = RedisQueue.getValueByKey(Const.MATCH_LIKE_COUNT);
                break;
            case "cTrip-noMatchCount":
                count = RedisQueue.getValueByKey(Const.NO_MATCH_COUNT);
                break;
            case "cTrip-hadCount":
                count = RedisQueue.getValueByKey(Const.HAD_COUNT);
                break;
        }
        return BaseResponse.withSuccess(count);
    }

}
