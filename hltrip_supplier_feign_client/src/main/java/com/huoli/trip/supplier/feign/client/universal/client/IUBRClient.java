package com.huoli.trip.supplier.feign.client.universal.client;

import com.huoli.trip.supplier.feign.client.universal.client.impl.UBRClientFallback;
import com.huoli.trip.supplier.feign.client.universal.interceptor.UBRFeignInterceptor;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@FeignClient(name = "ubr", url = "${btg.host.server}"
        , configuration = UBRFeignInterceptor.class
        , fallbackFactory = UBRClientFallback.class)
public interface IUBRClient {

    /**
     * 登录
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/user/login")
    UBRBaseResponse<UBRLoginResponse> login(@RequestBody UBRLoginRequest request);

    /**
     * 刷新token
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/user/refresh")
    UBRBaseResponse<UBRLoginResponse> refreshToken();

    /**
     * 获取门票列表
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/ticket")
    UBRBaseResponse<UBRTicketList> getTicketList(@RequestParam("type") String type);

    /**
     * 初始化数据
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/init")
    UBRBaseResponse init();

}
