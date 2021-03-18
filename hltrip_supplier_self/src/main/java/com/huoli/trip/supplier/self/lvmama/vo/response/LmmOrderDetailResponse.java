package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import lombok.Data;

/**
 * @author :zhouwenbin
 * @time   :2021/3/17
 * @comment:
 **/
@Data
public class LmmOrderDetailResponse extends  LmmBaseResponse{
	private LvOrderDetail order;
}
