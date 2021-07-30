package com.huoli.trip.supplier.web.caissa.controller;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.web.caissa.constant.Constant;
import com.huoli.trip.supplier.web.caissa.service.ParseService;
import com.huoli.trip.supplier.web.caissa.util.RedisQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/caissa")
@Slf4j
public class SpiderController {

    @Autowired
    private ParseService parseService;

    @GetMapping("/run/{start}/{end}")
    public BaseResponse start(@PathVariable("start") Integer start, @PathVariable("end") Integer end) {
        if (StringUtils.isNotEmpty(RedisQueue.getValueByKey(Constant.CAISSA_RUN))) {
            log.info("caissa抓取正在进行，请勿重复进行");
            return BaseResponse.withFail(-1, "caissa抓取正在进行，请勿重复进行");
        }
        try {
            RedisQueue.set(Constant.CAISSA_RUN, "1");
            do {
                log.info("caissaSpiderPage:{}", start);
                try {
                    parseService.getList(String.format(Constant.CAISSA_APP_LIST, start));
                } catch (Exception e) {
                    log.info("errorCaissaSpiderPage:{}", start);
                    e.printStackTrace();
                }
                start = start + 1;
            } while (start < end);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            RedisQueue.deleteKey(Constant.CAISSA_RUN);
        }
        return BaseResponse.withSuccess();
    }
}
