package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.mapper.ChinaCityMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
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
public class CommonServiceImpl implements CommonService {

    @Autowired
    private BackChannelMapper backChannelMapper;

    @Autowired
    private RedisTemplate jedisTemplate;

    @Autowired
    private BackupProductDao backupProductDao;

    @Autowired
    private HodometerDao hodometerDao;

    @Autowired
    private ChinaCityMapper chinaCityMapper;

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

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
        if(true){
            return;
        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(StringUtils.isNotBlank(product.getName()) && !StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // 图片
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // 产品描述
            if(StringUtils.isNotBlank(product.getDescription()) && !StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getIncludeDesc()) && !StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getExcludeDesc()) && !StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getRefundDesc()) && !StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getBookDesc()) && !StringUtils.equals(backupProduct.getBookDesc(), product.getBookDesc())){
                productFields.add("bookDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getRemark()) && !StringUtils.equals(backupProduct.getRemark(), product.getRemark())){
                productFields.add("remark");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getSuitDesc()) && !StringUtils.equals(backupProduct.getSuitDesc(), product.getSuitDesc())){
                productFields.add("suitDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
                        }
                    });
                }
            }
        }
    }

    @Override
    public void compareToursProduct(ProductPO product){
        // 暂时屏蔽
        if(true){
            return;
        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(StringUtils.isNotBlank(product.getName()) && !StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // 产品图
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // 产品描述
            if(StringUtils.isNotBlank(product.getDescription()) && !StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getIncludeDesc()) && !StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getExcludeDesc()) && !StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getRefundDesc()) && !StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            if(StringUtils.isNotBlank(product.getDiffPriceDesc()) && !StringUtils.equals(backupProduct.getDiffPriceDesc(), product.getDiffPriceDesc())){
                productFields.add("diffPriceDesc");
                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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

    @Override
    public void setCity(ScenicSpotMPO scenic){
        if(scenic == null){
            return;
        }
        String provinceCode = null;
        String cityCode = null;
        if(StringUtils.isNotBlank(scenic.getProvince())){
            if(scenic.getProvince().endsWith("省")){
                scenic.setProvince(scenic.getProvince().substring(0, scenic.getProvince().length() - 1));
            }
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getProvince(), 1, null);
            if(ListUtils.isNotEmpty(chinaCities)){
                provinceCode = chinaCities.get(0).getCode();
                scenic.setProvinceCode(provinceCode);
            }
        }
        if(StringUtils.isNotBlank(scenic.getCity())){
            if(scenic.getCity().endsWith("市")){
                scenic.setCity(scenic.getCity().substring(0, scenic.getCity().length() - 1));
            }
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getCity(), 2, provinceCode);
            if(ListUtils.isNotEmpty(chinaCities)){
                cityCode = chinaCities.get(0).getCode();
                scenic.setCityCode(cityCode);
            }
        }
        if(StringUtils.isNotBlank(scenic.getDistrict())){
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getDistrict(), 3, cityCode);
            if(ListUtils.isNotEmpty(chinaCities)){
                scenic.setDistrictCode(chinaCities.get(0).getCode());
            }
        }
    }

    @Override
    public void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String scenicId, Object origin){
        ScenicSpotBackupMPO scenicSpotBackupMPO = JSON.parseObject(JSON.toJSONString(newScenic), ScenicSpotBackupMPO.class);
        scenicSpotBackupMPO.setSupplierId(Constants.SUPPLIER_CODE_LMM_TICKET);
        scenicSpotBackupMPO.setSupplierScenicId(scenicId);
        scenicSpotBackupMPO.setOriginContent(JSON.toJSONString(origin));
        ScenicSpotBackupMPO exist = scenicSpotBackupDao.getScenicSpotBySupplierScenicIdAndSupplierId(scenicId, Constants.SUPPLIER_CODE_LMM_TICKET);
        if(exist == null){
            scenicSpotBackupMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        scenicSpotBackupMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotBackupDao.saveScenicSpotBackup(scenicSpotBackupMPO);
    }

    @Override
    public void updateScenicSpotMapping(String channelScenicId, String channel, ScenicSpotMPO newScenic){
        // 查映射关系
        ScenicSpotMappingMPO exist = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(channelScenicId, channel);
        if(exist != null){
            return;
        }
        // 没有找到映射就往本地新增一条
        ScenicSpotMPO addScenic = scenicSpotDao.addScenicSpot(newScenic);
        // 同时保存映射关系
        ScenicSpotMappingMPO scenicSpotMappingMPO = new ScenicSpotMappingMPO();
        scenicSpotMappingMPO.setChannelScenicSpotId(channelScenicId);
        scenicSpotMappingMPO.setScenicSpotId(addScenic.getId());
        scenicSpotMappingMPO.setChannel(channel);
        scenicSpotMappingMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingDao.addScenicSpotMapping(scenicSpotMappingMPO);
    }
}
