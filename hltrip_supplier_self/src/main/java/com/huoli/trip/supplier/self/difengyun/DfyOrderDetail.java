package com.huoli.trip.supplier.self.difengyun;

import lombok.Data;

import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:https://open.difengyun.com/doc/api/all/8/2
 **/
@Data
public class DfyOrderDetail {

	private String orderId;
	private String canPay;//支付开关 0.不可支付，1.可以支付；分销商系统需要控制，当canPay="1"时，才调用【出票(代扣)接口】。
	private Integer productId;
	private Integer scenicId;//景点ID
	/**
	 * 订单状态:

	 待确认：订单正在校验/占位；
	 待付款：订单在此状态下，同时满足“支付开关canPay=1.可支付”，则可以调用出票代扣接口进行付款；

	 出票中（已确认）：付款后出票中到此状态；
	 已完成：表示出票成功；
	 已取消：订单取消成功，或者退票成功，到此状态；
	 */
	private String orderStatus;
	private String orderTime;//下单时间
	private String planDate;//出游日期
	private Integer bookNumber;//预订数量
	private Float amoutPrice;
	private String pickUpAddress;//取票地址
	private String externalOrderId;//凭证码（“已完成”状态有值）
	private Contact contact;
	private List<Tourist> touristList;
	private EnterCertificate enterCertificate;//入园方式及凭证信息
	private Delivery delivery;


	@Data
	public static class Contact {
		private String contactName;
		private String contactEmail;
		private String contactTel;
		private Integer psptType;//证件类型：1、二代身份证2、护照3、军官证4、港澳通行证7、台胞证8、回乡证9、户口簿10、出生证明11、台湾通行证
		private String psptId;
	}

	@Data
	public static class Tourist {
		private String name;
		private Integer psptType;//证件类型：1、二代身份证2、护照3、军官证4、港澳通行证7、台胞证8、回乡证9、户口簿10、出生证明11、台湾通行证
		private String psptId;
		private String tel;
		private String email;
	}

	@Data
	public static class EnterCertificate {
		private String enterCertificateType;
		private List<EnterCertificateTypeInfo> enterCertificateTypeInfo;
		private String enterCertificateTxt;
	}

	@Data
	public static class EnterCertificateTypeInfo {
		private String resourceId;
		private List<TicketCertInfo> ticketCertInfos;
	}
	@Data
	public static class TicketCertInfo {
		private int certType;
		private String[] fileUrls;
	}
	@Data
	public static class Delivery {
		private String deliveryType;
		private String receiverName;
		private String telNum;
		private String deliveryEndAddress;
		private String zipCode;
	}

}
