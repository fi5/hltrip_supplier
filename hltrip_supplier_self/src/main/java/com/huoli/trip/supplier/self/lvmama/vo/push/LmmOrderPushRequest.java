package com.huoli.trip.supplier.self.lvmama.vo.push;

import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author :zhouwenbin
 * @time   :2021/3/17
 * @comment:
 **/
@Data
@XmlRootElement(name = "request")
public class LmmOrderPushRequest implements Serializable {

	private LmmOrderPushRequest.OrderPushBody body;

	@XmlElement(name = "body")
	public LmmOrderPushRequest.OrderPushBody getBody() {
		return body;
	}

	public void setBody(LmmOrderPushRequest.OrderPushBody body) {
		this.body = body;
	}

	@Data
	public static class OrderPushBody implements Serializable {

		private LvOrderDetail order;
	}



}
