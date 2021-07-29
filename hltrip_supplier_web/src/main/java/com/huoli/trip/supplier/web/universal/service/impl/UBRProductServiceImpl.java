package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.v2.ScenicSpotRuleCompare;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.data.api.ProductDataService;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.UBRBaseProduct;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketInfo;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import com.huoli.trip.supplier.web.dao.ScenicSpotDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductPriceDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotRuleDao;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.universal.service.UBRProductService;
import com.xiaoleilu.hutool.lang.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
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
 * 创建日期：2021/7/28<br>
 */
@Service
@Slf4j
public class UBRProductServiceImpl implements UBRProductService {

    @Autowired
    private IUBRClient ubrClient;

    @Autowired
    private RedisTemplate jedisTemplate;


    @Reference(group = "hltrip")
    private DataService dataService;

    @Reference(group = "hltrip")
    private ProductDataService productDataService;

    @Autowired
    private ScenicSpotProductPriceDao priceDao;

    @Autowired
    private ScenicSpotProductDao productDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private ScenicSpotRuleDao ruleDao;

    @Autowired
    private CommonService commonService;

    @PostConstruct
    @Async
    public void checkUserInfo(){
        try {
            Thread.sleep(30000);
            if(!jedisTemplate.hasKey(UBRConstants.AUTH_KEY)) {
                log.info("环球影城token过期，准备重新登录。。");
                getToken();
            } else {
                Long hours = jedisTemplate.getExpire(UBRConstants.AUTH_KEY, TimeUnit.HOURS);
                log.info("环球影城token有效期还有{}小时", hours);
                // token 有效期7天。小于24小时的时候就刷新一下
                if(jedisTemplate.getExpire(UBRConstants.AUTH_KEY, TimeUnit.HOURS) < 24){
                    log.info("环球影城token有效期小于24小时，准备刷新。。");
                    refreshToken();
                    log.info("环球影城token刷新完成。。");
                }
            }
        } catch (Throwable e) {
            log.error("环球影城检查登录信息异常，", e);
        }
    }

    @Override
    public UBRTicketList getTicketList(UBRTicketListRequest request){
        UBRBaseResponse<UBRTicketList> response = ubrClient.getTicketList(request);
        if(response == null){
            log.error("环球影城门票列表无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城门票列表返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null){
            log.error("环球影城门票列表返回空数据");
            return null;
        }
        return response.getData();
    }

    @Override
    public void init(){
        UBRBaseResponse response = ubrClient.init();
        if(response == null){
            log.error("环球影城初始化无返回内容");
            return;
        }
        if(response.getCode() != 200){
            log.error("环球影城初始化返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return;
        }
        if(response.getData() == null){
            log.error("环球影城初始化返回空数据");
            return;
        }
        log.info("环球影城初始化返回：{}", JSON.toJSONString(response));
    }

    public void syncProduct(String type){
        UBRTicketListRequest request = new UBRTicketListRequest();
        request.setType(type);
        UBRTicketList ubrTicketList = getTicketList(request);

    }

    @Override
    public String getToken(){
        String account = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_ACCOUNT);
        String password = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_PASSWORD);
        UBRLoginRequest request = new UBRLoginRequest();
        request.setAccount(account);
        request.setPassword(Base64.encode(password));
        log.info("请求环球影城登录，request={}", JSON.toJSONString(request));
        UBRBaseResponse<UBRLoginResponse> response = ubrClient.login(request);
        if(response == null){
            log.error("环球影城登录无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城登录失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null || response.getData().getAuth() == null
                || StringUtils.isBlank(response.getData().getAuth().getToken())){
            log.error("环球影城没有返回正确的登录信息，code={}, msg={}, data={}",
                    response.getCode(), response.getMsg(), response.getData() == null ? null : JSON.toJSONString(response.getData()));
            return null;
        }
        String token = response.getData().getAuth().getToken();
        log.info("环球影城登录成功，token={}, result={}", token, JSON.toJSONString(response));
        jedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token, (7 * 24), TimeUnit.HOURS);
        return token;
    }

