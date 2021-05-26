package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import lombok.Data;

import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2021/3/17
 * @comment:
 **/
@Data
public class LmmOrderDetailResponse extends  LmmBaseResponse{
	private List<LvOrderDetail> orderList;
	private String blackIdList;
}
