package com.huoli.trip.supplier.web.caissa.controller;

import com.huoli.trip.supplier.web.caissa.constant.Constant;
import com.huoli.trip.supplier.web.caissa.service.ParseService;
import lombok.extern.slf4j.Slf4j;
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
    public void start(@PathVariable("start") Integer start, @PathVariable("end") Integer end) {
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
    }
}
