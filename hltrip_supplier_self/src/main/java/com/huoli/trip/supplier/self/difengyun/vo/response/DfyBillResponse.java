package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :zhouwenbin
 * @time   :2020/12/21
 * @comment:
 *
amount		String	本次交易金额
bizOrderId	String	订单号
merFlowNo	String	账户流水号；与途牛钱包网站【交易管理】模块呈现的流水号一致。
outTradeNo	String	代扣流水号；与【出票(代扣)接口】返回的outTradeNo
字段对应，可用于系统自动对账
status	Integer	账单处理结果，1处理完成-1处理失败3处理
billType	Integer	账单类型：1支付，2代扣，3代付，4退款，5提现
billTypeDesc	String	账单类型描述
time	String	添加时间，格式如：2016-03-11 13:54:52
productType	String	产品品类
remark	String 备注


 **/
@Data
public class DfyBillResponse implements Serializable {
	private Integer count;
	private Integer pages;
	private List<QueryBillsDto> rows;
	@Data
	public static class QueryBillsDto {

		private Float amount;
		private String bizOrderId;
		private String merFlowNo;
		private String outTradeNo;
		private int status;
		private int billType;
		private String billTypeDesc;
		private String time;
		private String productType;
		private String remark;
	}
}
