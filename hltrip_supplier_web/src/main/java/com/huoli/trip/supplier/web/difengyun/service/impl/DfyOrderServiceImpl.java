package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyOrderDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:
 **/
@Service
@Slf4j
public class DfyOrderServiceImpl implements DfyOrderService {

    @Autowired
    private IDiFengYunClient diFengYunClient;

    public BaseResponse<DfyOrderDetail> orderDetail(BaseOrderRequest request){

        DfyOrderDetailRequest dfyOrderDetail=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetail);
        dfyOrderDetail.setOrderId(request.getSupplierOrderId());
        try {
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);


            DfyOrderDetail detail = baseResult.getData();
            return BaseResponse.success(detail);
        } catch (Exception e) {
        	log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
        }


    }
}
