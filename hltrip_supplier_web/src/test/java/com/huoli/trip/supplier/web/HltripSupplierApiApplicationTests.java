package com.huoli.trip.supplier.web;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.CoordinateUtil;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfGetPriceRequest;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import com.huoli.trip.supplier.web.yaochufa.task.SyncPriceTask;
import com.huoli.trip.supplier.web.task.RefreshItemTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
class HltripSupplierApiApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private YcfSyncService ycfSyncService;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private SyncPriceTask syncPriceTask;

    @Autowired
    private RefreshItemTask refreshItemTask;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private DfySyncService dfySyncService;

    //    @Test
    void contextLoads() {
    }

    //    @Test
    public void test0(){
        List<ProductItemPO> productItemPOList =  mongoTemplate.find(Query.query(Criteria.where("itemCoordinate").ne("").ne(null)), ProductItemPO.class);
        productItemPOList.forEach(productItemPO -> {
            if(productItemPO.getItemCoordinate() != null && productItemPO.getItemCoordinate().length == 2){
                try {
                    double[] d = CoordinateUtil.bd09_To_Gcj02(productItemPO.getItemCoordinate()[1], productItemPO.getItemCoordinate()[0]);
                    mongoTemplate.updateFirst(Query.query(Criteria.where("code").is(productItemPO.getCode())),
                            Update.update("itemCoordinate", new Double[]{d[1],d[0]}), productItemPO.getClass());
                } catch (Exception e) {
                    log.error("??????", e);
                }
            }
        });
    }

    //    @Test
    public void test1(){
        YcfGetPriceRequest request = new YcfGetPriceRequest();
        request.setEndDate("2020-10-20");
        request.setProductID("913850_2101767");
        request.setPartnerProductID("yaochufa_913850_2101767");
        request.setStartDate("2020-08-20");
        ycfSyncService.getPrice(request);
    }

    //    @Test
    public void test2(){
        List<ProductItemPO> items = mongoTemplate.findAll(ProductItemPO.class);
        items.forEach(item -> {
            StringBuilder sb = new StringBuilder();
            if(item.getFeatures() != null){
                item.getFeatures().stream().filter(f ->
                        f.getType() == 1 && StringUtils.isNotBlank(f.getDetail())).findFirst().ifPresent(f ->
                {
                    String s = format(f.getDetail());
                    if(StringUtils.isNotBlank(s)){
                        sb.append("true");
                        f.setDetail(s);
                    }
                });
            }
            if(StringUtils.equals("true", sb.toString())){
                log.info("??????{}?????????????????????", item.getCode());
                mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(item.getId())), Update.update("features", item.getFeatures()), ProductItemPO.class);
            }
        });
        log.info("???????????????????????????????????????");
    }

    //    @Test
    public void test3(){
        PricePO pricePO = priceDao.getByProductCode("yaochufa_909604_2094577");

        List<PriceInfoPO> priceInfoPOs = pricePO.getPriceInfos();
        log.info("???????????????{}", JSON.toJSONString(priceInfoPOs));
        priceInfoPOs.sort(Comparator.comparing(po -> po.getSaleDate().getTime(), Long::compareTo));
        log.info("????????????????????????????????????????????????????????????{}", JSON.toJSONString(priceInfoPOs));
    }

    //    @Test
    public void test4(){
        mongoTemplate.updateMulti(Query.query(Criteria.where("createTime").is(null)), Update.update("createTime", MongoDateUtils.handleTimezoneInput(new Date())), PricePO.class);
        mongoTemplate.updateMulti(Query.query(Criteria.where("createTime").is(null)), Update.update("createTime", MongoDateUtils.handleTimezoneInput(new Date())), ProductPO.class);
        mongoTemplate.updateMulti(Query.query(Criteria.where("createTime").is(null)), Update.update("createTime", MongoDateUtils.handleTimezoneInput(new Date())), ProductItemPO.class);
    }

    //    @Test
    public void test5(){
        syncPriceTask.syncFullPrice();
    }

    //    @Test
    public void test6(){
        refreshItemTask.refreshItemProduct();
    }

