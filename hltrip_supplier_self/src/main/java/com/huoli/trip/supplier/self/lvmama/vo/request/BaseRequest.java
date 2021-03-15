package com.huoli.trip.supplier.self.lvmama.vo.request;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1514:58
 */
public class BaseRequest implements Serializable {
    private String appKey;
    private String timestamp;
    private String messageFormat;
    private String sign;
}
