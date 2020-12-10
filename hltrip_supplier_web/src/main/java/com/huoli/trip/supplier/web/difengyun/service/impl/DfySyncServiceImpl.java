package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/9<br>
 */
@Service
@Slf4j
public class DfySyncServiceImpl implements DfySyncService {

    @Autowired
    private IDiFengYunClient diFengYunClient;

    public void syncScenicList(DfyScenicListRequest request){
        DfyBaseRequest<DfyScenicListRequest> listRequest = new DfyBaseRequest<>(request);
        DfyBaseResult<DfyScenicListResponse> baseResult = diFengYunClient.getScenicList(listRequest);
        DfyScenicListResponse response = baseResult.getData();
        if(response != null && ListUtils.isNotEmpty(response.getRows())){
            List<DfyScenic> scenics= response.getRows();
            scenics.forEach(s -> {
                DfyScenicDetailRequest detailRequest = new DfyScenicDetailRequest();
                detailRequest.setScenicId(s.getScenicId());
                DfyBaseRequest detailBaseRequest = new DfyBaseRequest<>(detailRequest);
                DfyBaseResult<DfyScenicDetail> detailBaseResult = diFengYunClient.getScenicDetail(detailBaseRequest);
                if(detailBaseResult != null && detailBaseResult.getData() != null){
                    DfyScenicDetail scenicDetail = detailBaseResult.getData();
                } else {
                    log.error("笛风云门票详情返回空，request = {}", JSON.toJSONString(detailBaseRequest));
                }
            });
        } else {
            log.error("笛风云门票列表返回空，request = {}", JSON.toJSONString(listRequest));
        }

    }
}
