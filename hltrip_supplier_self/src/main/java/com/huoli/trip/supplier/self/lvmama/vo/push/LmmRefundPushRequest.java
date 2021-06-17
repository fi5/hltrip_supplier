package com.huoli.trip.supplier.self.lvmama.vo.push;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author :zhouwenbin
 * @time   :2021/3/17
 * @comment:
 * <
order
<orderId>10000</orderId>//
驴妈妈订单号
<partnerOrderID>12345</partnerOrderID>//
分销商订单号
<orderStatus>
正常 已取消 </ 订单状态
<request
Status 已退款 申请驳回 <request Status 申请状态
<
refundAmount refundAmount 退款金额，单位分
<
factorage factora ge 手续费，单位分
<
refundInfo refundInfo 备注说明
</
order
 **/
@Data
@XmlRootElement(name = "request")
public class LmmRefundPushRequest implements Serializable {


	private LmmRefundPushRequest.RefundPushBody body;

	@XmlElement(name = "body")
	public LmmRefundPushRequest.RefundPushBody getBody() {
		return body;
	}

	public void setBody(LmmRefundPushRequest.RefundPushBody body) {
		this.body = body;
	}

	@Data
	public static class RefundPushBody implements Serializable {

		private PushOrder order;
	}

	@Data
	public static class PushOrder implements Serializable {
		private String orderId;
		private String partnerOrderID;
		private String orderStatus;
		/**
		 * PASS:
		 * 已退款
		 * REVIEWING:
		 * 审核中
		 * REJECT:
		 * 申请驳回
		 */
		private String requestStatus;

		private Double refundAmount;//退款最终金额，单位：分
		private Double factorage;//手续费
		private String ndInfo;
	}

}
