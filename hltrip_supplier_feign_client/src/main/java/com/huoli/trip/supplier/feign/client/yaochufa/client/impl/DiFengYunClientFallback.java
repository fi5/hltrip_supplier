package com.huoli.trip.supplier.feign.client.yaochufa.client.impl;

import com.huoli.trip.supplier.feign.client.yaochufa.client.IDiFengYunClient;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyTicketDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Slf4j
@Component
public class DiFengYunClientFallback implements FallbackFactory<IDiFengYunClient> {

    @Override
    public IDiFengYunClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new IDiFengYunClient() {

            @Override
            public DfyBaseResult<DfyScenicListResponse> getScenicList(DfyBaseRequest<DfyScenicListRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyScenicDetail> getScenicDetail(DfyBaseRequest<DfyScenicDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyTicketDetail> getTicketDetail(DfyBaseRequest<DfyTicketDetailRequest> request) {
                return null;
            }
        };

    }
}
