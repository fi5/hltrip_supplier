package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :zhouwenbin
 * @time   :2020/12/21
 * @comment:
 * https://open.difengyun.com/doc/api/all/8/2
 **/
@Data
public class DfyBillQueryDataReq extends TraceRequest implements Serializable{

	private String acctId;
	private int accType;
	private int billType;
	private int status;
	private String beginTime;
	private String endTime;
	private int start;
	private int limit;
}
