package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huoli.trip.common.constant.*;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.*;
import com.huoli.trip.common.entity.mpo.groupTour.*;
import com.huoli.trip.common.entity.mpo.hotel.HotelMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.entity.po.PassengerTemplatePO;
import com.huoli.trip.common.util.*;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.vo.ImageBase;
import com.huoli.trip.common.vo.v2.ScenicSpotRuleCompare;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.data.api.ProductDataService;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.dao.BackupProductDao;
import com.huoli.trip.supplier.web.dao.HodometerDao;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.mapper.ChinaCityMapper;
import com.huoli.trip.supplier.web.mapper.PassengerTemplateMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.math.BigDecimal;
import java.util.Date;
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

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private PoiReviewDao poiReviewDao;

    @Autowired
    private SubscribeProductDao subscribeProductDao;

    @Autowired
    private ProductUpdateNoticeDao productUpdateNoticeDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private HotelMappingDao hotelMappingDao;

    @Autowired
    private HotelDao hotelDao;

    @Autowired
    private PassengerTemplateMapper passengerTemplateMapper;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

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
    public void compareProduct(ProductPO product, ProductPO existProduct){
        // product是根据供应商数据新创建的，如果跟备份数据比较没有变化，还得用已有数据existProduct，否则可能会覆盖本地编辑过的数据

        // 暂时屏蔽
//        if(true){
//            return;
//        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        log.info("比对产品{}", backupProductPO.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(!StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品名称变更。原值={}，新值={}", product.getCode(), backupProduct.getName(), product.getName());
            } else {
                product.setName(existProduct.getName());
            }
            // 图片
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品图片变更。原值={}，新值={}", product.getCode(), JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()));
            } else {
                product.setImages(existProduct.getImages());
            }
            // 产品描述
            if(!StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品描述变更。原值={}，新值={}", product.getCode(), backupProduct.getDescription(), product.getDescription());
            } else {
                product.setDescription(existProduct.getDescription());
            }
            if(!StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用包含变更。原值={}，新值={}", product.getCode(), backupProduct.getIncludeDesc(), product.getIncludeDesc());
            } else {
                product.setIncludeDesc(existProduct.getIncludeDesc());
            }
            if(!StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用不包含变更。原值={}，新值={}", product.getCode(), backupProduct.getExcludeDesc(), product.getExcludeDesc());
            } else {
                product.setExcludeDesc(existProduct.getExcludeDesc());
            }
            if(!StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品退改说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRefundDesc(), product.getRefundDesc());
            } else {
                product.setRefundDesc(existProduct.getRefundDesc());
            }
            if(!StringUtils.equals(backupProduct.getBookDesc(), product.getBookDesc())){
                productFields.add("bookDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品预订须知变更。原值={}，新值={}", product.getCode(), backupProduct.getBookDesc(), product.getBookDesc());
            } else {
                product.setBookDesc(existProduct.getBookDesc());
            }
            if(!StringUtils.equals(backupProduct.getRemark(), product.getRemark())){
                productFields.add("remark");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品其它说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRemark(), product.getRemark());
            } else {
                product.setRemark(existProduct.getRemark());
            }
            if(!StringUtils.equals(backupProduct.getSuitDesc(), product.getSuitDesc())){
                productFields.add("suitDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品适用条件变更。原值={}，新值={}", product.getCode(), backupProduct.getSuitDesc(), product.getSuitDesc());
            } else {
                product.setSuitDesc(existProduct.getSuitDesc());
            }
            product.setChangedFields(productFields);
            // 产品说明 这里不用更新
            if(ListUtils.isNotEmpty(product.getBookDescList())){
                // 如果备份没有，说明新的全都相当于是变化的
                if(ListUtils.isEmpty(backupProduct.getBookDescList())){
                    product.getBookDescList().forEach(b -> {
                        List<String> descFields = Lists.newArrayList();
                        descFields.add("title");
                        descFields.add("content");
                        b.setChangedFields(descFields);
//                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                            log.info("{}产品动态预订说明变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
//                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            } else {
                                DescriptionPO existDesc = existProduct.getBookDescList().stream().filter(bb ->
                                        StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                                if(existDesc != null){
                                    b.setContent(existDesc.getContent());
                                }
                            }
                        }
                    });
                }
                if(ListUtils.isNotEmpty(product.getBookDescList()) && ListUtils.isNotEmpty(existProduct.getBookDescList())){
                    // 取数据库数据与供应商数据差集，因为可能有的元素有变化，有变化的数据会多个changedFields，所以不能直接合并，直接合并可能会丢失changedFields
                    List<DescriptionPO> diff = existProduct.getBookDescList().stream().filter(ep ->
                            product.getBookDescList().stream().filter(p ->
                                    StringUtils.equals(p.getTitle(), ep.getTitle())).findFirst().orElse(null) == null).collect(Collectors.toList());
                    product.getBookDescList().addAll(diff);
                }
            }
            // 资源
            if(product.getRoom() != null && ListUtils.isNotEmpty(product.getRoom().getRooms())){
                if(backupProduct.getRoom() == null || ListUtils.isEmpty(backupProduct.getRoom().getRooms())){
                    product.getRoom().getRooms().forEach(r -> {
                        List<String> roomFields = Lists.newArrayList();
                        roomFields.add("title");
                        r.setChangedFields(roomFields);
//                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                        product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                            log.info("{}产品门票资源名称变更（更新）。原值={}，新值={}", product.getCode(), ticketInfoPO.getTitle(), r.getTitle());
                        }
                    });
                }
            }
        }
    }

    @Override
    public void compareToursProduct(ProductPO product, ProductPO existProduct){
        // 暂时屏蔽
//        if(true){
//            return;
//        }
        BackupProductPO backupProductPO = backupProductDao.getBackupProductByCode(product.getCode());
        if(backupProductPO != null){
            List<String> productFields = Lists.newArrayList();
            ProductPO backupProduct = JSON.parseObject(backupProductPO.getData(), ProductPO.class);
            // 产品名称
            if(!StringUtils.equals(backupProduct.getName(), product.getName())){
                productFields.add("name");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品名称变更。原值={}，新值={}", product.getCode(), backupProduct.getName(), product.getName());
            } else {
                product.setName(existProduct.getName());
            }
            // 产品图
            if(product.getImages() != null && !StringUtils.equals(JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()))){
                productFields.add("images");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品图片变更。原值={}，新值={}", product.getCode(), JSON.toJSONString(backupProduct.getImages()), JSON.toJSONString(product.getImages()));
            } else {
                product.setImages(existProduct.getImages());
            }
            // 产品描述
            if(!StringUtils.equals(backupProduct.getDescription(), product.getDescription())){
                productFields.add("description");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品描述变更。原值={}，新值={}", product.getCode(), backupProduct.getDescription(), product.getDescription());
            } else {
                product.setDescription(existProduct.getDescription());
            }
            if(!StringUtils.equals(backupProduct.getIncludeDesc(), product.getIncludeDesc())){
                productFields.add("includeDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用包含变更。原值={}，新值={}", product.getCode(), backupProduct.getIncludeDesc(), product.getIncludeDesc());
            } else {
                product.setIncludeDesc(existProduct.getIncludeDesc());
            }
            if(!StringUtils.equals(backupProduct.getExcludeDesc(), product.getExcludeDesc())){
                productFields.add("excludeDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品费用不包含变更。原值={}，新值={}", product.getCode(), backupProduct.getExcludeDesc(), product.getExcludeDesc());
            } else {
                product.setExcludeDesc(existProduct.getExcludeDesc());
            }
            if(!StringUtils.equals(backupProduct.getRefundDesc(), product.getRefundDesc())){
                productFields.add("refundDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品退改说明变更。原值={}，新值={}", product.getCode(), backupProduct.getRefundDesc(), product.getRefundDesc());
            } else {
                product.setRefundDesc(existProduct.getRefundDesc());
            }
            if(!StringUtils.equals(backupProduct.getDiffPriceDesc(), product.getDiffPriceDesc())){
                productFields.add("diffPriceDesc");
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                log.info("{}产品差价说明变更。原值={}，新值={}", product.getCode(), backupProduct.getDiffPriceDesc(), product.getDiffPriceDesc());
            } else {
                product.setDiffPriceDesc(existProduct.getDiffPriceDesc());
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
//                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明bookdesc变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
//                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订说明bookdesc变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            } else {
                                DescriptionPO existDesc = existProduct.getBookDescList().stream().filter(bb ->
                                        StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                                if(existDesc != null){
                                    b.setContent(existDesc.getContent());
                                }
                            }
                        }
                    });
                }
                if(ListUtils.isNotEmpty(product.getBookDescList()) && ListUtils.isNotEmpty(existProduct.getBookDescList())){
                    // 取数据库数据与供应商数据差集，因为可能有的元素有变化，有变化的数据会多个changedFields，所以不能直接合并，直接合并可能会丢失changedFields
                    List<DescriptionPO> diff = existProduct.getBookDescList().stream().filter(ep ->
                            product.getBookDescList().stream().filter(p ->
                                    StringUtils.equals(p.getTitle(), ep.getTitle())).findFirst().orElse(null) == null).collect(Collectors.toList());
                    product.getBookDescList().addAll(diff);
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
//                            product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
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
//                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订须知booknotice变更（部分新增）。新值={}", product.getCode(), JSON.toJSONString(b));
                            }
                        } else {
                            if(StringUtils.isNotBlank(b.getContent()) && !StringUtils.equals(descriptionPO.getContent(), b.getContent())){
                                List<String> descFields = Lists.newArrayList();
                                descFields.add("content");
                                b.setChangedFields(descFields);
//                                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                                log.info("{}产品动态预订须知booknotice变更（更新）。原值={}，新值={}", product.getCode(), JSON.toJSONString(descriptionPO), JSON.toJSONString(b));
                            } else {
                                DescriptionPO existNotice = backupProduct.getBookNoticeList().stream().filter(bb ->
                                        StringUtils.equals(bb.getTitle(), b.getTitle())).findFirst().orElse(null);
                                if(existNotice != null){
                                    b.setContent(existNotice.getContent());
                                }
                            }
                        }
                    });
                }
                if(ListUtils.isNotEmpty(product.getBookNoticeList()) && ListUtils.isNotEmpty(existProduct.getBookNoticeList())){
                    // 取数据库数据与供应商数据差集，因为可能有的元素有变化，有变化的数据会多个changedFields，所以不能直接合并，直接合并可能会丢失changedFields
                    List<DescriptionPO> diff = existProduct.getBookNoticeList().stream().filter(ep ->
                            product.getBookNoticeList().stream().filter(p ->
                                    StringUtils.equals(p.getTitle(), ep.getTitle())).findFirst().orElse(null) == null).collect(Collectors.toList());
                    product.getBookNoticeList().addAll(diff);
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
    public void checkProduct(ProductPO productPO, Date date){
        try {
            if(!checkProductStatus(productPO, date)){
                return;
            }
            // 自动上线，这个要放在最后判断，如果放在前面的话价格状态有问题会被再次修改，状态可能会有短暂不准确
            if(productPO.getValidTime() != null && productPO.getValidTime() != null
                    && date.getTime() >= productPO.getValidTime().getTime()
                    && date.getTime() <= productPO.getInvalidTime().getTime()
                    && Constants.PRODUCT_STATUS_INVALID_SALE_DATE == productPO.getStatus() ){
                log.error("已进入销售日期范围，并且状态是日期异常，改成上线。。。code = {}, validDate = {}",
                        productPO.getCode(), DateTimeUtil.formatDate(productPO.getValidTime()));
                // 供应商渠道不自动上线
                if(!Lists.newArrayList(Constants.SUPPLIER_CODE_YCF, Constants.SUPPLIER_CODE_DFY, Constants.SUPPLIER_CODE_DFY_TOURS).contains(productPO.getSupplierId())){
                    productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_VALID);
                }
                return;
            }
        } catch (Exception e) {
            log.error("刷新产品状态异常，productCode={}", productPO.getCode(), e);
        }
    }

    @Override
    public void checkProductReverse(String productCode){
        try {
            ProductPO productPO = productDao.getByCode(productCode);
            if(productPO == null){
                log.error("产品{}不存在", productCode);
                return;
            }
            if(checkProductStatus(productPO, new Date())){
                // 如果以上条件都不满足说明产品没问题。所以应该是上线状态
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_VALID);
            }
        } catch (Exception e) {
            log.error("刷新产品状态异常，productCode={}", productCode, e);
        }
    }

    @Override
    public void checkProductReverseByItemId(String itemCode){
        try {
            List<ProductPO> productPOs = productDao.getProductListByItemIds(Lists.newArrayList(itemCode));
            if(ListUtils.isEmpty(productPOs)){
                log.error("item{}没有关联的产品", itemCode);
                return;
            }
            productPOs.forEach( productPO -> {
                if(checkProductStatus(productPO, new Date())){
                    // 如果以上条件都不满足说明产品没问题。所以应该是上线状态
                    productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_VALID);
                }
            });
        } catch (Exception e) {
            log.error("刷新产品状态异常，itemCode={}", itemCode, e);
        }
    }

    private boolean checkProductStatus(ProductPO productPO, Date date){
        if(productPO.getValidTime() != null && date.getTime() < productPO.getValidTime().getTime()){
            log.error("还没到销售日期。。。code = {}, validDate = {}",
                    productPO.getCode(), DateTimeUtil.formatDate(productPO.getValidTime()));
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_SALE_DATE);
            return false;
        }
        if(productPO.getInvalidTime() != null && date.getTime() > productPO.getInvalidTime().getTime()){
            log.error("已经过了销售日期。。。code = {}, invalidDate = {}",
                    productPO.getCode(), DateTimeUtil.formatDate(productPO.getInvalidTime()));
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_SALE_DATE);
            return false;
        }
        PricePO pricePO = priceDao.getByProductCode(productPO.getCode());
        if(pricePO == null || ListUtils.isEmpty(pricePO.getPriceInfos())){
            log.error("没有价格信息，code = {}", productPO.getCode());
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE_STOCK);
            return false;
        }
        if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                && checkPrice(p.getSalePrice()) && checkStock(p.getStock()))){
            log.error("没有有效的价格和库存信息，code = {}", productPO.getCode());
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE_STOCK);
            return false;
        }
        if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                && checkPrice(p.getSalePrice()))){
            log.error("没有有效的价格信息，code = {}", productPO.getCode());
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE);
            return false;
        }
        if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                && checkStock(p.getStock()))){
            log.error("没有有效的库存信息，code = {}", productPO.getCode());
            productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_STOCK);
            return false;
        }
        return true;
    }

    private boolean checkPrice(BigDecimal price){
        return price != null && price.compareTo(BigDecimal.valueOf(0)) == 1;
    }

    private boolean checkStock(Integer stock){
        return stock != null && stock > 0;
    }

    private boolean checkDate(Date cDate, Date nDate){
        if(cDate == null){
            return false;
        }
        long cTime = DateTimeUtil.trancateToDate(cDate).getTime();
        long nTime = DateTimeUtil.trancateToDate(nDate).getTime();
        return cTime >= nTime;
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
    public void setCity(HotelMPO hotel){
        if(hotel == null){
            return;
        }
        AddressInfo addressInfo = setCity(hotel.getProvinceName(), hotel.getCity(), null);
        if(StringUtils.isNotBlank(addressInfo.getProvinceCode())){
            hotel.setProvinceCode(addressInfo.getProvinceCode());
        }
        if(StringUtils.isNotBlank(addressInfo.getProvinceName())){
            hotel.setProvinceName(addressInfo.getProvinceName());
        }
        if(StringUtils.isNotBlank(addressInfo.getCityCode())){
            hotel.setCityCode(addressInfo.getCityCode());
        }
        if(StringUtils.isNotBlank(addressInfo.getCityName())){
            hotel.setCity(addressInfo.getCityName());
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
            addressInfo.setDestinationCode(cityId);
            addressInfo.setDestinationName(city);
        }
        return addressInfo;
    }

    @Override
    public void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String channelScenicId, String channel, Object origin){
        log.info("开始保存景点副本");
        ScenicSpotBackupMPO scenicSpotBackupMPO = scenicSpotBackupDao.getScenicSpotBySupplierScenicIdAndSupplierId(channelScenicId, channel);
        if(scenicSpotBackupMPO == null){
            scenicSpotBackupMPO = new ScenicSpotBackupMPO();
            scenicSpotBackupMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            scenicSpotBackupMPO.setSupplierId(channel);
            scenicSpotBackupMPO.setSupplierScenicId(channelScenicId);
            scenicSpotBackupMPO.setCreateTime(new Date());
            log.info("创建新的景点备份，scenicId={}, name={}, channel={}，channelScenicId={}", newScenic.getId(), newScenic.getName(), channel, channelScenicId);
        }
        scenicSpotBackupMPO.setScenicSpotMPO(newScenic);
        scenicSpotBackupMPO.setOriginContent(JSON.toJSONString(origin));
        scenicSpotBackupMPO.setUpdateTime(new Date());
        scenicSpotBackupDao.saveScenicSpotBackup(scenicSpotBackupMPO);
        log.info("景点副本保存成功id={}", scenicSpotBackupMPO.getId());
    }

    @Override
    public void updateScenicSpotMapping(String channelScenicId, String channel, String channelName, ScenicSpotMPO newScenic){
        // 查映射关系
        log.info("查询景点是否映射，channel={}, channelScenicId={}", channel, channelScenicId);
        ScenicSpotMappingMPO exist = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(channelScenicId, channel);
        if(exist != null){
            log.info("{}景点{}已有映射id={}，跳过", channel, channelScenicId, exist.getId());
            return;
        }
        log.info("查询是否存在同名同址景点，name={}，address={}", newScenic.getName());
        ScenicSpotMPO existScenic = scenicSpotDao.getScenicSpotByNameAndAddress(newScenic.getName(), newScenic.getCityCode());
        String scenicId;
        if(existScenic == null){
            newScenic.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            newScenic.setCreateTime(new Date());
            newScenic.setUpdateTime(new Date());
            if (!CollectionUtils.isEmpty(newScenic.getImages())) {
                newScenic.setImages(UploadUtil.getNetUrlAndUpload(newScenic.getImages()));
            }
            // 没有找到映射就往本地新增一条
            scenicSpotDao.addScenicSpot(newScenic);
            scenicId = newScenic.getId();
            // 景点申请单
            PoiReviewMPO poiReviewMPO = new PoiReviewMPO();
            poiReviewMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            poiReviewMPO.setChannel(channel);
            poiReviewMPO.setChannelName(channelName);
            poiReviewMPO.setPoiName(newScenic.getName());
            poiReviewMPO.setStatus(0);
            poiReviewMPO.setCreateTime(new Date());
            poiReviewMPO.setCityName(newScenic.getCity());
            poiReviewMPO.setCityCode(newScenic.getCityCode());
            poiReviewMPO.setPoiId(newScenic.getId());
            poiReviewMPO.setPoiType(0);
            poiReviewMPO.setUpdateTime(new Date());
            poiReviewDao.addPoiReview(poiReviewMPO);
            log.info("不存在同名同址景点，添加新的景点记录和申请单，景点id={}，申请单id={}", scenicId, poiReviewMPO.getId());
        } else {
            scenicId = existScenic.getId();
            log.info("已存在同名同址景点，不用新增景点，直接关联，景点id={}", scenicId);
        }
        // 同时保存映射关系
        ScenicSpotMappingMPO scenicSpotMappingMPO = new ScenicSpotMappingMPO();
        scenicSpotMappingMPO.setChannelScenicSpotId(channelScenicId);
        scenicSpotMappingMPO.setScenicSpotId(scenicId);
        scenicSpotMappingMPO.setChannel(channel);
        scenicSpotMappingMPO.setCreateTime(new Date());
        scenicSpotMappingMPO.setUpdateTime(new Date());
        scenicSpotMappingDao.addScenicSpotMapping(scenicSpotMappingMPO);
        log.info("{}景点{}已关联本地景点id={}", channel, channelScenicId, scenicId);
    }

    @Override
    public void updateHotelMapping(String channelHotelId, String channel, String channelName, HotelMPO hotelMPO){
        // 查映射关系
        log.info("查询酒店是否映射，channel={}, channelHotelId={}", channel, channelHotelId);
        HotelMappingMPO exist = hotelMappingDao.getHotelByChannelHotelIdAndChannel(channelHotelId, channel);
        if(exist != null){
            log.info("{}酒店{}已有映射id={}，跳过", channel, channelHotelId, exist.getId());
            return;
        }
        log.info("查询是否存在同名同址酒店，name={}，address={}", hotelMPO.getName(), hotelMPO.getAddress());
        HotelMPO existHotel = hotelDao.getHotelByNameAndAddress(hotelMPO.getName(), hotelMPO.getAddress());
        String hotelId;
        if(existHotel == null){
            hotelMPO.setCreateTime(new Date());
            hotelMPO.setUpdateTime(new Date());
            hotelMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_HOTEL)));
            // 没有找到映射就往本地新增一条
            hotelDao.addHotel(hotelMPO);
            hotelId = hotelMPO.getId();
            // 景点申请单
            PoiReviewMPO poiReviewMPO = new PoiReviewMPO();
            poiReviewMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            poiReviewMPO.setChannel(channel);
            poiReviewMPO.setChannelName(channelName);
            poiReviewMPO.setPoiName(hotelMPO.getName());
            poiReviewMPO.setStatus(0);
            poiReviewMPO.setCreateTime(new Date());
            poiReviewMPO.setCityName(hotelMPO.getCity());
            poiReviewMPO.setPoiId(hotelMPO.getId());
            poiReviewMPO.setPoiType(1);
            poiReviewMPO.setUpdateTime(new Date());
            poiReviewDao.addPoiReview(poiReviewMPO);
            log.info("不存在同名同址酒店，添加新的酒店记录和申请单，酒店id={}，申请单id={}", hotelId, poiReviewMPO.getId());
        } else {
            hotelId = existHotel.getId();
            log.info("已存在同名同址酒店，不用新增酒店，直接关联，酒店id={}", hotelId);
        }
        // 同时保存映射关系
        HotelMappingMPO hotelMappingMPO = new HotelMappingMPO();
        hotelMappingMPO.setChannelHotelId(channelHotelId);
        hotelMappingMPO.setHotelId(hotelId);
        hotelMappingMPO.setChannel(channel);
        hotelMappingMPO.setCreateTime(new Date());
        hotelMappingMPO.setUpdateTime(new Date());
        hotelMappingDao.addHotelMapping(hotelMappingMPO);
        log.info("{}酒店{}已关联本地酒店id={}", channel, channelHotelId, hotelMPO.getId());
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
    public void transTours(String code){
        List<ProductPO> productPOs = Lists.newArrayList();
        if(StringUtils.isNotBlank(code)){
            ProductPO productPO = productDao.getByCode(code);
            productPOs.add(productPO);
        } else {
            productPOs = productDao.getBySupplierId(Constants.SUPPLIER_CODE_SHENGHE_TICKET);
        }
        for (ProductPO productPO : productPOs) {
            log.info("开始处理  {}", JSON.toJSONString(productPO));
            boolean add = false;
            GroupTourProductMPO groupTourProductMPO = groupTourProductDao.getTourProduct(productPO.getSupplierProductId(), Constants.SUPPLIER_CODE_SHENGHE_TICKET);
            if(groupTourProductMPO == null ){
                groupTourProductMPO = new GroupTourProductMPO();
                groupTourProductMPO.setId(getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
                groupTourProductMPO.setCreateTime(new Date());
                add = true;
            }
            groupTourProductMPO.setSupplierProductId(productPO.getSupplierProductId());
            groupTourProductMPO.setMerchantCode(productPO.getSupplierProductId());
            groupTourProductMPO.setChannel(Constants.SUPPLIER_CODE_SHENGHE_TICKET);
            groupTourProductMPO.setProductName(productPO.getName());
            if(productPO.getProductType() == ProductType.TRIP_GROUP.getCode()){
                groupTourProductMPO.setGroupTourType("1");
            } else if(productPO.getProductType() == ProductType.TRIP_FREE.getCode()){
                groupTourProductMPO.setGroupTourType("2");
            } else if(productPO.getProductType() == ProductType.TRIP_GROUP_PRIVATE.getCode()){
                groupTourProductMPO.setGroupTourType("3");
            } else if(productPO.getProductType() == ProductType.TRIP_GROUP_SEMI.getCode()){
                groupTourProductMPO.setGroupTourType("5");
            } else if(productPO.getProductType() == ProductType.TRIP_GROUP_LOCAL.getCode()){
                groupTourProductMPO.setGroupTourType("6");
            }
            groupTourProductMPO.setRefundDesc(productPO.getRefundDesc());
            groupTourProductMPO.setFreeInsurance(0);
            groupTourProductMPO.setContractSigningType(0);
            groupTourProductMPO.setNonGroupAgreement("1");
            groupTourProductMPO.setGroupWorkAgreement(1);
            if(productPO.getMainItem() == null){
                log.info("{}mainitem为空", productPO.getCode());
                continue;
            }
            List<String> highs = Lists.newArrayList();
            if(ListUtils.isNotEmpty(productPO.getMainItem().getTags())){
                highs = productPO.getMainItem().getTags();
            }
            if(StringUtils.isNotBlank(productPO.getMainItem().getSubTitle())){
                highs.add(productPO.getMainItem().getSubTitle());
            }
            groupTourProductMPO.setHighlights(highs);

            if(ListUtils.isNotEmpty(productPO.getMainItem().getImages())){
                groupTourProductMPO.setImages(productPO.getMainItem().getImages().stream().map(i -> i.getUrl()).collect(Collectors.toList()));
            }
            if(ListUtils.isNotEmpty(productPO.getMainItem().getMainImages())){
                groupTourProductMPO.setMainImage(productPO.getMainItem().getMainImages().get(0).getUrl());
            } else if(ListUtils.isNotEmpty(groupTourProductMPO.getImages())){
                groupTourProductMPO.setMainImage(groupTourProductMPO.getImages().get(0));
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
            groupTourProductMPO.setUpdateTime(new Date());
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
            if(productPO.getBookAheadMin() != null){
                productPayInfo.setBeforeBookDay(Double.valueOf(BigDecimalUtil.div(BigDecimalUtil.div(productPO.getBookAheadMin(), 60, 0), 24, 0)).intValue());
            }
            productPayInfo.setConfirmUploadDay(0);
            productPayInfo.setLatestBookTime("23:59");

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
            if(ListUtils.isNotEmpty(productPO.getBookRules())){
                BookRulePO bookRulePO = productPO.getBookRules().stream().filter(b -> StringUtils.equals(b.getRuleType(), "1")).findFirst().orElse(null);
                if(bookRulePO != null){
                    // 创建默认的出行人模板
                    String idInfo = null;
                    if(ListUtils.isNotEmpty(bookRulePO.getCredentials())){
                        idInfo = bookRulePO.getCredentials().stream().map(String::valueOf).collect(Collectors.joining(","));
                    }
                    List<String> pi = Lists.newArrayList();
                    if(bookRulePO.getCnName() != null && bookRulePO.getCnName()){
                        pi.add("2");
                    }
                    if(bookRulePO.getPhone() != null && bookRulePO.getPhone()){
                        pi.add("6");
                    }
                    if(bookRulePO.getEmail() != null && bookRulePO.getEmail()){
                        pi.add("10");
                    }
                    String passengerInfo = pi.stream().collect(Collectors.joining(","));
                    PassengerTemplatePO passengerTemplatePO = passengerTemplateMapper.getPassengerTemplateByCond(Constants.SUPPLIER_CODE_SHENGHE_TICKET,
                            bookRulePO.getPeopleLimit() == null ? 0 : bookRulePO.getPeopleLimit(), passengerInfo, idInfo);
                    if(passengerTemplatePO == null){
                        passengerTemplatePO = new PassengerTemplatePO();
                        passengerTemplatePO.setChannel(Constants.SUPPLIER_CODE_SHENGHE_TICKET);
                        passengerTemplatePO.setCreateTime(new Date());
                        passengerTemplatePO.setStatus(1);
                        passengerTemplatePO.setIdInfo(idInfo);
                        passengerTemplatePO.setPassengerInfo(passengerInfo);
                        passengerTemplatePO.setPeopleLimit(bookRulePO.getPeopleLimit() == null ? 0 : bookRulePO.getPeopleLimit());
                        passengerTemplatePO.setPayBefore(0);
                        passengerTemplateMapper.addPassengerTemplate(passengerTemplatePO);
                    }
                    groupTourProductMPO.setTravelerTemplateId(passengerTemplatePO.getId());
                    // 笛风云没有退改
                    groupTourProductDao.saveProduct(groupTourProductMPO);
                }
            }

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
                setMealMPO.setTripNight(productPO.getTripNights());
                setMealMPO.setConstInclude(productPO.getIncludeDesc());
                setMealMPO.setCostExclude(productPO.getExcludeDesc());
                StringBuffer sb = new StringBuffer();
                if(StringUtils.isNotBlank(productPO.getBookDesc())){
                    sb.append("预订须知：<br>").append(productPO.getBookDesc()).append("<br>");
                }
                if(ListUtils.isNotEmpty(productPO.getBookNoticeList())){
                    productPO.getBookNoticeList().stream().filter(bn -> StringUtils.isNotBlank(bn.getContent())).forEach(b ->
                        sb.append(b.getTitle()).append("<br>")
                                .append(b.getContent()).append("<br>"));
                }
                setMealMPO.setBookNotice(sb.toString());
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
                        if(StringUtils.isNotBlank(hodometer.getScheduling())){
                            GroupTourProductTripItem item1 = new GroupTourProductTripItem();
                            item1.setType("15");
                            item1.setPoiName("行程安排");
                            item1.setPoiDesc(hodometer.getScheduling());
                            items.add(item1);
                        }
                        if (ListUtils.isNotEmpty(hodometer.getRoutes())) {

                            List<GroupTourHotel> groupTourHotels = hodometer.getRoutes().stream().filter(r -> r.getMduleType() == DfyConstants.MODULE_TYPE_HOTEL).map(r -> {
                                GroupTourHotel groupTourHotel = new GroupTourHotel();
                                groupTourHotel.setDesc(r.getDescribe());
                                groupTourHotel.setHotelName(r.getName());
                                if (ListUtils.isNotEmpty(r.getImages())) {
                                    groupTourHotel.setImages(r.getImages().stream().map(ImageBase::getUrl).collect(Collectors.toList()));
                                }
                                return groupTourHotel;
                            }).collect(Collectors.toList());
                            if(ListUtils.isNotEmpty(groupTourHotels)){
                                GroupTourProductTripItem item1 = new GroupTourProductTripItem();
                                item1.setType("5");
                                item1.setGroupTourHotels(groupTourHotels);
                                items.add(item1);
                            }
                            for (Route route : hodometer.getRoutes()) {
                                int type = route.getMduleType();
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                if (type == DfyConstants.MODULE_TYPE_SCENIC) {
                                    item.setType("1");
                                    item.setPoiName(route.getTitle());
                                } else if (type == DfyConstants.MODULE_TYPE_TRAFFIC) {
                                    String means = "";
                                    if (route.getTransportationType() > 0) {
                                        switch (route.getTransportationType()) {
                                            case 1:
                                                means = "飞机";
                                                item.setType("7");
                                                break;
                                            case 2:
                                                means = "火车";
                                                item.setType("6");
                                                break;
                                            case 3:
                                                means = "轮渡";
                                                item.setType("13");
                                                break;
                                            case 4:
                                                means = "汽车";
                                                item.setType("8");
                                                item.setCarType("汽车");
                                                break;
                                            case 5:
                                            default:
                                                means = "自主";
                                                item.setType("14");
                                                break;
                                        }
                                    }
                                    if (StringUtils.isBlank(means)) {
                                        item.setPoiName(String.format("从%s到%s", route.getDeparture(), route.getArrival()));
                                    } else {
//                                        item.setPoiName(String.format("从%s乘%s到%s", route.getDeparture(), means, route.getArrival()));
                                        // 如果不能确定类型就舍弃这个节点
                                        continue;
                                    }
                                } else if (type == DfyConstants.MODULE_TYPE_FOOD) {
                                    item.setType("3");
                                    item.setPoiName(route.getName());
                                } else if (type == DfyConstants.MODULE_TYPE_SHOPPING) {
                                    item.setPoiName(route.getName());
                                    item.setType("4");
                                } else if (type == DfyConstants.MODULE_TYPE_ACTIVITY) {
                                    item.setType("10");
                                    item.setPoiName(route.getName());
                                } else if(type == DfyConstants.MODULE_TYPE_REMINDER){
                                    item.setType("12");
                                } else {
                                    continue;
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

    @Override
    public void transScenic(){
        List<ProductItemPO> productItemPOs = productItemDao.selectAll();
        for (ProductItemPO productItemPO : productItemPOs) {
            ScenicSpotMPO scenicSpotMPO = new ScenicSpotMPO();
            AddressInfo addressInfo = setCity(null, productItemPO.getCity(), null);
            if(addressInfo != null){
                scenicSpotMPO.setCity(addressInfo.getCityName());
                scenicSpotMPO.setCityCode(addressInfo.getCityCode());
                scenicSpotMPO.setProvince(addressInfo.getProvinceName());
                scenicSpotMPO.setProvinceCode(addressInfo.getProvinceCode());
            }
            scenicSpotMPO.setId(getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            if(ListUtils.isNotEmpty(productItemPO.getImages())){
                scenicSpotMPO.setImages(productItemPO.getImages().stream().map(ImageBasePO::getUrl).collect(Collectors.toList()));
            }
            if(ListUtils.isEmpty(scenicSpotMPO.getImages()) && ListUtils.isNotEmpty(productItemPO.getMainImages())){
                scenicSpotMPO.setImages(productItemPO.getMainImages().stream().map(ImageBasePO::getUrl).collect(Collectors.toList()));
            }
            scenicSpotMPO.setDetailDesc(productItemPO.getDescription());
            if(productItemPO.getItemCoordinate() != null && productItemPO.getItemCoordinate().length == 2){
                Coordinate coordinate = new Coordinate();
                coordinate.setLongitude(productItemPO.getItemCoordinate()[0]);
                coordinate.setLatitude(productItemPO.getItemCoordinate()[1]);
                scenicSpotMPO.setCoordinate(coordinate);
            }
            scenicSpotMPO.setPhone(productItemPO.getPhone());
            if(ListUtils.isNotEmpty(productItemPO.getTopic())){
                StringBuffer sb = new StringBuffer();
                for (BaseCode baseCode : productItemPO.getTopic()) {
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
                scenicSpotMPO.setTheme(theme);
            }
            if(productItemPO.getLevel() != null){
                switch (productItemPO.getLevel()){
                    case 11:
                        scenicSpotMPO.setLevel("1");
                        break;
                    case 12:
                        scenicSpotMPO.setLevel("2");
                        break;
                    case 13:
                        scenicSpotMPO.setLevel("3");
                        break;
                    case 14:
                        scenicSpotMPO.setLevel("4");
                        break;
                    case 15:
                        scenicSpotMPO.setLevel("5");
                        break;
                }
            }
            scenicSpotMPO.setAddress(productItemPO.getAddress());
            scenicSpotMPO.setName(productItemPO.getName());
            if(ListUtils.isNotEmpty(productItemPO.getFeatures())){
                ItemFeaturePO itemFeaturePO = productItemPO.getFeatures().stream().filter(f -> f.getType() == 2).findFirst().orElse(null);
                if(itemFeaturePO != null){
                    scenicSpotMPO.setTraffic(itemFeaturePO.getDetail());
                }
            }
            if(StringUtils.isNotBlank(productItemPO.getBusinessHours())){
                ScenicSpotOpenTime scenicSpotOpenTime = new ScenicSpotOpenTime();
                scenicSpotOpenTime.setTimeDesc(productItemPO.getBusinessHours());
                scenicSpotMPO.setScenicSpotOpenTimes(Lists.newArrayList(scenicSpotOpenTime));
            }
            scenicSpotMPO.setCreateTime(new Date());
            scenicSpotMPO.setProposalPlayTime(productItemPO.getSuggestPlaytime());
            scenicSpotMPO.setOperatingStatus(1);
            scenicSpotMPO.setSpotOfficialWeb(productItemPO.getWebsite());
            scenicSpotMPO.setStatus(1);
            scenicSpotMPO.setTages(productItemPO.getTags());
            scenicSpotMPO.setUpdateTime(new Date());
            // 同时保存映射关系
            updateScenicSpotMapping(productItemPO.getCode(), productItemPO.getSupplierId(), "晟和", scenicSpotMPO);
            // 更新备份
            updateScenicSpotMPOBackup(scenicSpotMPO, productItemPO.getCode(), productItemPO.getSupplierId(), productItemPO);
        }
    }

    @Override
    public void addScenicProductSubscribe(ScenicSpotMPO scenicSpotMPO, ScenicSpotProductMPO scenicSpotProductMPO, boolean fresh){
        if(scenicSpotMPO == null){
            scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotProductMPO.getScenicSpotId());
        }
        List<SubscribeProductMPO> subscribes = subscribeProductDao.getByCategory("d_ss_ticket");
        if(ListUtils.isNotEmpty(subscribes)){
            for (SubscribeProductMPO subscribe : subscribes) {
                // 城市必填，没有不比
                if(StringUtils.isBlank(subscribe.getCityCode()) || !StringUtils.equals(subscribe.getCityCode(), scenicSpotMPO.getCityCode())){
                    continue;
                }
                // 景点id必填，没有不比
                if(StringUtils.isBlank(subscribe.getPoiId()) || !StringUtils.equals(subscribe.getPoiId(), scenicSpotMPO.getId())){
                    continue;
                }
                // 渠道空代表所有，不空的时候比
                if(ListUtils.isNotEmpty(subscribe.getChannelCodes()) && !subscribe.getChannelCodes().contains(scenicSpotProductMPO.getChannel())){
                    continue;
                }
                // 产品id非必填，有就比，没有就过
                if(StringUtils.isNotBlank(subscribe.getProductId()) && StringUtils.equals(subscribe.getProductId(), scenicSpotProductMPO.getId())){
                    continue;
                }
                ProductUpdateNoticeMPO noticeMPO = productUpdateNoticeDao.getUnreadNotice(0, subscribe.getUserId(), scenicSpotProductMPO.getId());
                if(noticeMPO == null){
                    noticeMPO = new ProductUpdateNoticeMPO();
                    noticeMPO.setCreateTime(new Date());
                    noticeMPO.setId(getId(BizTagConst.BIZ_SUBSCRIBE_PRODUCT));
                    noticeMPO.setProductId(scenicSpotProductMPO.getId());
                    noticeMPO.setChannel(scenicSpotProductMPO.getChannel());
                    BackChannelEntry backChannelEntry = backChannelMapper.getChannelInfoByChannelCode(scenicSpotProductMPO.getChannel());
                    noticeMPO.setChannelName(backChannelEntry.getChannelName());
                    noticeMPO.setNoticeStatus(0);
                    noticeMPO.setCategory(scenicSpotProductMPO.getScenicSpotProductBaseSetting().getCategoryCode());
                    noticeMPO.setType(0);
                    noticeMPO.setUserId(subscribe.getUserId());
                    noticeMPO.setScenicId(scenicSpotMPO.getId());
                }
                noticeMPO.setUpdateTime(new Date());
                if(ListUtils.isNotEmpty(scenicSpotProductMPO.getImages())){
                    noticeMPO.setProductImageUrl(scenicSpotProductMPO.getImages().get(0));
                }
                noticeMPO.setTitle(String.format("产品更新通知(%s)", scenicSpotProductMPO.getName()));
                noticeMPO.setProductName(scenicSpotProductMPO.getName());
                noticeMPO.setProductStatus(scenicSpotProductMPO.getStatus());
                noticeMPO.setUpdateType(fresh ? "0" : "1");
                noticeMPO.setScenicName(scenicSpotMPO.getName());
                List<ScenicSpotProductPriceMPO> priceMPOs = scenicSpotProductPriceDao.getByProductId(scenicSpotProductMPO.getId());
                priceMPOs.stream().filter(p ->
                        p.getSettlementPrice() != null).collect(Collectors.toList()).sort(Comparator.comparing(p ->
                        p.getSettlementPrice().doubleValue(), Double::compare));
                noticeMPO.setPrice(priceMPOs.get(0).getSettlementPrice());
                int stock = priceMPOs.stream().mapToInt(p -> p.getStock()).sum();
                noticeMPO.setStock(stock);
                productUpdateNoticeDao.saveProductUpdateNotice(noticeMPO);
            }
        }
    }

    @Override
    public void addHotelProductSubscribe(HotelScenicSpotProductMPO productMPO, HotelScenicSpotProductSetMealMPO setMealMPO, boolean fresh){
        List<SubscribeProductMPO> subscribes = subscribeProductDao.getByCategory("hotel_scenicSpot");
        if(ListUtils.isNotEmpty(subscribes)){
            for (SubscribeProductMPO subscribe : subscribes) {
                List<String> elements = Lists.newArrayList();
                List<HotelScenicSpotProductHotelElement> hotels = setMealMPO.getHotelElements();
                List<HotelScenicSpotProductScenicSpotElement> scenicSpots = setMealMPO.getScenicSpotElements();

                if(ListUtils.isNotEmpty(hotels)){
                    elements.add("1");
                    if(!hotels.stream().anyMatch(h -> StringUtils.equals(h.getHotelId(), subscribe.getHotelId()))){
                        continue;
                    }
                    if(ListUtils.isNotEmpty(scenicSpots)){
                        if(!scenicSpots.stream().anyMatch(s -> StringUtils.equals(s.getScenicSpotId(), subscribe.getPoiId()))){
                            continue;
                        }
                    }
                }
                if(ListUtils.isNotEmpty(scenicSpots)){
                    elements.add("2");
                    if(!scenicSpots.stream().anyMatch(s -> StringUtils.equals(s.getScenicSpotId(), subscribe.getPoiId()))){
                        continue;
                    }
                    if(ListUtils.isNotEmpty(hotels)){
                        if(!hotels.stream().anyMatch(h -> StringUtils.equals(h.getHotelId(), subscribe.getHotelId()))){
                            continue;
                        }
                    }
                }
                if(ListUtils.isNotEmpty(setMealMPO.getOtherElements())){
                    elements.add("7");
                }
                if(ListUtils.isNotEmpty(setMealMPO.getRestaurantElements())){
                    elements.add("4");
                }
                if(ListUtils.isNotEmpty(setMealMPO.getSpecialActivityElements())){
                    elements.add("5");
                }
                if(ListUtils.isNotEmpty(setMealMPO.getSpaElements())){
                    elements.add("6");
                }
                if(ListUtils.isNotEmpty(setMealMPO.getTrafficConnectionElements())){
                    elements.add("3");
                }
                if(ListUtils.isEmpty(subscribe.getElements()) && ListUtils.isNotEmpty(elements)){
                    continue;
                }
                // 只要有一个包含就通过
                if(ListUtils.isNotEmpty(subscribe.getElements()) && ListUtils.isNotEmpty(elements)
                        && !subscribe.getElements().stream().anyMatch(e -> elements.contains(e))){
                    continue;
                }
                // 渠道空代表所有，不用比
                if(ListUtils.isNotEmpty(subscribe.getChannelCodes()) && !subscribe.getChannelCodes().contains(productMPO.getChannel())){
                    continue;
                }
                // 产品id非必填，有就比，没有就过
                if(StringUtils.isNotBlank(subscribe.getProductId()) && StringUtils.equals(subscribe.getProductId(), productMPO.getId())){
                    continue;
                }
                ProductUpdateNoticeMPO noticeMPO = productUpdateNoticeDao.getUnreadNotice(0, subscribe.getUserId(), productMPO.getId());
                if(noticeMPO == null){
                    noticeMPO = new ProductUpdateNoticeMPO();
                    noticeMPO.setCreateTime(new Date());
                    noticeMPO.setId(getId(BizTagConst.BIZ_SUBSCRIBE_PRODUCT));
                    noticeMPO.setProductId(productMPO.getId());
                    noticeMPO.setChannel(productMPO.getChannel());
                    BackChannelEntry backChannelEntry = backChannelMapper.getChannelInfoByChannelCode(productMPO.getChannel());
                    noticeMPO.setChannelName(backChannelEntry.getChannelName());
                    noticeMPO.setNoticeStatus(0);
                    noticeMPO.setCategory(productMPO.getCategory());
                    noticeMPO.setType(2);
                    noticeMPO.setUserId(subscribe.getUserId());
                }
                noticeMPO.setUpdateTime(new Date());
                if(ListUtils.isNotEmpty(productMPO.getImages())){
                    noticeMPO.setProductImageUrl(productMPO.getImages().get(0));
                }
                noticeMPO.setProductName(productMPO.getProductName());
                noticeMPO.setProductStatus(productMPO.getStatus());
                noticeMPO.setUpdateType(fresh ? "0" : "1");
                noticeMPO.setTitle(String.format("产品更新通知(%s)", productMPO.getProductName()));
                List<HotelScenicSpotPriceStock> priceMPOs = setMealMPO.getPriceStocks();
                priceMPOs.stream().filter(p ->
                        p.getAdtSellPrice() != null).collect(Collectors.toList()).sort(Comparator.comparing(p ->
                        p.getAdtSellPrice().doubleValue(), Double::compare));
                noticeMPO.setPrice(priceMPOs.get(0).getAdtSellPrice());
                int stock = priceMPOs.stream().mapToInt(p -> p.getAdtStock()).sum();
                noticeMPO.setStock(stock);
                productUpdateNoticeDao.saveProductUpdateNotice(noticeMPO);
            }
        }
    }

    @Override
    public void addToursProductSubscribe(GroupTourProductMPO groupTourProductMPO, boolean fresh){
        List<SubscribeProductMPO> subscribes = subscribeProductDao.getByCategory("group_tour");
        if(ListUtils.isNotEmpty(subscribes)){
            for (SubscribeProductMPO subscribe : subscribes) {
                // 出发城市必填，没有就不比
                if(StringUtils.isBlank(subscribe.getCityCode()) || ListUtils.isEmpty(groupTourProductMPO.getDepInfos())
                        || !groupTourProductMPO.getDepInfos().stream().map(c ->
                        c.getCityCode()).collect(Collectors.toList()).contains(subscribe.getCityCode())){
                    continue;
                }
                // 目的地城市必填，没有就不比
                if(StringUtils.isBlank(subscribe.getArrCityCode()) || ListUtils.isEmpty(groupTourProductMPO.getArrInfos())
                        || !groupTourProductMPO.getArrInfos().stream().map(c ->
                        c.getCityCode()).collect(Collectors.toList()).contains(subscribe.getArrCityCode())){
                    continue;
                }
                // 渠道空代表所有，不用比
                if(ListUtils.isNotEmpty(subscribe.getChannelCodes()) && !subscribe.getChannelCodes().contains(groupTourProductMPO.getChannel())){
                    continue;
                }
                // 产品id非必填，有就比，没有就过
                if(StringUtils.isNotBlank(subscribe.getProductId()) && StringUtils.equals(subscribe.getProductId(), groupTourProductMPO.getId())){
                    continue;
                }
                ProductUpdateNoticeMPO noticeMPO = productUpdateNoticeDao.getUnreadNotice(1, subscribe.getUserId(), groupTourProductMPO.getId());
                if(noticeMPO == null){
                    noticeMPO = new ProductUpdateNoticeMPO();
                    noticeMPO.setCreateTime(new Date());
                    noticeMPO.setId(getId(BizTagConst.BIZ_SUBSCRIBE_PRODUCT));
                    noticeMPO.setProductId(groupTourProductMPO.getId());
                    noticeMPO.setChannel(groupTourProductMPO.getChannel());
                    BackChannelEntry backChannelEntry = backChannelMapper.getChannelInfoByChannelCode(groupTourProductMPO.getChannel());
                    noticeMPO.setChannelName(backChannelEntry.getChannelName());
                    noticeMPO.setNoticeStatus(0);
                    noticeMPO.setCategory(groupTourProductMPO.getCategory());
                    noticeMPO.setType(1);
                    noticeMPO.setUserId(subscribe.getUserId());
                }
                noticeMPO.setUpdateTime(new Date());
                if(ListUtils.isNotEmpty(groupTourProductMPO.getImages())){
                    noticeMPO.setProductImageUrl(groupTourProductMPO.getImages().get(0));
                }
                noticeMPO.setProductName(groupTourProductMPO.getProductName());
                noticeMPO.setProductStatus(groupTourProductMPO.getStatus());
                noticeMPO.setUpdateType(fresh ? "0" : "1");
                noticeMPO.setTitle(String.format("产品更新通知(%s)", groupTourProductMPO.getProductName()));
                List<GroupTourProductSetMealMPO> setMealMPOs = groupTourProductSetMealDao.getSetMealByProductId(groupTourProductMPO.getId());
                if(ListUtils.isNotEmpty(setMealMPOs)){
                    List<GroupTourPrice> prices = setMealMPOs.stream().flatMap(m -> m.getGroupTourPrices().stream()).collect(Collectors.toList());
                    prices.stream().filter(p ->
                            p.getAdtPrice() != null).collect(Collectors.toList()).sort(Comparator.comparing(p ->
                            p.getAdtPrice().doubleValue(), Double::compare));
                    noticeMPO.setPrice(prices.get(0).getAdtPrice());
                    int stock = prices.stream().mapToInt(p -> p.getAdtStock()).sum();
                    noticeMPO.setStock(stock);
                } else {
                    log.error("产品{}没有套餐信息", groupTourProductMPO.getId());
                }
                productUpdateNoticeDao.saveProductUpdateNotice(noticeMPO);
            }
        }
    }

    @Override
    public void transScenic(List<String> codes){
        List<ProductItemPO> productItemPOs = productItemDao.selectByCodes(codes);
        for (ProductItemPO productItemPO : productItemPOs) {
            boolean n = false;
            ScenicSpotMPO scenicSpotMPO;
            String supplierItemId = productItemPO.getSupplierItemId();
            ScenicSpotMappingMPO mappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(supplierItemId, productItemPO.getSupplierId());
            if(mappingMPO != null){
                scenicSpotMPO = scenicSpotDao.getScenicSpotById(mappingMPO.getScenicSpotId());
            } else {
                scenicSpotMPO = scenicSpotDao.getScenicSpotByNameAndAddress(productItemPO.getName(), null);
                if(scenicSpotMPO == null){
                    scenicSpotMPO = new ScenicSpotMPO();
                    n = true;
                }
            }
            if(StringUtils.isBlank(scenicSpotMPO.getId())){
                scenicSpotMPO.setId(getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                AddressInfo addressInfo = setCity(null, productItemPO.getCity(), null);
                if(addressInfo != null){
                    scenicSpotMPO.setCity(addressInfo.getCityName());
                    scenicSpotMPO.setCityCode(addressInfo.getCityCode());
                    scenicSpotMPO.setProvince(addressInfo.getProvinceName());
                    scenicSpotMPO.setProvinceCode(addressInfo.getProvinceCode());
                }
                scenicSpotMPO.setCreateTime(new Date());
                scenicSpotMPO.setName(productItemPO.getName());
                scenicSpotMPO.setOperatingStatus(1);
                scenicSpotMPO.setAddress(productItemPO.getAddress());
                scenicSpotMPO.setStatus(1);
            }
            List<String> images = Lists.newArrayList();
            if(ListUtils.isNotEmpty(productItemPO.getMainImages())){
                images.addAll(productItemPO.getMainImages().stream().map(ImageBasePO::getUrl).collect(Collectors.toList()));
            }
            if(ListUtils.isNotEmpty(productItemPO.getImages())){
                images.addAll(productItemPO.getImages().stream().map(ImageBasePO::getUrl).collect(Collectors.toList()));
            }
            if(ListUtils.isNotEmpty(images)){
                scenicSpotMPO.setImages(images);
            }
            if(StringUtils.isNotBlank(productItemPO.getDescription())){
                scenicSpotMPO.setCharacteristic(productItemPO.getDescription());
            }

            if(productItemPO.getItemCoordinate() != null
                    && productItemPO.getItemCoordinate().length == 2
                    && scenicSpotMPO.getCoordinate() == null){
                Coordinate coordinate = new Coordinate();
                coordinate.setLongitude(productItemPO.getItemCoordinate()[0]);
                coordinate.setLatitude(productItemPO.getItemCoordinate()[1]);
                scenicSpotMPO.setCoordinate(coordinate);
            }
            if(StringUtils.isBlank(scenicSpotMPO.getPhone())){
                scenicSpotMPO.setPhone(productItemPO.getPhone());
            }

            if(ListUtils.isNotEmpty(productItemPO.getTopic())){
                String theme = null;
                for (BaseCode baseCode : productItemPO.getTopic()) {
                    if(StringUtils.equals(baseCode.getCode(), "1000")){
                        theme = "37";
                    } else if(StringUtils.equals(baseCode.getCode(), "1001")){
                        theme = "36";
                    } else if(StringUtils.equals(baseCode.getCode(), "1002")){
                        theme = "6";
                    } else if(StringUtils.equals(baseCode.getCode(), "1003")){
                        theme = "9";
                    }
                }
                scenicSpotMPO.setTheme(theme);
            }
            if(productItemPO.getLevel() != null){
                switch (productItemPO.getLevel()){
                    case 11:
                        scenicSpotMPO.setLevel("1");
                        break;
                    case 12:
                        scenicSpotMPO.setLevel("2");
                        break;
                    case 13:
                        scenicSpotMPO.setLevel("3");
                        break;
                    case 14:
                        scenicSpotMPO.setLevel("4");
                        break;
                    case 15:
                        scenicSpotMPO.setLevel("5");
                        break;
                }
            }
            if(ListUtils.isNotEmpty(productItemPO.getFeatures())){
                ItemFeaturePO itemFeaturePO = productItemPO.getFeatures().stream().filter(f -> f.getType() == 2).findFirst().orElse(null);
                if(itemFeaturePO != null){
                    scenicSpotMPO.setTraffic(itemFeaturePO.getDetail());
                }

                ItemFeaturePO detail = productItemPO.getFeatures().stream().filter(f -> f.getType() == 3).findFirst().orElse(null);
                if(detail != null && StringUtils.isBlank(scenicSpotMPO.getDetailDesc())){
                    scenicSpotMPO.setDetailDesc(detail.getDetail());
                }

                ItemFeaturePO bookNotice = productItemPO.getFeatures().stream().filter(f -> f.getType() == 1).findFirst().orElse(null);
                ItemFeaturePO important = productItemPO.getFeatures().stream().filter(f -> f.getType() == 4).findFirst().orElse(null);
                ItemFeaturePO tourNotice = productItemPO.getFeatures().stream().filter(f -> f.getType() == 5).findFirst().orElse(null);
                StringBuffer sb = new StringBuffer();
                if(bookNotice != null){
                    sb.append("购买须知：").append("<br>");
                    sb.append(bookNotice.getDetail()).append("<br>");
                }
                if(important != null){
                    sb.append("重要条款：").append("<br>");
                    sb.append(important.getDetail()).append("<br>");
                }
                if(tourNotice != null){
                    sb.append("游玩须知：").append("<br>");
                    sb.append(tourNotice.getDetail()).append("<br>");
                }
                scenicSpotMPO.setImportantDesc(sb.toString());
            }
            if(StringUtils.isBlank(scenicSpotMPO.getProposalPlayTime())){
                scenicSpotMPO.setProposalPlayTime(productItemPO.getSuggestPlaytime());
            }
            if(StringUtils.isBlank(scenicSpotMPO.getSpotOfficialWeb())){
                scenicSpotMPO.setSpotOfficialWeb(productItemPO.getWebsite());
            }
            if(ListUtils.isEmpty(scenicSpotMPO.getTages())){
                scenicSpotMPO.setTages(productItemPO.getTags());
            }
            scenicSpotMPO.setUpdateTime(new Date());
            String supplierName = null;
            if(StringUtils.equals(productItemPO.getSupplierId(), Constants.SUPPLIER_CODE_YCF)){
                supplierName = Constants.SUPPLIER_NAME_YCF;
            }
            if(StringUtils.equals(productItemPO.getSupplierId(), Constants.SUPPLIER_CODE_DFY)){
                supplierName = Constants.SUPPLIER_NAME_DFY;
            }
            if(StringUtils.equals(productItemPO.getSupplierId(), Constants.SUPPLIER_CODE_LMM_TICKET)){
                supplierName = Constants.SUPPLIER_NAME_LMM_TICKET;
            }
            if(n){
                // 同时保存映射关系
                updateScenicSpotMapping(productItemPO.getSupplierItemId(), productItemPO.getSupplierId(), supplierName , scenicSpotMPO);
            } else {
                scenicSpotDao.saveScenicSpot(scenicSpotMPO);
            }
            // 更新备份
            updateScenicSpotMPOBackup(scenicSpotMPO, productItemPO.getSupplierItemId(), productItemPO.getSupplierId(), productItemPO);
        }
    }

    @Override
    public void setPoiReviewCity(){
        int i = 1;
        int size = 100;
        do{
            List<PoiReviewMPO> reviewMPOs = poiReviewDao.getPoiReviewByPage(i, size);
            if(ListUtils.isEmpty(reviewMPOs)){
                break;
            }
            reviewMPOs.forEach(r -> {
                if(StringUtils.isNotBlank(r.getCityName())){
                    List<ChinaCity> cites = chinaCityMapper.getCityByNameAndTypeAndParentId(r.getCityName(), 2, null);
                    if(ListUtils.isNotEmpty(cites)) {
                        ChinaCity cityObj = cites.get(0);
                        r.setCityCode(cityObj.getCode());
                        poiReviewDao.updateCity(r);
                    }
                }
            });
            i++;
        } while (true);
    }
    @Override
    public void upLoadImageToLocal(List<String> ids){
        log.info("处理景点图片信息.....");
        List<ScenicSpotMPO> scenicSpotMPOs;
        if (!CollectionUtils.isEmpty(ids)){
            scenicSpotMPOs = scenicSpotDao.getNetImagesByIds(ids);
        } else {
            scenicSpotMPOs = scenicSpotDao.getNetImages();
        }
        int i = scenicSpotMPOs.size();
        for (ScenicSpotMPO scenicSpotMPO : scenicSpotMPOs){
            log.info("剩余处理数量 ：" + i);
            log.info("处理景点ID :" + scenicSpotMPO.getId());
            scenicSpotDao.updateImagesById(UploadUtil.getNetUrlAndUpload(scenicSpotMPO.getImages()),scenicSpotMPO.getId());
            i--;
        }
        log.info("处理景点图片信息完毕");
    }

    @Override
    public void refreshScenicSpotDetailDesc(List<String> ids){
        log.info("处理景点描述信息.....");
        if (!CollectionUtils.isEmpty(ids)){
            int i = ids.size();
            for (String id : ids){
                log.info("剩余处理数量 ：" + i);
                log.info("处理景点ID :" + id);
                ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(id);
                scenicSpotMPO.setDetailDesc(StringUtil.delHTMLTag(scenicSpotMPO.getDetailDesc()));
                scenicSpotMPO.setDetailDesc(StringUtil.replaceImgSrc(scenicSpotMPO.getDetailDesc()));
                scenicSpotDao.updateDeatailDescById(scenicSpotMPO.getDetailDesc(),scenicSpotMPO.getId());
                i--;
            }
        } else {
            List<ScenicSpotMPO> scenicSpotMPOs = scenicSpotDao.getdetailDesc();
            int i = scenicSpotMPOs.size();
            for (ScenicSpotMPO scenicSpotMPO : scenicSpotMPOs){
                log.info("剩余处理数量 ：" + i);
                log.info("处理景点ID :" + scenicSpotMPO.getId());
                scenicSpotMPO.setDetailDesc(StringUtil.delHTMLTag(scenicSpotMPO.getDetailDesc()));
                scenicSpotMPO.setDetailDesc(StringUtil.replaceImgSrc(scenicSpotMPO.getDetailDesc()));
                scenicSpotDao.updateDeatailDescById(scenicSpotMPO.getDetailDesc(),scenicSpotMPO.getId());
                i--;
            }
        }
        log.info("处理景点描述信息完毕.....");
    }

    @Override
    public String queryCityCodeByName(String cityName) {
        String code = "";
        int len = 0;
        if (StringUtils.isNotEmpty(cityName)) {
            List<String> list = chinaCityMapper.queryCityCodeByName(cityName);
            if (ListUtils.isNotEmpty(list)) {
                for (String s : list) {
                    if (s.length() > len) {
                        len = s.length();
                        code = s;
                    }
                }
            }
        }
        return code;
    }

    @Override
    public void cleanPsTmp(String channel){
        List<GroupTourProductMPO> passengerInfos = groupTourProductDao.getTravelerTemplateIds(channel);
        if(ListUtils.isNotEmpty(passengerInfos)){
            Map<String, String> psMap = Maps.newHashMap();
            for (GroupTourProductMPO passengerInfo : passengerInfos) {
                PassengerTemplatePO pt = passengerTemplateMapper.getById(passengerInfo.getTravelerTemplateId());
                if(pt != null){
                    SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
                    filter.getExcludes().addAll(Arrays.asList("id", "createTime", "updateTime"));
                    String s = JSON.toJSONString(pt, filter);
                    String key = MD5Util.encode(s);
                    if(psMap.containsKey(key)){
                        groupTourProductDao.updateTravelerTemplateId(passengerInfo.getId(), Integer.valueOf(psMap.get(key)));
                        passengerTemplateMapper.removeById(pt.getId());
                    } else {
                        psMap.put(key, String.valueOf(pt.getId()));
                    }
                }
            }
        }
        List<PassengerTemplatePO> pts = passengerTemplateMapper.getByChannel(channel);
        if(ListUtils.isNotEmpty(pts)){
            for (PassengerTemplatePO pt : pts) {
                List<GroupTourProductMPO> groupTourProductMPOS = groupTourProductDao.getTravelerTemplateId(pt.getId());
                if(ListUtils.isEmpty(groupTourProductMPOS)){
                    passengerTemplateMapper.removeById(pt.getId());
                }
            }
        }
    }

    //这种匹配方式有个问题，如果规则有变化就会创建新规则。旧规则还在，价格日历就会有两份，会有问题；所以还采用一个产品一个规则，弊端就是可能会出现大量重复的规则
    @Override
    public ScenicSpotRuleMPO compareRule(String scenicId, String productId, ScenicSpotRuleMPO ruleMPO){
        List<ScenicSpotRuleMPO> ruleMPOs = scenicSpotRuleDao.getScenicSpotRule(scenicId);
        if(ListUtils.isNotEmpty(ruleMPOs)){
            boolean match = false;
            for (ScenicSpotRuleMPO mpo : ruleMPOs) {
                ScenicSpotRuleCompare compareOri = new ScenicSpotRuleCompare();
                BeanUtils.copyProperties(mpo, compareOri);
                ScenicSpotRuleCompare compareTgt = new ScenicSpotRuleCompare();
                BeanUtils.copyProperties(ruleMPO, compareTgt);
                // 对比规则，内容相同可以重复使用，
                if(StringUtils.equals(JSON.toJSONString(compareTgt), JSON.toJSONString(compareOri))){
                    ruleMPO.setId(mpo.getId());
                    match = true;
                    log.info("景点{}产品{}匹配到重复景点规则{}", scenicId, productId, mpo.getId());
                    break;
                }
            }
            // 没匹配到就创建新的
            if(!match){
                log.info("景点{}产品{}没有匹配到重复规则，创建新规则{}", scenicId, productId, ruleMPO.getId());
                scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
            }
        } else {
            log.info("景点{}产品{}还没有规则，创建新规则{}", scenicId, productId, ruleMPO.getId());
            scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
        }
        return ruleMPO;
    }
}
