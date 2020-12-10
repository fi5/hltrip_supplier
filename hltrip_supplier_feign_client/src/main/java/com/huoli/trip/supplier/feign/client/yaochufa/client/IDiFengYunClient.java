package com.huoli.trip.supplier.feign.client.yaochufa.client;

import com.huoli.trip.supplier.feign.client.yaochufa.Interceptor.DiFengYunFeignInterceptor;
import com.huoli.trip.supplier.feign.client.yaochufa.client.impl.DiFengYunClientFallback;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyTicketDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@FeignClient(name = "diFengYun", url = "${difengyun.host.server}"
        , configuration = DiFengYunFeignInterceptor.class
        , fallbackFactory = DiFengYunClientFallback.class)
public interface IDiFengYunClient {

    /**
     * 获取景点列表
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/scenicList")
    DfyBaseResult<DfyScenicListResponse> getScenicList(@RequestBody DfyBaseRequest<DfyScenicListRequest> request);

    /**
     * 获取景点详情
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/scenicDetail")
    DfyBaseResult<DfyScenicDetail> getScenicDetail(@RequestBody DfyBaseRequest<DfyScenicDetailRequest> request);

    /**
     * 获取门票详情
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/detail")
    DfyBaseResult<DfyTicketDetail> getTicketDetail(@RequestBody DfyBaseRequest<DfyTicketDetailRequest> request);
}
