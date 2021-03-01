package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :zhouwenbin
 * @time   :2021/2/20
 * @comment:
 **/
@Data
public class DfyVerifyOrderResponse implements Serializable {

	private boolean supportVerify;
	private String verifyMsg;
	private int totalCount;
	private int usedCount;
}
