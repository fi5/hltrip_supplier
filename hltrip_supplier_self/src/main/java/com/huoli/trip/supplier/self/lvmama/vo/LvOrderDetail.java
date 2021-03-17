package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2021/3/15
 * @comment:
 *
 * 未支付：
UNPAY
已支付：
PAYED
部分支付：
PARTPAY

已发送：CREDENCE_SEND
未发送
：CREDENCE_NO_SEND
 **/
@Data
public class LvOrderDetail implements Serializable {
	private String orderId;
	private String partnerOrderNo;
	private String status;//
	private String approveStatus;//
	private String paymentStatus;
	private String waitPaymentTime;
	private String credenctStatus;
	private String performStatus;//USED 已使用；UNUSE 未使用
	private List<Credential> credentials;

	@Data
	public static class Credential implements Serializable {
		private String goodsId;//
		private String sendSource;
		private String serialCode;
		private String QRcode;
		private String additional;
		private String voucherUrl;//凭证 URL 根据配置输出

	}
}