//    @Test
    public void test7(){
        dynamicProductItemService.refreshItemByCode("yaochufa_188031");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void test8(){
        DfyScenicListRequest request = new DfyScenicListRequest();
        request.setPage(1);
        request.setPageSize(2);
        request.setKey("ceshi..");
        dfySyncService.syncScenicList(request);
    }

    public static void main0(String[] args){
        HltripSupplierApiApplicationTests test = new HltripSupplierApiApplicationTests();
        log.info(test.format(htmlStr));
    }

    public static void main(String[] args){
        System.out.println(-17 % 5);
        System.out.println(-17 % -5);
    }

    private String format(String htmlStr){
        Document document = Jsoup.parse(htmlStr);
        Elements elements0 = document.getElementsContainingOwnText("????????????");
        if(elements0 == null){
            return null;
        }
        Elements elements1 = elements0.parents();
        if(elements1 == null){
            return null;
        }
        Elements elements2 = elements1.parents();
        if(elements2 == null){
            return null;
        }
        Element element = elements2.first();
        if(element == null){
            return null;
        }
        if(StringUtils.equals(element.tagName(), "div")){
            element.remove();
        }
        return document.toString();
    }


    private static String htmlStr = "<div class=\"bottom-height\"><h5><strong>??????????????????</strong></h5><ul class=\"des1 descolor\"><li><br />????????????????????????6??????<br />????????????+?????????/????????????????????????1???+??????1???+??????1??? <br />???????????????12:00~18:00 ???????????? ?????????????????????????????????????????????10????????????20??????????????????30??????????????????????????????????????????10??????<br />???????????????12:00~18:00 ????????????<br />K??????????????????????????????7????????????<br />???????????????????????????????????????????????????????????????                       ??????1???+??????2???+??????1???+??????1???+????????????   <br />??????????????????????????????????????????50???????????????????????????????????????80????????????<br />??????????????????????????????????????????????????????????????????????????????<br />?????????19:00~02:00 ???????????????)<br />??????????????????????????????????????????????????????</li></ul></div><div class=\"bottom-height\"><h5><strong>?????????????????????</strong></h5><ul class=\"des1 descolor\"><li>???????????????-2020???08???31???</li></ul></div><div class=\"bottom-height\"><h5><strong>??????????????????</strong></h5><ul class=\"des1 descolor\"><li>????????????:2020???07???23??? 00:20--2020???08???10??? 00:20</li></ul></div><div class=\"bottom-height\"><h5><strong>??????????????????</strong></h5><ul class=\"des1 descolor\"><li>1????????????????????????????????????????????????????????????????????????????????????<br />2??????????????????????????????????????????150???????????????<br />3??????????????????????????????????????????????????????<br />4????????????????????????3?????????????????????1~3??????<br />5???????????????????????????????????????????????????????????????????????????????????????????????????<br />6?????????????????????????????????????????????????????????????????????-??????????????????????????????<br /></li></ul></div><div class=\"bottom-height\"><h5><strong>??????????????????</strong></h5><ul class=\"des1 descolor\"><li><section><section><section><section style=\"box-sizing: border-box;\"><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; color: rgb(166, 25, 11); line-height: 2; letter-spacing: 2px; box-sizing: border-box;\"><span style=\"color: rgb(89, 89, 89);\">???????????????</span></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><section class=\"_135editor\"><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><span style=\"font-size: 14px; color: rgb(89, 89, 89);\">????????????????????????</span></p><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><span style=\"font-size: 14px; color: rgb(89, 89, 89);\">????????????????????????</span></p><p style=\"max-width: 100%; box-sizing: border-box; min-height: 1em; overflow-wrap: break-word !important;\"><br/></p><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><span style=\"font-size: 14px; color: rgb(89, 89, 89);\">??????????????????????????????????????????</span></p><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><span style=\"font-size: 14px; color: rgb(89, 89, 89);\">??????</span><span style=\"font-size: 14px;\"><strong><span style=\"color: rgb(216, 40, 33);\">K???????????????KTV</span></strong></span></p><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><strong><span style=\"font-size: 14px; color: rgb(216, 40, 33);\">K???????????????</span></strong><span style=\"font-size: 14px;\"><br/></span></p><p style=\"text-align: center; max-width: 100%; box-sizing: border-box; min-height: 1em; line-height: 2em; overflow-wrap: break-word !important;\"><span style=\"font-size: 14px; color: rgb(89, 89, 89);\">???????????????????????????</span></p><section class=\"_135editor\" style=\"text-align: center; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_17c6e6ed20200724094833.jif\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section><section class=\"_135editor\"><section style=\"text-align: center; padding-bottom: 1em; box-sizing: border-box;\"><section style=\"display: inline-block; text-align: right;\"><section style=\"display: inline-block; width: 15px; height: 1px; background: rgb(51, 51, 51); overflow: hidden; transform: rotate(-35deg);\"></section><section style=\"clear: both; border: 1px solid rgb(51, 51, 51); box-shadow: rgb(140, 18, 18) 4px 4px 0px; margin-top: -12px; box-sizing: border-box;\"><section class=\"135brush\" style=\"text-align: center; letter-spacing: 2px; padding: 4px 1.5em; font-weight: bold; font-size: 18px; text-shadow: rgb(140, 18, 18) 0px 2px 2px; box-sizing: border-box;\">K???????????????KTV</section></section><section style=\"width: 15px; height: 1px; background: rgb(51, 51, 51); margin-top: -6px; transform: rotate(-35deg);\"><br/></section></section></section></section></section><section class=\"_135editor\" style=\"text-align: center; margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_f726d65e20200724094833.jpg\" title=\"UEditorImages_68419455f68889db23c0b42a8ddc0eb8.jpg\" alt=\"UEditorImages_68419455f68889db23c0b42a8ddc0eb8.jpg\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section><section class=\"_135editor\" style=\"text-align: center; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><section class=\"_135editor\" style=\"margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_4dca92c120200724094832.jif\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section></section></section><p style=\"box-sizing: border-box;\"><br/></p><p style=\"box-sizing: border-box;\"><br/></p><section class=\"_135editor\"><section style=\"text-align: center; padding-bottom: 1em; box-sizing: border-box;\"><section style=\"display: inline-block; text-align: right;\"><section style=\"display: inline-block; width: 15px; height: 1px; background: rgb(51, 51, 51); overflow: hidden; transform: rotate(-35deg);\"></section><section style=\"clear: both; border: 1px solid rgb(51, 51, 51); box-shadow: rgb(140, 18, 18) 4px 4px 0px; margin-top: -12px; box-sizing: border-box;\"><section class=\"135brush\" style=\"text-align: center; letter-spacing: 2px; padding: 4px 1.5em; font-weight: bold; font-size: 18px; text-shadow: rgb(140, 18, 18) 0px 2px 2px; box-sizing: border-box;\">?????????????????????</section></section><section style=\"width: 15px; height: 1px; background: rgb(51, 51, 51); margin-top: -6px; overflow: hidden; transform: rotate(-35deg);\"></section></section></section></section><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; margin-right: 0px; margin-bottom: 10px; margin-left: 0px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; background-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"margin-top: 10px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section class=\"tn-yzk-fuid-text-13043-1547648912941\" style=\"text-align: left; font-size: 20px; color: rgb(255, 255, 255); letter-spacing: 3px; padding-right: 13px; padding-left: 13px; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><span style=\"text-shadow: rgb(214, 0, 74) 2px 0px 2px; box-sizing: border-box; font-family: Optima-Regular, PingFangTC-light;\"><strong style=\"box-sizing: border-box;\">??&nbsp;<em style=\"box-sizing: border-box;\">K????????????KTV</em></strong></span></p></section></section><section style=\"margin-top: 6px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_0b73286e20200724094833.jpg\" title=\"af9256359f04c2644862780d5d73523.jpg\" alt=\"af9256359f04c2644862780d5d73523.jpg\" style=\"vertical-align: middle; width: 722.773px; box-sizing: border-box;\"/></section></section><section style=\"text-align: justify; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">K????????????KTV</p><p style=\"box-sizing: border-box;\">?????????????????????????????????????????????</p><p style=\"box-sizing: border-box;\">?????????????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_1d6d365820200724094832.jpg\" title=\"408f5b7f74cb9c5e5af17ea773d9b40.jpg\" alt=\"408f5b7f74cb9c5e5af17ea773d9b40.jpg\" style=\"vertical-align: middle; width: 722.773px; box-sizing: border-box;\"/></section></section><section class=\"_135editor\" style=\"text-align: right; margin-top: -30px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; border-style: solid; border-width: 3px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"text-align: center; margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 716.797px; border-style: none; border-width: 2px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_db44bbdd20200724094832.jpg\" title=\"c3fbff71ebc8ea84418424352a65437.jpg\" alt=\"c3fbff71ebc8ea84418424352a65437.jpg\" style=\"vertical-align: middle; width: 716.797px; box-sizing: border-box;\"/></section></section></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">?????????????????????????????????</p><p style=\"box-sizing: border-box;\">?????????????????????????????????</p><p style=\"box-sizing: border-box;\">??????????????????????????????</p><p style=\"box-sizing: border-box;\">???????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; border-width: 0px; border-radius: 0px; border-style: solid; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_dacad40f20200724094833.jpg\" title=\"fa20d574771937936b1568888f74c1e.jpg\" alt=\"fa20d574771937936b1568888f74c1e.jpg\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; line-height: 2; letter-spacing: 2px; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\"><span style=\"color: rgb(166, 25, 11); box-sizing: border-box;\">JBL BMB??????????????????</span></strong></p><p style=\"box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\"><span style=\"color: rgb(166, 25, 11); box-sizing: border-box;\">?????????????????????</span></strong></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">????????????????????????????????????</p><p style=\"box-sizing: border-box;\">??????????????????????????????~</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><section style=\"box-sizing: border-box;\"><section style=\"box-sizing: border-box; transform: rotate(0deg);\"><section style=\"box-sizing: border-box;\"><section style=\"display: inline-block; vertical-align: top; width: 361.387px; box-sizing: border-box;\"><section style=\"text-align: center; margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; border-style: solid; border-width: 2px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_2970d05720200724094833.jpg\" title=\"eed00a0a2a1dded4f05ad5b1e8bf610.jpg\" alt=\"eed00a0a2a1dded4f05ad5b1e8bf610.jpg\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section><section style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section></section><section style=\"display: inline-block; vertical-align: top; width: 361.387px; box-sizing: border-box;\"><section style=\"text-align: center; margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; border-style: solid; border-width: 2px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_385cdeae20200724094833.jpg\" title=\"b51c13e5a21771e0d31313f6df1bd48.jpg\" alt=\"b51c13e5a21771e0d31313f6df1bd48.jpg\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section></section></section></section></section><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\"><span style=\"color: rgb(166, 25, 11); box-sizing: border-box;\">360?????????????????????</span></strong><br/></p><p style=\"box-sizing: border-box;\">???????????????????????????</p><p style=\"box-sizing: border-box;\">????????????????????????????????????</p><p style=\"box-sizing: border-box;\">?????????????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; margin-right: 0px; margin-bottom: 10px; margin-left: 0px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; background-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"margin-top: 20px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_3ade9d1e20200724094833.jpg\" title=\"4e8757a196e27936c6fd14762829a03.jpg\" alt=\"4e8757a196e27936c6fd14762829a03.jpg\" style=\"vertical-align: middle; box-sizing: border-box; width: 722.773px; height: auto;\"/></section></section><section style=\"text-align: justify; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">KTV??????????????????<br/></p><p style=\"box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\"><span style=\"color: rgb(166, 25, 11); box-sizing: border-box;\">???????????????????????????????????????</span></strong><br/></p><p style=\"box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\"><span style=\"color: rgb(166, 25, 11); box-sizing: border-box;\">?????????????????????????????????????????????</span></strong></p><p style=\"box-sizing: border-box;\">??????????????????<br/></p><p style=\"box-sizing: border-box;\">?????????????????????????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_68b3061820200724094832.jpg\" title=\"b2dd0315e73a533a0be6b632f522f25.jpg\" alt=\"b2dd0315e73a533a0be6b632f522f25.jpg\" style=\"vertical-align: middle; width: 722.773px; box-sizing: border-box;\"/></section></section><section class=\"_135editor\" style=\"text-align: right; margin-top: -50px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; border-style: solid; border-width: 3px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"text-align: center; margin: 10px 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 716.797px; border-style: none; border-width: 2px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_cab3bfc520200724094833.jpg\" title=\"7b82805eb64821a9abe0490966a6f15.jpg\" alt=\"7b82805eb64821a9abe0490966a6f15.jpg\" style=\"vertical-align: middle; width: 716.797px; box-sizing: border-box; height: 252px;\"/></section></section></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">??????????????????????????????????????????</p><p style=\"box-sizing: border-box;\">???K??????????????????????????????</p><p style=\"box-sizing: border-box;\">??????????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; margin-right: 0px; margin-bottom: 10px; margin-left: 0px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; background-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"margin-top: 10px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section class=\"tn-yzk-fuid-text-13043-1547648912941\" style=\"text-align: left; font-size: 20px; color: rgb(255, 255, 255); letter-spacing: 3px; padding-right: 13px; padding-left: 13px; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><span style=\"text-shadow: rgb(214, 0, 74) 2px 0px 2px; box-sizing: border-box; font-family: Optima-Regular, PingFangTC-light;\"><strong style=\"box-sizing: border-box;\">??&nbsp;<em style=\"box-sizing: border-box;\">???????????????</em></strong></span></p></section></section><section style=\"margin-top: 6px; margin-right: 0px; margin-left: 0px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_9a7b0b0c20200724094833.jpg\" title=\"4e8757a196e27936c6fd14762829a03.jpg\" alt=\"4e8757a196e27936c6fd14762829a03.jpg\" style=\"vertical-align: middle; width: 722.773px; box-sizing: border-box;\"/></section></section><section style=\"margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; width: 722.773px; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_456c355820200724094833.jpg\" title=\"713d2a5e57e3d2371c77ce2886bd860.jpg\" alt=\"713d2a5e57e3d2371c77ce2886bd860.jpg\" style=\"vertical-align: middle; width: 722.773px; box-sizing: border-box;\"/></section></section><section style=\"text-align: justify; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section></section></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">????????????????????????????????????</p><p style=\"box-sizing: border-box;\">??????????????????</p><p style=\"box-sizing: border-box;\">??????????????????????????????????????????</p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"text-align: center; margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><img class=\"raw-image\" src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_57534afe20200724094832.jpg\" title=\"44fd4e40f4b50ce4ad867969e28c736.jpg\" alt=\"44fd4e40f4b50ce4ad867969e28c736.jpg\" style=\"width: auto; vertical-align: middle; box-sizing: border-box;\"/></section></section><section class=\"_135editor\" style=\"text-align: center; font-size: 14px; color: rgb(166, 25, 11); letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p><p style=\"box-sizing: border-box;\"><span style=\"text-decoration-line: underline; box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\">??????????????????</strong></span></p><p style=\"box-sizing: border-box;\"><span style=\"text-decoration-line: underline; box-sizing: border-box;\"><strong style=\"box-sizing: border-box;\">???????????????????????????????????????</strong></span></p></section><section class=\"_135editor\" style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section class=\"_135editor\" style=\"margin: 30px 0px 10px; box-sizing: border-box;\"><section style=\"display: inline-block; width: 722.773px; vertical-align: top; border-style: solid; border-width: 4px; border-radius: 0px; border-color: rgb(166, 25, 11); box-sizing: border-box;\"><section style=\"box-sizing: border-box;\"><section style=\"display: inline-block; width: 714.805px; vertical-align: top; padding: 20px 10px; box-sizing: border-box;\"><section style=\"text-align: center; margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box; line-height: 2em;\"><span style=\"font-size: 18px;\"><strong><span style=\"color: rgb(89, 89, 89);\">???K????????????KTV???<br/></span></strong></span></p><section class=\"_135editor\" style=\"margin-top: 10px; margin-bottom: 10px; box-sizing: border-box;\"><section style=\"max-width: 100%; vertical-align: middle; display: inline-block; line-height: 0; box-sizing: border-box;\"><p style=\"line-height: 2em;\"><img src=\"https://ykz-cdn1-https.jinxidao.com/image/UEditorImages_b22b538a20200724094833.jpg\" title=\"af9256359f04c2644862780d5d73523.jpg\" alt=\"af9256359f04c2644862780d5d73523.jpg\" style=\"width: auto;\"/></p></section></section><p style=\"box-sizing: border-box; line-height: 2em;\"><span style=\"color: rgb(89, 89, 89);\">????????????????????????????????????????????????????????????</span></p><p style=\"box-sizing: border-box; line-height: 2em;\"><span style=\"color: rgb(89, 89, 89);\">??????????????????????????????????????????</span></p></section><section style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section style=\"font-size: 14px; line-height: 2; color: rgb(166, 25, 11); letter-spacing: 2px; box-sizing: border-box;\"><p style=\"box-sizing: border-box; line-height: 2em;\"><strong style=\"box-sizing: border-box;\">#????????????#</strong></p></section><section style=\"font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"color: rgb(62, 62, 62); line-height: 2em; font-family: &quot;Helvetica Neue&quot;, Helvetica, &quot;Hiragino Sans GB&quot;, &quot;Microsoft YaHei&quot;, &quot;Apple Color Emoji&quot;, &quot;Emoji Symbols Font&quot;, &quot;Segoe UI Symbol&quot;, Arial, sans-serif;\"><span style=\"color: rgb(89, 89, 89);\"></span>??????????????????????????????????????????15???C???</p><p style=\"color: rgb(62, 62, 62); line-height: 2em; font-family: &quot;Helvetica Neue&quot;, Helvetica, &quot;Hiragino Sans GB&quot;, &quot;Microsoft YaHei&quot;, &quot;Apple Color Emoji&quot;, &quot;Emoji Symbols Font&quot;, &quot;Segoe UI Symbol&quot;, Arial, sans-serif;\">???1978?????????????????????<span style=\"color: rgb(89, 89, 89);\"></span></p></section><section style=\"box-sizing: border-box;\"><p style=\"box-sizing: border-box;\"><br/></p></section><section style=\"font-size: 14px; color: rgb(166, 25, 11); line-height: 2; letter-spacing: 2px; box-sizing: border-box;\"><p style=\"box-sizing: border-box; line-height: 2em;\"><strong style=\"box-sizing: border-box;\">#????????????#</strong></p></section><section style=\"font-size: 14px; letter-spacing: 2px; line-height: 2; box-sizing: border-box;\"><p style=\"box-sizing: border-box; line-height: 2em;\"><span style=\"color: rgb(89, 89, 89);\">020-32160448</span></p></section></section></section></section><section style=\"box-sizing: border-box;\"><section style=\"display: inline-block; width: 714.805px; vertical-align: top; background-color: rgb(166, 25, 11); padding: 8px 10px; box-sizing: border-box;\"><section style=\"margin-right: 0px; margin-left: 0px; text-align: right; box-sizing: border-box;\"><section style=\"display: inline-block; vertical-align: top; box-sizing: border-box;\"><section style=\"display: inline-block; vertical-align: middle; color: rgb(255, 255, 255); font-size: 14px; line-height: 2; letter-spacing: 2px; box-sizing: border-box;\"><p style=\"box-sizing: border-box;\">K????????????KTV</p></section><section style=\"width: 0px; border-left: 8px solid rgb(166, 25, 11); border-top: 5px solid transparent; border-bottom: 5px solid transparent; margin-right: 3px; margin-left: 5px; display: inline-block; vertical-align: middle; box-sizing: border-box; height: 10px; overflow: hidden;\"></section><section style=\"width: 0px; border-left: 8px solid rgb(166, 25, 11); border-top: 4px solid transparent; border-bottom: 4px solid transparent; display: inline-block; vertical-align: middle; margin-right: -9px; box-sizing: border-box; height: 8px; overflow: hidden; transform: rotate(0deg);\"></section><section style=\"width: 0px; border-left: 10px solid rgb(166, 25, 11); border-top: 6px solid transparent; border-bottom: 6px solid transparent; display: inline-block; vertical-align: middle; box-sizing: border-box; height: 12px; overflow: hidden;\"></section></section></section></section></section></section></section></section></section><p style=\"text-align: center;\"><br/></p></section><section class=\"_135editor\"><p><br/></p></section></section><p><br style=\"white-space: normal;\"/></p><p><br/></p></li></ul></div>";
}
