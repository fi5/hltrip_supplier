package com.huoli.trip.supplier.web.hllx.service;

import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.TripOrderOperationLog;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.TripNotice;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.central.OrderStatusKafka;
import com.huoli.trip.supplier.self.hllx.vo.HllxOrderOperationRequest;
import com.huoli.trip.supplier.self.hllx.vo.HllxRefundNoticeRequest;
import com.huoli.trip.supplier.self.hllx.vo.HllxVoucher;
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.mapper.TripOrderOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class HllxSyncService {
    @Autowired
    TripOrderOperationLogMapper tripOrderOperationLogMapper;

    @Autowired
    private HuoliTrace huoliTrace;

    /**
     * 推送订单状态
     * @return
     */
    public boolean getOrderStatus(HllxOrderOperationRequest request){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
        OrderStatusKafka req = new OrderStatusKafka();
        req.setOrderStatus(request.getNewStatus());
        String remark = request.getExplain();
        if(StringUtils.isNotEmpty(remark)) {
            req.setRemark(remark);
        }
        List<HllxVoucher> vouchers =  request.getVochers();
        if(ListUtils.isNotEmpty(vouchers)){
            List<PushOrderStatusReq.Voucher> list = new ArrayList<>();
            vouchers.forEach(hllxVoucher -> {
                PushOrderStatusReq.Voucher voucher = new PushOrderStatusReq.Voucher();
                voucher.setType(hllxVoucher.getType());
                voucher.setInType(hllxVoucher.getInType());
                if(hllxVoucher.getType() == 2 || hllxVoucher.getType() ==3 || hllxVoucher.getType() == 6){
                    voucher.setVocherUrl(hllxVoucher.getVoucherInfo());
                }else{
                    voucher.setVocherNo(hllxVoucher.getVoucherInfo());
                }
                list.add(voucher);
            });
            req.setVochers(list);
        }
        req.setPartnerOrderId(request.getOrderId());
        try {
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
        } catch (Exception e) {

        }
        try {
            TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
            BeanUtils.copyProperties(request,tripOrderOperationLog);
            String updateTime = request.getUpdateTime();
            String explain = request.getExplain();
            if(StringUtils.isEmpty(updateTime)){
                updateTime = simpleDateFormat.format(new Date());
            }
            tripOrderOperationLog.setUpdateTime(updateTime);
            if(StringUtils.isNotEmpty(explain)){
                tripOrderOperationLog.setRemark(explain);
            }
            tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
        }catch (Exception exception){
            log.error("写入操作日志出现异常：",exception);
        }
        return true;
    }


    /**
     * 推送退款状态
     * @param request
     */
    public void refundNotice(HllxRefundNoticeRequest request){
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
        try {
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(request), 10000, TraceConfig.traceHeaders(huoliTrace, url));
        } catch (Exception e) {

        }
    }

    /**
     * 发送出团通知
     * @return
     */
    public boolean tripNotice(TripNotice request){
        String url = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral") + "/recSupplier/tripNotice";
        try {
            HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(request), 10000, TraceConfig.traceHeaders(huoliTrace, url));
        } catch (Exception e) {
            log.error("请求出团通知异常", e);
        }
        try {
            TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
            BeanUtils.copyProperties(request,tripOrderOperationLog);
            String updateTime = DateTimeUtil.formatDate(new Date());
            tripOrderOperationLog.setUpdateTime(updateTime);
            tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
        }catch (Exception exception){
            log.error("写入操作日志出现异常：", exception);
        }
        return true;
    }


}
