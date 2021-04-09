package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.TripModuleTypeEnum;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.groupTour.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.common.vo.ImageBase;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.data.api.ProductDataService;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyDepartCity;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyJourneyInfo;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyToursCalendarRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursCalendarResponse;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.difengyun.convert.DfyToursConverter;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.mapper.ChinaCityMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private ChinaCityMapper chinaCityMapper;

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Reference(group = "hltrip")
    private DataService dataService;

    @Reference(group = "hltrip")
    private ProductDataService productDataService;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private GroupTourProductDao groupTourProductDao;

    @Autowired
    private GroupTourProductSetMealDao groupTourProductSetMealDao;

    @Autowired
    private PriceDao priceDao;

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
        AddressInfo addressInfo = setCity(scenic.getProvince(), scenic.getCity(), scenic.getDistrict());
        if(StringUtils.isNotBlank(addressInfo.getProvinceCode())){
            scenic.setProvinceCode(addressInfo.getProvinceCode());
        }
        if(StringUtils.isNotBlank(addressInfo.getProvinceName())){
            scenic.setProvince(addressInfo.getProvinceName());
        }
        if(StringUtils.isNotBlank(addressInfo.getCityCode())){
            scenic.setCityCode(addressInfo.getCityCode());
        }
        if(StringUtils.isNotBlank(addressInfo.getCityName())){
            scenic.setCity(addressInfo.getCityName());
        }
        if(StringUtils.isNotBlank(addressInfo.getDestinationCode())){
            scenic.setDistrictCode(addressInfo.getDestinationCode());
        }
        if(StringUtils.isNotBlank(addressInfo.getDestinationName())){
            scenic.setDistrict(addressInfo.getDestinationName());
        }
    }

    @Override
    public AddressInfo setCity(String provinceName, String cityName, String districtName){
        AddressInfo addressInfo = new AddressInfo();
        String province = null;
        String provinceId = null;
        String city = null;
        String cityId = null;
        String county = null;
        String countyId = null;
        if(StringUtils.isNotBlank(provinceName)){
            if(provinceName.endsWith("省")){
                province = provinceName.substring(0, provinceName.length() - 1);
            }
            List<ChinaCity> provinces = chinaCityMapper.getCityByNameAndTypeAndParentId(province, 1, null);
            if(ListUtils.isNotEmpty(provinces)){
                ChinaCity provinceObj = provinces.get(0);
                province = provinceObj.getName();
                provinceId = provinceObj.getCode();
            }
        }
        if(StringUtils.isNotBlank(cityName)){
            if(cityName.endsWith("市")){
                cityName = cityName.substring(0, cityName.length() - 1);
            }
            List<ChinaCity> cites = chinaCityMapper.getCityByNameAndTypeAndParentId(cityName, 2, provinceId);
            if(ListUtils.isNotEmpty(cites)){
                ChinaCity cityObj = cites.get(0);
                city = cityObj.getName();
                cityId = cityObj.getCode();
                if(StringUtils.isBlank(provinceId)){
                    ChinaCity provinceObj = chinaCityMapper.getCityByCode(cityObj.getParentCode());
                    if(provinceObj != null){
                        provinceId = provinceObj.getCode();
                        province = provinceObj.getName();
                    }
                }
            }
        }
        if(StringUtils.isNotBlank(districtName)){
            List<ChinaCity> counties = chinaCityMapper.getCityByNameAndTypeAndParentId(districtName, 3, cityId);
            if(ListUtils.isNotEmpty(counties)){
                ChinaCity countyObj = counties.get(0);
                county = countyObj.getName();
                countyId = countyObj.getCode();
                if(StringUtils.isBlank(cityId)){
                    ChinaCity cityObj = chinaCityMapper.getCityByCode(countyObj.getParentCode());
                    if(cityObj != null){
                        city = cityObj.getName();
                        cityId = cityObj.getCode();
                        if(StringUtils.isBlank(provinceId)){
                            ChinaCity provinceObj = chinaCityMapper.getCityByCode(cityObj.getParentCode());
                            if(provinceObj != null){
                                provinceId = provinceObj.getCode();
                                province = provinceObj.getName();
                            }
                        }
                    }
                }
            }
        }
        addressInfo.setCityCode(cityId);
        addressInfo.setProvinceName(province);
        addressInfo.setProvinceCode(provinceId);
        addressInfo.setCityName(city);
        if(StringUtils.isNotBlank(countyId)){
            addressInfo.setType("1");
            addressInfo.setDestinationCode(countyId);
            addressInfo.setDestinationName(county);
        } else if(StringUtils.isNotBlank(cityId)){
            addressInfo.setType("0");
        }
        return addressInfo;
    }

    @Override
    public void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String scenicId, Object origin){
        log.info("开始保存景点副本");
        ScenicSpotBackupMPO scenicSpotBackupMPO = JSON.parseObject(JSON.toJSONString(newScenic), ScenicSpotBackupMPO.class);
        scenicSpotBackupMPO.setSupplierId(Constants.SUPPLIER_CODE_LMM_TICKET);
        scenicSpotBackupMPO.setSupplierScenicId(scenicId);
        scenicSpotBackupMPO.setOriginContent(JSON.toJSONString(origin));
        ScenicSpotBackupMPO exist = scenicSpotBackupDao.getScenicSpotBySupplierScenicIdAndSupplierId(scenicId, Constants.SUPPLIER_CODE_LMM_TICKET);
        if(exist == null){
            scenicSpotBackupMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            scenicSpotBackupMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        scenicSpotBackupMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotBackupDao.saveScenicSpotBackup(scenicSpotBackupMPO);
        log.info("景点副本保存成功id={}", scenicSpotBackupMPO.getId());
    }

    @Override
    public void updateScenicSpotMapping(String channelScenicId, String channel, ScenicSpotMPO newScenic){
        // 查映射关系
        ScenicSpotMappingMPO exist = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(channelScenicId, channel);
        if(exist != null){
            log.info("{}景点{}已有映射id={}", channel, channelScenicId, exist.getId());
            return;
        }
        newScenic.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
        // 没有找到映射就往本地新增一条
        ScenicSpotMPO addScenic = scenicSpotDao.addScenicSpot(newScenic);
        log.info("{}景点{}没有有映射新增一条景点id={}", channel, channelScenicId, newScenic.getId());
        // 同时保存映射关系
        ScenicSpotMappingMPO scenicSpotMappingMPO = new ScenicSpotMappingMPO();
        scenicSpotMappingMPO.setChannelScenicSpotId(channelScenicId);
        scenicSpotMappingMPO.setScenicSpotId(addScenic.getId());
        scenicSpotMappingMPO.setChannel(channel);
        scenicSpotMappingMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingDao.addScenicSpotMapping(scenicSpotMappingMPO);
    }

    @Override
    public String getId(String bizTag){
        return String.valueOf(dataService.getId(bizTag));
    }

    @Override
    public void refreshList(int type, String productId, int updateType, boolean add){
        if(add){
            log.info("新增刷新列表。。type = {}, productId = {}", type, productId);
            productDataService.addProduct(type, productId);
        } else {
            log.info("更新刷新列表。。type = {}, productId = {}, updateType = {}", type, productId, updateType);
            productDataService.updateProduct(type, productId, updateType);
        }
    }

    @Override
    public void transTours(){
        List<ProductPO> productPOs = productDao.getBySupplierId(Constants.SUPPLIER_CODE_SHENGHE_TICKET);
        for (ProductPO productPO : productPOs) {
            log.info("开始处理  {}", JSON.toJSONString(productPO));
            boolean add = false;
            GroupTourProductMPO groupTourProductMPO = groupTourProductDao.getTourProduct(productPO.getSupplierProductId(), Constants.SUPPLIER_CODE_SHENGHE_TICKET);
            if(groupTourProductMPO == null ){
                groupTourProductMPO = new GroupTourProductMPO();
                groupTourProductMPO.setId(getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
                groupTourProductMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                add = true;
            }
            groupTourProductMPO.setSupplierProductId(productPO.getSupplierProductId());
            groupTourProductMPO.setChannel(Constants.SUPPLIER_CODE_DFY_TOURS);
            groupTourProductMPO.setProductName(productPO.getName());
            if(productPO.getMainItem() == null){
                log.info("{}mainitem为空", productPO.getCode());
                continue;
            }
            if(ListUtils.isNotEmpty(productPO.getMainItem().getTopic())){
                StringBuffer sb = new StringBuffer();
                for (BaseCode baseCode : productPO.getMainItem().getTopic()) {
                    if(StringUtils.equals(baseCode.getCode(), "1000")){
                        sb.append("17").append(",");
                    } else if(StringUtils.equals(baseCode.getCode(), "1001")){
                        sb.append("18").append(",");
                    } else if(StringUtils.equals(baseCode.getCode(), "1002")){
                        sb.append("19").append(",");
                    } else if(StringUtils.equals(baseCode.getCode(), "1003")){
                        sb.append("20").append(",");
                    } else {
                        sb.append("16");
                    }
                }
                String theme = sb.toString();
                if(theme.endsWith(",")){
                    theme = theme.substring(0, theme.length() - 1);
                }
                groupTourProductMPO.setTheme(theme);
            }
            groupTourProductMPO.setGoTraffic(productPO.getGoTraffic() == null ? null : productPO.getGoTraffic().toString());
            groupTourProductMPO.setBackTraffice(productPO.getReturnTraffic() == null ? null : productPO.getReturnTraffic().toString());
            groupTourProductMPO.setIsDel(0);
            groupTourProductMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            if(StringUtils.isNotBlank(productPO.getOriCity())){
                groupTourProductMPO.setDepInfos(Arrays.asList(productPO.getOriCity().split(",")).stream().map(c ->
                        setCity(null, c, null)).filter(c -> StringUtils.isNotBlank(c.getCityCode())).collect(Collectors.toList()));
            }
            if(StringUtils.isNotBlank(productPO.getDesCity())){
                groupTourProductMPO.setArrInfos(Arrays.asList(productPO.getDesCity().split(",")).stream().map(c ->
                        setCity(null, c, null)).filter(c -> StringUtils.isNotBlank(c.getCityCode())).collect(Collectors.toList()));
            }
            GroupTourProductPayInfo productPayInfo = new GroupTourProductPayInfo();
            productPayInfo.setSellType(1);
            productPayInfo.setConfirmType(1);
            groupTourProductMPO.setGroupTourProductPayInfo(productPayInfo);
            groupTourProductMPO.setStatus(1);
            GroupTourProductBaseSetting baseSetting = new GroupTourProductBaseSetting();
            if(productPO.getValidTime() == null){
                if(productPO.getInvalidTime() != null){
                    // 已过期
                    if(DateTimeUtil.trancateToDate(new Date()).getTime() > productPO.getInvalidTime().getTime()){
                        groupTourProductMPO.setStatus(3);
                        baseSetting.setLaunchType(3);
                    }
                } else {
                    baseSetting.setLaunchType(1);
                }
            } else {
                // 没到上架时间
                if(productPO.getValidTime().getTime() > DateTimeUtil.trancateToDate(new Date()).getTime()){
                    groupTourProductMPO.setStatus(2);
                    baseSetting.setLaunchType(2);
                } else {
                    if(productPO.getInvalidTime() != null){
                        // 已过期
                        if(DateTimeUtil.trancateToDate(new Date()).getTime() > productPO.getInvalidTime().getTime()){
                            groupTourProductMPO.setStatus(3);
                            baseSetting.setLaunchType(3);
                        }
                    } else {
                        baseSetting.setLaunchType(1);
                    }
                }
            }
            baseSetting.setLaunchDateTime(productPO.getValidTime());
            baseSetting.setStockCount(0);
            baseSetting.setAppSource(ListUtils.isEmpty(productPO.getAppFrom()) ? null : String.join(",", productPO.getAppFrom()));
            groupTourProductMPO.setGroupTourProductBaseSetting(baseSetting);
            // 笛风云没有退改
            groupTourProductDao.saveProduct(groupTourProductMPO);
            if(ListUtils.isEmpty(groupTourProductMPO.getDepInfos())){
                log.info("{}出发城市为空", productPO.getCode());
                continue;
            }
            for (AddressInfo addressInfo : groupTourProductMPO.getDepInfos()) {
                GroupTourProductSetMealMPO setMealMPO = groupTourProductSetMealDao.getSetMeal(groupTourProductMPO.getId(), addressInfo.getCityCode());
                if(setMealMPO == null){
                    setMealMPO = new GroupTourProductSetMealMPO();
                    setMealMPO.setId(getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
                }
                setMealMPO.setGroupTourProductId(groupTourProductMPO.getId());
                setMealMPO.setName(groupTourProductMPO.getProductName());
                setMealMPO.setTripDay(productPO.getTripDays());
                setMealMPO.setConstInclude(productPO.getIncludeDesc());
                setMealMPO.setCostExclude(productPO.getExcludeDesc());
                setMealMPO.setBookNotice(productPO.getBookDesc());
                if(ListUtils.isNotEmpty(productPO.getBookNoticeList())){
                    setMealMPO.setBookNotices(productPO.getBookNoticeList().stream().map(b -> {
                        DescInfo descInfo = new DescInfo();
                        descInfo.setContent(b.getContent());
                        descInfo.setTitle(b.getTitle());
                        return descInfo;
                    }).collect(Collectors.toList()));
                }
                setMealMPO.setDepCode(addressInfo.getCityCode());
                setMealMPO.setDepName(addressInfo.getCityName());

                HodometerPO hodometerPO = hodometerDao.getHodometerPO(productPO.getCode());
                if(hodometerPO == null){
                    log.error("{}行程信息为空", productPO.getCode());
                    continue;
                }
                if(ListUtils.isNotEmpty(hodometerPO.getHodometers())){
                    List<GroupTourTripInfo> groupTourTripInfos = Lists.newArrayList();
                    int day = 1;
                    for(Hodometer hodometer : hodometerPO.getHodometers()) {
                        GroupTourTripInfo groupTourTripInfo = new GroupTourTripInfo();
                        groupTourTripInfo.setDay(day++);
                        List<GroupTourProductTripItem> items = Lists.newArrayList();
                        if (ListUtils.isNotEmpty(hodometer.getRoutes())) {
                            GroupTourProductTripItem item1 = new GroupTourProductTripItem();
                            item1.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_HOTEL.getCode()));
                            hodometer.getRoutes().stream().filter(r -> r.getMduleType() == DfyConstants.MODULE_TYPE_HOTEL).map(r -> {
                                GroupTourHotel groupTourHotel = new GroupTourHotel();
                                // todo 酒店名称没有，是需要关联本地酒店表吗
                                groupTourHotel.setDesc(r.getDescribe());
                                groupTourHotel.setRoomName(r.getName());
                                if (ListUtils.isNotEmpty(r.getImages())) {
                                    groupTourHotel.setImages(r.getImages().stream().map(ImageBase::getUrl).collect(Collectors.toList()));
                                }
                                return groupTourHotel;
                            }).collect(Collectors.toList());
                            items.add(item1);
                            for (Route route : hodometer.getRoutes()) {
                                int type = route.getMduleType();
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                if (type == DfyConstants.MODULE_TYPE_SCENIC) {
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_SCENIC.getCode()));
                                    item.setPoiName(route.getTitle());
                                } else if (type == DfyConstants.MODULE_TYPE_TRAFFIC) {
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_TRAFFIC.getCode()));
                                    String means = "";
                                    if (route.getTransportationType() > 0) {
                                        switch (route.getTransportationType()) {
                                            case 1:
                                                means = "飞机";
                                                break;
                                            case 2:
                                                means = "火车";
                                                break;
                                            case 3:
                                                means = "轮渡";
                                                break;
                                            case 4:
                                                means = "汽车";
                                                break;
                                            case 5:
                                            default:
                                                means = "自主";
                                                break;
                                        }
                                    }
                                    if (StringUtils.isBlank(means)) {
                                        item.setPoiName(String.format("从%s到%s", route.getDeparture(), route.getArrival()));
                                    } else {
                                        item.setPoiName(String.format("从%s乘%s到%s", route.getDeparture(), means, route.getArrival()));
                                    }
                                } else if (type == DfyConstants.MODULE_TYPE_FOOD) {
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_FOOD.getCode()));
                                    item.setPoiName(route.getName());
                                } else if (type == DfyConstants.MODULE_TYPE_SHOPPING) {
                                    item.setPoiName(route.getName());
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_SHOPPING.getCode()));
                                } else if (type == DfyConstants.MODULE_TYPE_ACTIVITY) {
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_ACTIVITY.getCode()));
                                    item.setPoiName(route.getName());
                                } else if (type == DfyConstants.MODULE_TYPE_REMINDER) {
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_REMINDER.getCode()));
                                    item.setPoiName(route.getName());
                                }
                                item.setTime(route.getDepTime());
                                item.setPlayTime(route.getDuration());
                                item.setPoiDesc(route.getDescribe());
                                if (ListUtils.isNotEmpty(route.getImages())) {
                                    item.setImages(route.getImages().stream().map(ImageBase::getUrl).collect(Collectors.toList()));
                                }
                                items.add(item);
                            }
                        }
                        groupTourTripInfo.setGroupTourProductTripItems(items);
                        groupTourTripInfos.add(groupTourTripInfo);
                    }
                    setMealMPO.setGroupTourTripInfos(groupTourTripInfos);
                }
                PricePO pricePO = priceDao.getByProductCode(productPO.getCode());
                if(pricePO != null && ListUtils.isNotEmpty(pricePO.getPriceInfos())){
                    setMealMPO.setGroupTourPrices(pricePO.getPriceInfos().stream().map(priceInfoPO -> {
                        GroupTourPrice groupTourPrice = new GroupTourPrice();
                        groupTourPrice.setDiffPrice(priceInfoPO.getRoomDiffPrice());
                        groupTourPrice.setChdPrice(priceInfoPO.getChdSettlePrice());
                        groupTourPrice.setChdSellPrice(priceInfoPO.getChdSalePrice());
                        groupTourPrice.setAdtPrice(priceInfoPO.getSettlePrice());
                        groupTourPrice.setAdtSellPrice(priceInfoPO.getSalePrice());
                        groupTourPrice.setAdtStock(priceInfoPO.getStock() == null ? 0 : priceInfoPO.getStock().intValue());
                        groupTourPrice.setChdStock(priceInfoPO.getStock() == null ? 0 : priceInfoPO.getStock().intValue());
                        groupTourPrice.setDate(DateTimeUtil.formatDate(priceInfoPO.getSaleDate()));
                        return groupTourPrice;
                    }).collect(Collectors.toList()));
                }
                groupTourProductSetMealDao.saveSetMeals(setMealMPO);
                refreshList(1, groupTourProductMPO.getId(), 1, add);
            }
        }
    }
}
