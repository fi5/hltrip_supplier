package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushRequest;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description: 笛风状态推送
 * @date 2020/12/1014:54
 */
@Service
@Slf4j
public class DfyCallBackService {

    public DfyOrderPushResponse orderStatusNotice(DfyOrderPushRequest request) {
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
        try {
            String string = JSONObject.toJSONString(request);
            log.info("中台订单推送传参json:"+string);
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(request), 10000, null);
            log.info("中台orderStatusNotice返回:"+res);
            return new DfyOrderPushResponse();
        } catch (Exception e) {
            log.info("",e);
            return new DfyOrderPushResponse(false,"内部通信异常","500");
        }
    }

}
