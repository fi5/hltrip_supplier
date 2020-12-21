package com.huoli.trip.supplier.self.difengyun.vo.push;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1014:58
 */
@Data
public class DfyOrderPushResponse implements Serializable {
    private boolean success = true;
    private String msg = "success";
    private String errorCode;

    public DfyOrderPushResponse() {
    }

    public DfyOrderPushResponse(boolean success, String msg, String errorCode) {
        this.success = success;
        this.msg = msg;
        this.errorCode = errorCode;
    }
}
