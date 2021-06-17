package com.huoli.trip.supplier.feign.client.lvmama.client.impl;

import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaProductClient;
import com.huoli.trip.supplier.self.lvmama.vo.response.*;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;


/**
* @Description: 驴妈妈接口对接
* @return ${return_type}
* @throws
* @author lunatic
* @date 2021/3/15 18:11
*/
@Component
@Slf4j
public class LvmamaProductClientFallback implements FallbackFactory<ILvmamaProductClient> {


    @Override
    public ILvmamaProductClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new ILvmamaProductClient() {

            @Override
            public LmmScenicListResponse getScenicList(@RequestParam("currentPage") int currentPage){
                return null;
            }

            @Override
            public LmmScenicListResponse getScenicListById(@RequestParam("scenicId") String scenicId){
                return null;
            }

            @Override
            public LmmProductListResponse getProductList(@RequestParam("currentPage") int currentPage){
                return null;
            }

            @Override
            public LmmProductListResponse getProductListById(@RequestParam("productIds") String productIds){
                return null;
            }

            @Override
            public LmmGoodsListByIdResponse getGoodsListById(@RequestParam("goodsIds") String goodsIds){
                return null;
            }

            @Override
            public LmmPriceResponse getPriceList(@RequestParam("goodsIds") String goodsIds, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate){
                return null;
            }
        };
    }
}
