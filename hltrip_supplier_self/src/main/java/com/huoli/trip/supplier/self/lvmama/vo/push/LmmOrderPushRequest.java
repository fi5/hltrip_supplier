package com.huoli.trip.supplier.self.lvmama.vo.push;

import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :zhouwenbin
 * @time   :2021/3/17
 * @comment:
 **/
@Data
public class LmmOrderPushRequest implements Serializable {

	private LvOrderDetail order;
}
