package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1514:58
 */
@Data
public class LmmBaseRequest implements Serializable {
    private String appKey;
    private String timestamp;
    private String messageFormat = "json";
    private String sign;
}