    private String refreshToken(){
        UBRBaseResponse<UBRLoginResponse> response = ubrClient.refreshToken();
        if(response == null){
            log.error("环球影城刷新token无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城刷新token失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null || response.getData().getAuth() == null
                || StringUtils.isBlank(response.getData().getAuth().getToken())){
            log.error("环球影城没有返回正确的鉴权信息，code={}, msg={}, data={}",
                    response.getCode(), response.getMsg(), response.getData() == null ? null : JSON.toJSONString(response.getData()));
            return null;
        }
        String token = response.getData().getAuth().getToken();
        log.info("环球影城刷新token成功，最新token={}, result={}", token, JSON.toJSONString(response));
        jedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token, (7 * 24), TimeUnit.HOURS);
        return token;
    }

    public ScenicSpotProductMPO convertToProduct(UBRTicketInfo ticketInfo){
        ScenicSpotProductMPO productMPO = productDao.getBySupplierProductId(ticketInfo.getBaseProduct().getCode(), Constants.SUPPLIER_CODE_BTG_TICKET);
        if(productMPO == null){
            productMPO = new ScenicSpotProductMPO();
            productMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            productMPO.setCreateTime(new Date());
            productMPO.setIsDel(0);
            productMPO.setSellType(1);
            productMPO.setSupplierProductId(ticketInfo.getBaseProduct().getCode());
            productMPO.setPayServiceType(0);
            productMPO.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
            // todo 景点需要后台创建
            String scenicId = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_SCENIC_ID);
            productMPO.setScenicSpotId(scenicId);
        }
        String scenicId = productMPO.getScenicSpotId();
        String productId = productMPO.getId();
        UBRBaseProduct baseProduct = ticketInfo.getBaseProduct();
        productMPO.setName(baseProduct.getName());
        if(baseProduct.getPurchasable() != null && baseProduct.getPurchasable()){
            productMPO.setStatus(1);
        } else {
            productMPO.setStatus(3);
        }
        productMPO.setUpdateTime(new Date());
        ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
        BackChannelEntry backChannelEntry = commonService.getSupplierById(productMPO.getChannel());
        if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
            baseSetting.setAppSource(backChannelEntry.getAppSource());
        }
        // 默认当前
        baseSetting.setLaunchDateTime(new Date());
        // 默认及时
        baseSetting.setLaunchType(1);
        baseSetting.setStockCount(0);
        baseSetting.setCategoryCode("d_ss_ticket");
        productMPO.setScenicSpotProductBaseSetting(baseSetting);
        // 交易设置
        productMPO.setScenicSpotProductTransaction(new ScenicSpotProductTransaction());

        ScenicSpotRuleMPO ruleMPO = convertToRule(productMPO, ticketInfo);
        productMPO.setRuleId(ruleMPO.getId());
        productMPO.setPcDescription(ticketInfo.getDescription());


        if(ListUtils.isNotEmpty(baseProduct.getPrices())){
            baseProduct.getPrices().stream().filter(p -> StringUtils.isNotBlank(p.getValue())).map(p -> {
                ScenicSpotProductPriceMPO priceMPO = new ScenicSpotProductPriceMPO();
                priceMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
                priceMPO.setScenicSpotProductId(productId);
                priceMPO.setScenicSpotRuleId(ruleMPO.getId());
                priceMPO.setUpdateTime(new Date());
                priceMPO.setCreateTime(new Date());
                priceMPO.setSettlementPrice(new BigDecimal(p.getValue()));
                priceMPO.setSellPrice(priceMPO.getSettlementPrice());
                priceMPO.setStartDate(p.getDatetime());
                priceMPO.setEndDate(priceMPO.getStartDate());
                baseProduct.getStocks().stream().filter(s -> StringUtils.equals(s.getDatetime(), p.getDatetime())
                        && StringUtils.isNotBlank(s.getStatus())
                        && StringUtils.equals(s.getStatus(), "normal")).findFirst().ifPresent(s -> {
                    priceMPO.setStock(999);
                });
                return priceMPO;
            }).filter(price -> price.getStock() > 0).collect(Collectors.toList());
        }
        return null;
    }

    private ScenicSpotRuleMPO convertToRule(ScenicSpotProductMPO productMPO, UBRTicketInfo ticketInfo){
        ScenicSpotRuleMPO ruleMPO;
        if(StringUtils.isBlank(productMPO.getRuleId())){
            ruleMPO = new ScenicSpotRuleMPO();
            ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            ruleMPO.setRuleName("退改规则");
            ruleMPO.setScenicSpotId(productMPO.getScenicSpotId());
            ruleMPO.setRuleCode(String.valueOf(System.currentTimeMillis()));
            ruleMPO.setIsCouponRule(0);
            ruleMPO.setChannel(productMPO.getChannel());
            ruleMPO.setValid(1);
            ruleMPO.setCreateTime(new Date());
            ruleMPO.setRefundCondition(2);
        } else {
            ruleMPO = ruleDao.getScenicSpotRuleById(productMPO.getRuleId());
        }
        ruleMPO.setMaxCount(StringUtils.isBlank(ticketInfo.getMaxQuantity()) ? 99 : Integer.valueOf(ticketInfo.getMaxQuantity()));
        // todo 是否需要最小购买数量
        // todo 是否需要最大最小年龄
        // todo 没有
        //      ticketCategory	string 票类别：Park Ticket, Express, VIP Experiences, Annual Pass 目前只有单日票
        if(StringUtils.isNotBlank(ticketInfo.getRefundable()) && StringUtils.equals(ticketInfo.getRefundable(), "true")){
            ruleMPO.setRefundCondition(2);
            RefundRule refundRule = new RefundRule();
            // todo 其它，供应商没有类型
            refundRule.setRefundRuleType(5);
            refundRule.setDeductionType(1);
            refundRule.setFee(StringUtils.isBlank(ticketInfo.getServiceFee()) ? 0d : Double.valueOf(ticketInfo.getServiceFee()));
            ruleMPO.setRefundRules(Lists.newArrayList(refundRule));
        } else {
            ruleMPO.setRefundCondition(1);
        }
        ruleMPO.setTicketInfos(Lists.newArrayList(0, 1, 2));
        ruleMPO.setTicketCardTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        ruleMPO.setTravellerInfos(Lists.newArrayList(0, 1));
        ruleMPO.setTravellerTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        // todo 缺少证件或人脸识别
        if(StringUtils.equals(ticketInfo.getMediaType(), "GID/FR")){
            ruleMPO.setVoucherType();
        } else if(StringUtils.equals(ticketInfo.getMediaType(), "QR Code")){
            ruleMPO.setVoucherType(0);
        } else {
            ruleMPO.setVoucherType(5);
        }

        List<ScenicSpotRuleMPO> ruleMPOs = ruleDao.getScenicSpotRule(productMPO.getScenicSpotId());
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
                    log.info("景点{}产品{}匹配到重复景点规则{}", productMPO.getScenicSpotId(), productMPO.getId(), mpo.getId());
                    break;
                }
            }
            // 没匹配到就创建新的
            if(!match){
                log.info("景点{}产品{}没有匹配到重复规则，创建新规则{}", productMPO.getScenicSpotId(), productMPO.getId(), ruleMPO.getId());
                ruleDao.saveScenicSpotRule(ruleMPO);
            }
        } else {
            log.info("景点{}产品{}还没有规则，创建新规则{}", productMPO.getScenicSpotId(), productMPO.getId(), ruleMPO.getId());
            ruleDao.saveScenicSpotRule(ruleMPO);
        }
        return ruleMPO;
    }
}
