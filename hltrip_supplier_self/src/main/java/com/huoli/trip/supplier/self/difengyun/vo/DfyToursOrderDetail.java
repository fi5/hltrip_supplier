package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:https://open.difengyun.com/doc/api/all/8/2
 **/
@Data
public class DfyToursOrderDetail implements Serializable {

	private String orderId;

	/**
	 * 订单状态:
	 *
	 * 订单状态：待确认，待付款，已确认，已完成，已取消
子状态:初始状态
	 需求确认
	 占位成功
	 待签约付款
	 确认反馈中
	 确认失败
	 待通知
	 出团通知发送中
	 出游前
	 出游中
	 出游归来
	 加人加资源占位中
	 签约后变更
	 加人加资源占位成功
	 加人加资源占位失败
	 加人加资源占位已反馈
	 加人加资源确认中
	 核损中
	 取消订单核损中
	 核损已反馈
	 取消订单核损已反馈
	 加人加资源待付款
	 已取消
	 已取消



	 */
	private String orderStatus;
	private ToursOrderInfo orderInfo;
	private List<OrderAttachment> attachments;


	@Data
	public static class ToursOrderInfo implements Serializable {
		private String status;//
		private String canPay;//支付开关 0.不可支付，1.可以支付；分销商系统需要控制，当canPay="1"时，才调用【出票(代扣)接口】。
		private String orderId;//途牛订单
		private String orderTime;//下单时间
		private String clearTime;//清位时间
		private String contactName;//联系人姓名
		private String contactFixPhone;//
		private String contactMail;//
		private String productId;//
		private String productName;
		private int adultCount;
		private int childCount;
		private int duration;//出游天数
		private String startCityName;
		private String desCityName;
		private String departureTime;//出发日期
		private String returnTime;//归来日期
		private List<OrderInsuranceInfo> orderInsuranceInfo;
		private List<OtherInfo> orderOtherInfo;
		private BigDecimal payedPrice;//已支付金额
		private BigDecimal amoutPrice;//订单总价
		private List<Tourist> tourists;

	}

	@Data
	public static class OrderInsuranceInfo implements Serializable{
		private String name;
		private int insuranceType;//	保险类型
		private BigDecimal price;//单价
		private String effectiveDate;
		private String expirationDate;
		private int num;
		private Long id;
		private String insuranceTypeDesc;
		private int buyStatus;//购买状态1、已购买0、未购买
		private String buyStatusDesc;
		private Integer limitDayStart;
		private Integer limitDayEnd;
		private String term;//适用期限
		private BigDecimal totalPrice;//总价
	}

	@Data
	public static class Tourist implements Serializable{
		private String personType;
		private String name;
		private String lastname;
		private String firstname;
		private int sex;
		private int pspt_type;
		private String pspt_id;
		private String birthday;
	}

	@Data
	public static class OrderAttachment implements Serializable{
		private String fileName;
		private String type;
		private String url;
	}

	@Data
	public static class OtherInfo implements Serializable{
		private String pickUpInfo;//上车地点信息
	}



}
