package com.huoli.trip.supplier.web.controller.YaoChuFa;


import com.huoli.trip.supplier.self.yaochufa.vo.YcfCommonResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVochersResult;
import com.huoli.trip.supplier.web.Service.YaoChuFaCallBackService;
import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * yaochufa订单相关接口控制器
 */
@RestController
public class OrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @Autowired
    private YaoChuFaCallBackService yaoChuFaCallBackService;


    YcfCommonResult<YcfVochersResult> getVochers(String partnerOrderId){
        return iYaoChuFaClient.getVochers(partnerOrderId);

    }

    YcfCommonResult<YcfOrderStatusResult> getOrderStatus(String partnerOrderId){
        return iYaoChuFaClient.getOederStatus(partnerOrderId);
    }

    @RequestMapping(method = {RequestMethod.POST},value="/api/service/yaochufa/refundNotice")
    private void refundNotice(@RequestBody YcfRefundNoticeRequest request){
        yaoChuFaCallBackService.refundNotice(request);
    }



}
