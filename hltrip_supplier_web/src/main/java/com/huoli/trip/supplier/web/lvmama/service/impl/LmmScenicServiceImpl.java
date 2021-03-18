package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicResponse;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.lvmama.service.LmmScenicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
 */
@Service
@Slf4j
public class LmmScenicServiceImpl implements LmmScenicService {

    @Autowired
    private ILvmamaClient lvmamaClient;

    @Autowired
    private ProductItemDao productItemDao;

    public void syncScenicList(LmmScenicRequest request){
        LmmScenicResponse lmmScenicResponse = lvmamaClient.getScenicList(request);

        if(lmmScenicResponse != null && lmmScenicResponse.getState() != null
                && StringUtils.equals(lmmScenicResponse.getState().getCode(), "1000")){
            // todo 同步景点
        }
    }
}
