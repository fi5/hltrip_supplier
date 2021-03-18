package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.ImageBasePO;
import com.huoli.trip.common.entity.ItemFeaturePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListByIdRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicResponse;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.lvmama.convert.LmmTicketConverter;
import com.huoli.trip.supplier.web.lvmama.service.LmmScenicService;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private CommonService commonService;

    @Override
    public boolean syncScenicList(LmmScenicListRequest request){
        LmmScenicResponse lmmScenicResponse = lvmamaClient.getScenicList(request);
        return updateScenic(lmmScenicResponse);
    }

    @Override
    public void syncScenicListById(LmmScenicListByIdRequest request){
        LmmScenicResponse lmmScenicResponse = lvmamaClient.getScenicListById(request);
        updateScenic(lmmScenicResponse);
    }

    @Override
    public void syncScenicListById(String id){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        LmmScenicResponse lmmScenicResponse = lvmamaClient.getScenicListById(request);
        updateScenic(lmmScenicResponse);
    }

    @Override
    public List<String> getSupplierScenicIds(){
        return productItemDao.selectSupplierItemIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                Constants.PRODUCT_ITEM_TYPE_TICKET);
    }

    private boolean updateScenic(LmmScenicResponse lmmScenicResponse){
        if(lmmScenicResponse == null){
            log.error("驴妈妈景点列表接口返回空");
            return false;
        }
        if(lmmScenicResponse.getState() == null){
            log.error("驴妈妈景点列表接口返回状态为空");
            return false;
        }
        if(!StringUtils.equals(lmmScenicResponse.getState().getCode(), "1000")){
            log.error("驴妈妈景点列表接口返回失败，code={}, message={}, solution={}",
                    lmmScenicResponse.getState().getCode(), lmmScenicResponse.getState().getMessage(),
                    lmmScenicResponse.getState().getSolution());
            return false;
        }
        if(ListUtils.isEmpty(lmmScenicResponse.getScenicNameList())){
            log.error("驴妈妈景点列表接口返回的数据为空");
            return false;
        }
        lmmScenicResponse.getScenicNameList().forEach(s -> {
            ProductItemPO newItem = LmmTicketConverter.convertToProductItemPO(s);
            ProductItemPO oldItem = productItemDao.selectByCode(newItem.getCode());
            List<ItemFeaturePO> featurePOs = null;
            ProductPO productPO = null;
            List<ImageBasePO> imageDetails = null;
            List<ImageBasePO> images = null;
            List<ImageBasePO> mainImages = null;
            if (oldItem == null) {
                oldItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // 笛风云跟团游默认审核通过
                oldItem.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
            } else {
                imageDetails = oldItem.getImageDetails();
                images = oldItem.getImages();
                mainImages = oldItem.getMainImages();
                newItem.setAuditStatus(oldItem.getAuditStatus());
                productPO = oldItem.getProduct();
                featurePOs = oldItem.getFeatures();
                // 比对信息
                commonService.compareProductItem(newItem);
            }
            newItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            newItem.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
            newItem.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
            // 保存副本
            commonService.saveBackupProductItem(newItem);
            newItem.setProduct(productPO);
            newItem.setImageDetails(imageDetails);
            newItem.setImages(images);
            newItem.setMainImages(mainImages);
            newItem.setFeatures(featurePOs);
            if(ListUtils.isEmpty(newItem.getImages()) && ListUtils.isEmpty(newItem.getMainImages())){
                log.info("{}没有列表图、轮播图，设置待审核", newItem.getCode());
                newItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            productItemDao.updateByCode(newItem);
        });
        return true;
    }
}
