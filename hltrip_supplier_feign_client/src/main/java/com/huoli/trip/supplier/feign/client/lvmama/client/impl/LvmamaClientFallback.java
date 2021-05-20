package com.huoli.trip.supplier.feign.client.lvmama.client.impl;

import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmProductListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.*;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
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
public class LvmamaClientFallback implements FallbackFactory<ILvmamaClient> {


    @Override
    public ILvmamaClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new ILvmamaClient() {

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

            @Override
            public LmmOrderDetailResponse orderDetail(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public LmmBaseResponse getCheckInfos(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse payOrder(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse createOrder(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse cancelOrder(@RequestParam("PartnerOrderNo") String PartnerOrderNo, @RequestParam("orderId") String orderId) {
                return null;
            }

            @Override
            public LmmBaseResponse refundTicket(@RequestParam("PartnerOrderNo") String PartnerOrderNo, @RequestParam("orderId") String orderId) {
                return null;
            }
        };
    }
}
