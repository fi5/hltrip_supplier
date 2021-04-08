package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.BackupProductDao;
import com.huoli.trip.supplier.web.dao.HodometerDao;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/2<br>
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private BackChannelMapper backChannelMapper;

    @Autowired
    private RedisTemplate jedisTemplate;

    @Autowired
    private BackupProductDao backupProductDao;

    @Autowired
    private HodometerDao hodometerDao;

    @Override
    public BackChannelEntry getSupplierById(String supplierId){
        String key = String.join("_", "SUPPLIER_", supplierId);
        if(jedisTemplate.hasKey(key)){
            Object value = jedisTemplate.opsForValue().get(key);
            if(value != null){
                return JSON.parseObject(value.toString(), BackChannelEntry.class);
            }
            return null;
        }
        BackChannelEntry backChannelEntry = backChannelMapper.getChannelInfoByChannelCode(supplierId);
        if(backChannelEntry != null){
            jedisTemplate.opsForValue().set(key, JSON.toJSONString(backChannelEntry), 1, TimeUnit.DAYS);
            return backChannelEntry;
        }
        return null;
    }

    @Override
    public void compareProduct(ProductPO product){
        // 暂时屏蔽
//        if(true){
//            return;
//        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(StringUtils.isNotBlank(product.getName()) && !StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品名称变更。原值={}，新值={}", product.getCode(), backupProduct.getName(), product.getName());
            }
            // 图片
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品图片变更。原值={}，新值={}", product.getCode(), JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()));
            }
            // 产品描述
            if(StringUtils.isNotBlank(product.getDescription()) && !StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品描述变更。原值={}，新值={}", product.getCode(), backupProduct.getDescription(), product.getDescription());
            }
            if(StringUtils.isNotBlank(product.getIncludeDesc()) && !StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用包含变更。原值={}，新值={}", product.getCode(), backupProduct.getIncludeDesc(), product.getIncludeDesc());
            }
            if(StringUtils.isNotBlank(product.getExcludeDesc()) && !StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用不包含变更。原值={}，新值={}", product.getCode(), backupProduct.getExcludeDesc(), product.getExcludeDesc());
            }
            if(StringUtils.isNotBlank(product.getRefundDesc()) && !StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品退改说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRefundDesc(), product.getRefundDesc());
            }
            if(StringUtils.isNotBlank(product.getBookDesc()) && !StringUtils.equals(backupProduct.getBookDesc(), product.getBookDesc())){
                productFields.add("bookDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品预订须知变更。原值={}，新值={}", product.getCode(), backupProduct.getBookDesc(), product.getBookDesc());
            }
            if(StringUtils.isNotBlank(product.getRemark()) && !StringUtils.equals(backupProduct.getRemark(), product.getRemark())){
                productFields.add("remark");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品其它说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRemark(), product.getRemark());
            }
            if(StringUtils.isNotBlank(product.getSuitDesc()) && !StringUtils.equals(backupProduct.getSuitDesc(), product.getSuitDesc())){
                productFields.add("suitDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品适用条件变更。原值={}，新值={}", product.getCode(), backupProduct.getSuitDesc(), product.getSuitDesc());
            }
            product.setChangedFields(productFields);
            // 产品说明
            if(ListUtils.isNotEmpty(product.getBookDescList())){
                // 如果备份没有，说明新的全都相当于是变化的
                if(ListUtils.isEmpty(backupProduct.getBookDescList())){
                    product.getBookDescList().forEach(b -> {
                        List<String> descFields = Lists.newArrayList();
                        descFields.add("title");
                        descFields.add("content");
                        b.setChangedFields(descFields);
                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                    });
                    log.info("{}产品动态预订说明变更（全新增）。新值={}", JSON.toJSONString(product.getBookDescList()));
                } else {
                    product.getBookDescList().forEach(b -> {
                        DescriptionPO descriptionPO = backupProduct.getBookDescList().stream().filter(bb ->
                                StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                        if(descriptionPO == null){
                            List<String> descFields = Lists.newArrayList();
                            descFields.add("title");
                            descFields.add("content");
                            b.setChangedFields(descFields);
                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                            log.info("{}产品动态预订说明变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            }
                        }
                    });
                }
            }
            // 资源
            if(product.getRoom() != null && ListUtils.isNotEmpty(product.getRoom().getRooms())){
                if(backupProduct.getRoom() == null || ListUtils.isEmpty(backupProduct.getRoom().getRooms())){
                    product.getRoom().getRooms().forEach(r -> {
                        List<String> roomFields = Lists.newArrayList();
                        roomFields.add("title");
                        r.setChangedFields(roomFields);
                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                    });
                    log.info("{}产品房间资源名称变更（全新增）。新值={}", product.getCode(), JSON.toJSONString(product.getRoom()));
                } else {
                    product.getRoom().getRooms().forEach(r -> {
                        RoomInfoPO roomInfoPO = backupProduct.getRoom().getRooms().stream().filter(br ->
                                StringUtils.equals(br.getItemId(), r.getItemId())).findFirst().orElse(null);
                        if(roomInfoPO == null || (StringUtils.isNotBlank(r.getTitle())
                                && !StringUtils.equals(r.getTitle(), roomInfoPO.getTitle()))){
                            List<String> roomFields = Lists.newArrayList();
                            roomFields.add("title");
                            r.setChangedFields(roomFields);
                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                            log.info("{}产品房间资源名称变更（更新）。原值={}，新值={}", product.getCode(), roomInfoPO.getTitle(), r.getTitle());
                        }
                    });
                }
            }
            if(product.getTicket() != null && ListUtils.isNotEmpty(product.getTicket().getTickets())){
                if(backupProduct.getTicket() == null || ListUtils.isEmpty(backupProduct.getTicket().getTickets())){
                    product.getTicket().getTickets().forEach(r -> {
                        List<String> roomFields = Lists.newArrayList();
                        roomFields.add("title");
                        r.setChangedFields(roomFields);
                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                    });
                    log.info("{}产品门票资源名称变更（全新增）。新值={}", product.getCode(), JSON.toJSONString(product.getTicket()));
                } else {
                    product.getTicket().getTickets().forEach(r -> {
                        TicketInfoPO ticketInfoPO = backupProduct.getTicket().getTickets().stream().filter(br ->
                                StringUtils.equals(br.getItemId(), r.getItemId())).findFirst().orElse(null);
                        if(ticketInfoPO == null || StringUtils.isNotBlank(r.getTitle())
                                && !StringUtils.equals(r.getTitle(), ticketInfoPO.getTitle())){
                            List<String> roomFields = Lists.newArrayList();
                            roomFields.add("title");
                            r.setChangedFields(roomFields);
                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                            log.info("{}产品门票资源名称变更（更新）。原值={}，新值={}", product.getCode(), ticketInfoPO.getTitle(), r.getTitle());
                        }
                    });
                }
            }
        }
    }

    @Override
    public void compareToursProduct(ProductPO product){
        // 暂时屏蔽
//        if(true){
//            return;
//        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(StringUtils.isNotBlank(product.getName()) && !StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品名称变更。原值={}，新值={}", product.getCode(), backupProduct.getName(), product.getName());
            }
            // 产品图
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品图片变更。原值={}，新值={}", product.getCode(), JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()));
            }
            // 产品描述
            if(StringUtils.isNotBlank(product.getDescription()) && !StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品描述变更。原值={}，新值={}", product.getCode(), backupProduct.getDescription(), product.getDescription());
            }
            if(StringUtils.isNotBlank(product.getIncludeDesc()) && !StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用包含变更。原值={}，新值={}", product.getCode(), backupProduct.getIncludeDesc(), product.getIncludeDesc());
            }
            if(StringUtils.isNotBlank(product.getExcludeDesc()) && !StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用不包含变更。原值={}，新值={}", product.getCode(), backupProduct.getExcludeDesc(), product.getExcludeDesc());
            }
            if(StringUtils.isNotBlank(product.getRefundDesc()) && !StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品退改说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRefundDesc(), product.getRefundDesc());
            }
            if(StringUtils.isNotBlank(product.getDiffPriceDesc()) && !StringUtils.equals(backupProduct.getDiffPriceDesc(), product.getDiffPriceDesc())){
                productFields.add("diffPriceDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品差价说明变更。原值={}，新值={}", product.getCode(), backupProduct.getDiffPriceDesc(), product.getDiffPriceDesc());
            }
            product.setChangedFields(productFields);
            // 产品说明
            if(ListUtils.isNotEmpty(product.getBookDescList())){
                // 如果备份没有，说明新的全都相当于是变化的
                if(ListUtils.isEmpty(backupProduct.getBookDescList())){
                    product.getBookDescList().forEach(b -> {
                        if(StringUtils.isNotBlank(b.getContent()) ){
                            List<String> descFields = Lists.newArrayList();
                            descFields.add("title");
                            descFields.add("content");
                            b.setChangedFields(descFields);
                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                        }
                    });
                    log.info("{}产品动态预订说明bookdesc变更（全新增）。新值={}", JSON.toJSONString(product.getBookDescList()));
                } else {
                    product.getBookDescList().forEach(b -> {
                        DescriptionPO descriptionPO = backupProduct.getBookDescList().stream().filter(bb ->
                                StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                        if(descriptionPO == null){
                            if(StringUtils.isNotBlank(b.getContent()) ){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("title");
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明bookdesc变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明bookdesc变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            }
                        }
                    });
                }
            }
            if(ListUtils.isNotEmpty(product.getBookNoticeList())){
                // 如果备份没有，说明新的全都相当于是变化的
                if(ListUtils.isEmpty(backupProduct.getBookNoticeList())){
                    product.getBookNoticeList().forEach(b -> {
                        if(StringUtils.isNotBlank(b.getContent()) ){
                            List<String> descFields = Lists.newArrayList();
                            descFields.add("title");
                            descFields.add("content");
                            b.setChangedFields(descFields);
                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                        }
                    });
                    log.info("{}产品动态预订须知booknotice变更（全新增）。新值={}", JSON.toJSONString(product.getBookNoticeList()));
                } else {
                    product.getBookNoticeList().forEach(b -> {
                        DescriptionPO descriptionPO = backupProduct.getBookNoticeList().stream().filter(bb ->
                                StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                        if(descriptionPO == null){
                            if(StringUtils.isNotBlank(b.getContent()) ){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("title");
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订须知booknotice变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订须知booknotice变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void compareProductItem(ProductItemPO productItem){
        if(true){
            return;
        }
        BackupProductItemPO backupProductItemPO = backupProductDao.getBackupProductItemByCode(productItem.getCode());
        if(backupProductItemPO != null){
            ProductItemPO backupProductItem = JSON.parseObject(backupProductItemPO.getData(), ProductItemPO.class);
            List<String> itemFields = Lists.newArrayList();
            if(StringUtils.isNotBlank(productItem.getBusinessHours())
                    && !StringUtils.equals(backupProductItem.getBusinessHours(), productItem.getBusinessHours())){
                itemFields.add("businessHours");
                productItem.setAuditStatus(-1);
            }
            if(StringUtils.isNotBlank(productItem.getName())
                    && !StringUtils.equals(backupProductItem.getName(), productItem.getName())){
                itemFields.add("name");
                productItem.setAuditStatus(-1);
            }
            if(productItem.getMainImages() != null
                    && !StringUtils.equals(JSON.toJSONString(backupProductItem.getMainImages()), JSON.toJSONString(productItem.getMainImages()))){
                itemFields.add("mainImages");
                productItem.setAuditStatus(-1);
            }
            productItem.setChangedFields(itemFields);
        }
    }

    @Override
    public boolean compareHodometer(HodometerPO hodometerPO){
        if(true){
            return false;
        }
        boolean changed = false;
        BackupHodometerPO backupHodometerPO = hodometerDao.getBackupHodometerPO(hodometerPO.getCode());
        HodometerPO backupHodometer = null;
        if(backupHodometerPO != null ){
            backupHodometer = JSON.parseObject(backupHodometerPO.getData(), HodometerPO.class);
        }
        if(ListUtils.isNotEmpty(hodometerPO.getHodometers())){
            int i = 0;
            for (Hodometer h : hodometerPO.getHodometers()) {
                if(backupHodometer == null || ListUtils.isEmpty(backupHodometer.getHodometers())) {
                    if (ListUtils.isNotEmpty(h.getRoutes())) {
                        for (Route r : h.getRoutes()) {
                            if (StringUtils.isNotBlank(r.getDescribe())) {
                                List<String> routeFields = Lists.newArrayList();
                                routeFields.add("describe");
                                r.setChangedFields(routeFields);
                                changed = true;
                            }
                        }
                    }
                } else {
                    Hodometer hodometer = backupHodometer.getHodometers().get(i++);
                    if(ListUtils.isNotEmpty(h.getRoutes())){
                        int j = 0;
                        for (Route route : h.getRoutes()) {
                            Route backupRoute = null;
                            if(hodometer.getRoutes().size() > j){
                                backupRoute = hodometer.getRoutes().get(j++);
                            }
                            if(StringUtils.isNotBlank(route.getDescribe())
                                && (backupRoute == null ||
                                    !StringUtils.equals(route.getDescribe(), backupRoute.getDescribe()))){
                                List<String> routeFields = Lists.newArrayList();
                                routeFields.add("describe");
                                route.setChangedFields(routeFields);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public void saveBackupProduct(ProductPO product){
        BackupProductPO productBackup = new BackupProductPO();
        productBackup.setCode(product.getCode());
        productBackup.setData(JSON.toJSONString(product));
        backupProductDao.updateBackupProductByCode(productBackup);
    }

    @Override
    public void saveBackupProductItem(ProductItemPO productItem){
        BackupProductItemPO backupProductItemPO = new BackupProductItemPO();
        backupProductItemPO.setCode(productItem.getCode());
        backupProductItemPO.setData(JSON.toJSONString(productItem));
        backupProductDao.updateBackupProductItemByCode(backupProductItemPO);
    }

    @Override
    public void saveBackupHodometer(HodometerPO hodometerPO){
        BackupHodometerPO backupHodometerPO = new BackupHodometerPO();
        backupHodometerPO.setCode(hodometerPO.getCode());
        backupHodometerPO.setData(JSON.toJSONString(hodometerPO));
        hodometerDao.updateBackupByCode(backupHodometerPO);
    }
}
