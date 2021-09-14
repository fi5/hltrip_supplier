package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.common.entity.TicketInfoPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.StringUtil;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.v2.ScenicSpotRuleCompare;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.data.api.ProductDataService;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.*;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRStockRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import com.huoli.trip.supplier.web.dao.*;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ScenicSpotProductBackupDao productBackupDao;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

    @PostConstruct
//    @Async
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
        UBRBaseResponse<UBRTicketList> response = ubrClient.getTicketList(request.getType());
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

    @Override
    public List<UBRVirtualStock> getStock(UBRStockRequest request){
        UBRBaseResponse<List<UBRVirtualStock>> response = ubrClient.getStock(request.getStartAt(), request.getEndAt(), request.getCategory());
        if(response == null){
            log.error("环球影城虚拟库存无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城虚拟库存返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null){
            log.error("环球影城虚拟库存返回空数据");
            return null;
        }
        return response.getData();
    }

    @Override
    public void syncProduct(String type){
        UBRTicketListRequest request = new UBRTicketListRequest();
//        request.setType(type);
        UBRTicketList ubrTicketList = getTicketList(request);
        if(ubrTicketList != null){
            for (Map.Entry<String, Object> stringObjectEntry : ubrTicketList.getProducts().entrySet()) {
                UBRTicketInfo ubrTicketInfo = JSON.parseObject(JSON.toJSONString(stringObjectEntry.getValue()), UBRTicketInfo.class);
                convertToProduct(ubrTicketInfo);
            }
        }
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

    @Override
    public String refreshToken(){
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

    public void convertToProduct(UBRTicketInfo ticketInfo){
        ScenicSpotProductMPO productMPO = productDao.getBySupplierProductId(ticketInfo.getBaseProduct().getCode(), Constants.SUPPLIER_CODE_BTG_TICKET);
        boolean fresh = false;
        UBRBaseProduct baseProduct = ticketInfo.getBaseProduct();
        if(productMPO == null){
            productMPO = new ScenicSpotProductMPO();
            productMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
            productMPO.setCreateTime(new Date());
            productMPO.setIsDel(0);
            productMPO.setSellType(1);
            productMPO.setSupplierProductId(baseProduct.getCode());
            productMPO.setPayServiceType(0);
            productMPO.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
            String scenicId = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_SCENIC_ID);
            productMPO.setScenicSpotId(scenicId);
            fresh = true;
        }
        String productId = productMPO.getId();
        productMPO.setName(ticketInfo.getDescription());
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
        StringBuffer sb = new StringBuffer();
        if(StringUtils.isNotBlank(baseProduct.getSummary())){
            sb.append(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(baseProduct.getSummary()))).append("<br>");
        }
        // 摘要 和说明拼起来
        if(StringUtils.isNotBlank(baseProduct.getDescription())){
            sb.append(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(baseProduct.getDescription())));
        }
        productMPO.setPcDescription(sb.toString());
        productDao.saveProduct(productMPO);
        convertPrice(baseProduct, productId, ruleMPO.getId(), ticketInfo.getPersonType());
        ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = new ScenicSpotProductBackupMPO();
        scenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
        scenicSpotProductBackupMPO.setScenicSpotProduct(productMPO);
        scenicSpotProductBackupMPO.setOriginContent(JSON.toJSONString(ticketInfo));
        productBackupDao.saveScenicSpotProductBackup(scenicSpotProductBackupMPO);
        commonService.refreshList(0, productId, 1, fresh);
    }

    private void convertPrice(UBRBaseProduct baseProduct, String productId, String ruleId, String personType){
        if(ListUtils.isNotEmpty(baseProduct.getPrices())){
            List<UBRVirtualStock> ubrVirtualStocks = getVirtualStock(baseProduct.getPrices());
            List<ScenicSpotProductPriceMPO> priceMPOs = priceDao.getByProductId(productId);
            baseProduct.getPrices().stream().filter(p -> StringUtils.isNotBlank(p.getValue())).forEach(p -> {
                String date = DateTimeUtil.formatDate(DateTimeUtil.parseDate(p.getDatetime()));
                if(DateTimeUtil.parseDate(date).getTime() < DateTimeUtil.trancateToDate(new Date()).getTime()){
                    // 历史库存不更新
                    return;
                }
                ScenicSpotProductPriceMPO priceMPO = priceMPOs.stream().filter(pm -> StringUtils.equals(pm.getStartDate(), date)).findFirst().orElse(null);
                if(priceMPO == null){
                    priceMPO = new ScenicSpotProductPriceMPO();
                    priceMPO.setId(String.valueOf(dataService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT)));
                    priceMPO.setScenicSpotProductId(productId);
                    priceMPO.setScenicSpotRuleId(ruleId);
                    priceMPO.setMerchantCode(baseProduct.getCode());
                    priceMPO.setCreateTime(new Date());
                    priceMPO.setSettlementPrice(new BigDecimal(p.getValue()));
                    priceMPO.setSellPrice(priceMPO.getSettlementPrice());
                    priceMPO.setStartDate(date);
                    priceMPO.setEndDate(priceMPO.getStartDate());
                    priceMPO.setWeekDay("1,2,3,4,5,6,7");
                    if(StringUtils.isBlank(personType)){
                        priceMPO.setTicketKind(String.valueOf(TicketType.TICKET_TYPE_1.getCode()));
                    } else if(StringUtils.equals(personType, UBRConstants.PERSON_TYPE_ADT)){
                        priceMPO.setTicketKind(String.valueOf(TicketType.TICKET_TYPE_2.getCode()));
                    } else if(StringUtils.equals(personType, UBRConstants.PERSON_TYPE_CHD)){
                        priceMPO.setTicketKind(String.valueOf(TicketType.TICKET_TYPE_7.getCode()));
                    } else if(StringUtils.equals(personType, UBRConstants.PERSON_TYPE_OLD)){
                        priceMPO.setTicketKind(String.valueOf(TicketType.TICKET_TYPE_8.getCode()));
                    } else {
                        priceMPO.setTicketKind(String.valueOf(TicketType.TICKET_TYPE_1.getCode()));
                    }

                } else {
                    if(StringUtils.isNotBlank(priceMPO.getStartDate())){
                        priceMPO.setStartDate(DateTimeUtil.formatDate(DateTimeUtil.parseDate(priceMPO.getStartDate())));
                    }
                    if(StringUtils.isNotBlank(priceMPO.getEndDate())){
                        priceMPO.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.parseDate(priceMPO.getEndDate())));
                    }
                    priceMPO.setSellPrice(new BigDecimal(p.getValue()));
                    priceMPO.setSettlementPrice(priceMPO.getSellPrice());
                }
                UBRStock ubrStock = baseProduct.getStocks().stream().filter(s -> StringUtils.equals(s.getDatetime(), p.getDatetime())
                        && StringUtils.isNotBlank(s.getStatus())
                        && StringUtils.equals(s.getStatus(), "normal")).findFirst().orElse(null);
                UBRVirtualStock virtualStock = ubrVirtualStocks.stream().filter(s -> StringUtils.equals(s.getDate(),
                        date)).findFirst().orElse(null);
                if(virtualStock != null){
                    if(virtualStock.getCommonStock() > 0 && ubrStock != null){
                        priceMPO.setStock(virtualStock.getCommonStock());
                    } else {
                        log.info("虚拟库存为0或者神舟库存不是normal都将库存置为0，虚拟库存={}，神舟库存={}", virtualStock.getCommonStock(), ubrStock == null ? "null" : ubrStock.getStatus());
                        priceMPO.setStock(0);
                    }
                } else {
                    if(ubrStock != null){
                        priceMPO.setStock(999);
                    } else {
                        priceMPO.setStock(0);
                    }
                }
                priceMPO.setUpdateTime(new Date());
                priceDao.saveScenicSpotProductPrice(priceMPO);
            });
        }
    }

    private List<UBRVirtualStock> getVirtualStock(List<UBRPrice> prices){
        String startDate = DateTimeUtil.formatDate(new Date());
        String endDate = startDate;
        UBRPrice startPrice = prices.stream().min(Comparator.comparing(p -> DateTimeUtil.parseDate(p.getDatetime()).getTime())).get();
        UBRPrice endPrice = prices.stream().max(Comparator.comparing(p -> DateTimeUtil.parseDate(p.getDatetime()).getTime())).get();
        if(startPrice != null && StringUtils.isNotBlank(startPrice.getDatetime())){
            startDate = DateTimeUtil.formatDate(DateTimeUtil.parseDate(startPrice.getDatetime()));
        }
        if(endPrice != null && StringUtils.isNotBlank(endPrice.getDatetime())){
            endDate = DateTimeUtil.formatDate(DateTimeUtil.parseDate(endPrice.getDatetime()));
        }
        List<UBRVirtualStock> ubrVirtualStocks = syncStock(startDate, endDate);
        if(ubrVirtualStocks == null){
            ubrVirtualStocks = Lists.newArrayList();
        }
        return ubrVirtualStocks;
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
        ruleMPO.setMinCount(StringUtils.isBlank(ticketInfo.getMinQuantity()) ? 1 : Integer.valueOf(ticketInfo.getMinQuantity()));
        ruleMPO.setMaxAge(StringUtils.isBlank(ticketInfo.getPersonTypeMaxAge()) ? 100 : Integer.valueOf(ticketInfo.getPersonTypeMaxAge()));
        ruleMPO.setMinAge(StringUtils.isBlank(ticketInfo.getPersonTypeMinAge()) ? 0 : Integer.valueOf(ticketInfo.getPersonTypeMinAge()));
        if(StringUtils.isNotBlank(ticketInfo.getRefundable()) && StringUtils.equals(ticketInfo.getRefundable(), "true")){
            ruleMPO.setRefundCondition(2);
            RefundRule refundRule = new RefundRule();
            refundRule.setRefundRuleType(1);
            refundRule.setDeductionType(1);
            refundRule.setFee(StringUtils.isBlank(ticketInfo.getServiceFee()) ? 0d : Double.valueOf(ticketInfo.getServiceFee()));
            refundRule.setDay(ticketInfo.getTicketVoidAdvanceDays() == null ? 0 : ticketInfo.getTicketVoidAdvanceDays());
            ruleMPO.setRefundRules(Lists.newArrayList(refundRule));
        } else {
            ruleMPO.setRefundCondition(1);
        }
        ruleMPO.setTicketInfos(Lists.newArrayList(0, 1));
        ruleMPO.setTicketCardTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        ruleMPO.setTravellerInfos(Lists.newArrayList(0, 1, 2));
        ruleMPO.setTravellerTypes(Lists.newArrayList(Certificate.ID_CARD.getCode(), Certificate.PASSPORT.getCode()));
        if(StringUtils.equals(ticketInfo.getMediaType(), "GID/FR")){
            ruleMPO.setVoucherType(9);
//        } else if(StringUtils.equals(ticketInfo.getMediaType(), "QR Code")){  // 不需要二维码。所有都可以通过证件入园
//            ruleMPO.setVoucherType(2);
        } else {
            ruleMPO.setVoucherType(8);
        }
//        return commonService.compareRule(productMPO.getScenicSpotId(), productMPO.getId(), ruleMPO);
        scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
        return ruleMPO;
    }

    @Override
    public List<UBRVirtualStock> syncStock(String startDate, String endDate){
        UBRStockRequest request = new UBRStockRequest();
        request.setCategory("Park");
        request.setStartAt(startDate);
        request.setEndAt(endDate);
        return getStock(request);
    }
}
