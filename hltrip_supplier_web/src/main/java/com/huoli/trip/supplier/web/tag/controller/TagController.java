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

    @GetMapping("/run")
    public BaseResponse start() {
        if (StringUtils.isNotEmpty(RedisQueue.getValueByKey(Const.TAG_RUN))) {
            log.info("景点tags抓取正在进行，请勿重复进行");
            return BaseResponse.withFail(-1, "景点tags抓取正在进行，请勿重复进行");
        }
        try {
            RedisQueue.set(Const.TAG_RUN, "1");
            tagService.getTag();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.withFail(-1, "景点tags抓取报错");
        } finally {
            RedisQueue.deleteKey(Const.TAG_RUN);
        }
        return BaseResponse.withSuccess();
    }

    @GetMapping("/city/run")
    public BaseResponse cityStart() {
        if (StringUtils.isNotEmpty(RedisQueue.getValueByKey(Const.CITY_RUN))) {
            log.info("景点tagsCity抓取正在进行，请勿重复进行");
            return BaseResponse.withFail(-1, "景点tagsCity抓取正在进行，请勿重复进行");
        }
        try {
            RedisQueue.set(Const.CITY_RUN, "1");
            tagService.getCity();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.withFail(-1, "景点tagsCity抓取报错");
        } finally {
            RedisQueue.deleteKey(Const.CITY_RUN);
        }
        return BaseResponse.withSuccess();
    }

    @GetMapping("/delCityRun")
    public BaseResponse delCityRun() {
        RedisQueue.deleteKey(Const.CITY_RUN);
        return BaseResponse.withSuccess();
    }

    @GetMapping("/delTagRun")
    public BaseResponse delTagRun() {
        RedisQueue.deleteKey(Const.TAG_RUN);
        return BaseResponse.withSuccess();
    }

    @GetMapping("/delTagCity")
    public BaseResponse delTagCity() {
        RedisQueue.deleteKey(Const.CTRIP_CITY_TAG);
        return BaseResponse.withSuccess();
    }

    @GetMapping("/len")
    public BaseResponse len() {
        Long aLong = RedisQueue.lLen(Const.CTRIP_CITY_TAG);
        return BaseResponse.withSuccess(aLong);
    }

    @GetMapping("/count")
    public BaseResponse count() {
        String valueByKey = RedisQueue.getValueByKey(Const.MATCH_COUNT);
        return BaseResponse.withSuccess(valueByKey);
    }

    @GetMapping("/hadCount")
    public BaseResponse hadCount() {
        String valueByKey = RedisQueue.getValueByKey(Const.HAD_COUNT);
        return BaseResponse.withSuccess(valueByKey);
    }

}
