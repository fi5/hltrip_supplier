package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

@Data
public class LmmResendCodeRequest extends LmmBaseRequest {

    /**
     * 重发凭证
     */
    private LmmResendCodeOrder order;
}
