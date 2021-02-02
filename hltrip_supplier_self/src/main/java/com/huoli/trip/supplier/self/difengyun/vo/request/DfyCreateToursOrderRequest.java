package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import com.huoli.trip.supplier.self.difengyun.vo.ReceiveVisaCompanyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.response.ToursTourist;
import lombok.Data;

import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/2/110:05
 */
@Data
public class DfyCreateToursOrderRequest extends TraceRequest {
    /**
     * 来源订单id
     */
    private String sourceOrderId;
    /**
     * 成人数
     */
    private Integer adultNum;
    /**
     * 儿童数
     */
    private Integer childNum;
    /**
     * 分销商业务联系人邮箱 必填
     */
    private String contactEmail;
    /**
     * 分销商业务联系人电话（笛风客服会拨打此电话沟通出游需求等相关事宜，请务必保证手机号码真实有效）
     */
    private String contactTel;
    /**
     * 分销商业务联系人姓名
     */
    private String contactName;
    /**
     * 产品id 必填
     */
    private Integer productId;
    /**
     * 出发日期 必填
     */
    private String startTime;
    /**
     * 出发城市 必填
     */
    private String startCity;
    /**
     * 出发城市编码  必填
     */
    private String startCityCode;
    /**
     * 	订单备注
     */
    private String remark;
    /**
     * 	账号id 必填
     */
    private String acctId;
    /**
     * 签证资源id集合
     */
    private List<Long> visaResIdList;
    /**
     * 	保险资源id集合
     */
    private List<Long> insuranceResIdList;
    /**
     * 出游人信息
     * 如果希望系统自动占位，此字段必传。
     */
    private List<ToursTourist> touristList;
    /**
     * 签证材料递送途牛门市信息
     * 如果希望系统自动占位，当订单包含签证时，此字段必传。
     * 传入此字段前，请确保：您接受签证材料的要求作为合同要约的一部分，并已清楚了解您需要提供的签证材料，且确保您可以在截止收取材料前（发团前30天）递交所需的签证材料到指定地址。
     */
    private ReceiveVisaCompanyInfo receiveVisaCompanyInfo;
}
