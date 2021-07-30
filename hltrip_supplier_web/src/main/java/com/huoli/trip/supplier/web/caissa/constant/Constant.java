package com.huoli.trip.supplier.web.caissa.constant;

import com.google.common.collect.Lists;
import com.huoli.trip.supplier.web.caissa.dto.SubDataDto;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public interface Constant {

    String CAISSA_APP_LIST = "https://m.caissa.com.cn/Searchlist/ajaxList?isSearch=1&start=%d&continent=10&country=10100&countryName=中国&travel_days=1,99&source_id=team_list,caissa_cruise,erp_cruise,team_supplier_list,caissa_lines,zyx_product&keyword=中国&pro_types=参团游,";

    String CAISSA_APP_DETAIL_URL = "http://m.caissa.com.cn/group/index/details?id=%s";

    String EXTERNAL_DETAIL_URL = "http://m.caissa.com.cn/group/External/details?id=%s&team_source=2";

    String FEE_URL = "https://m.caissa.com.cn/group/Index/details_fyyd?id=%s&is_sale=0";


    String FEE_DATE_URL = "http://m.caissa.com.cn/group/index/calendars?id=%s&is_sale=0";

    Queue<String> DB_ID_LIST = new LinkedList<>();

    String CAISSA_WEB_LIST = "http://search.caissa.com.cn/?type=group&k=亚洲&co=10100&pn=%d&ps=20&gfcn=(1192,116,98,6)";

    String CAISSA_WEB_DETAIL = "http://group.caissa.com.cn/detail/%s";

    //自营产品价格日历链接
    String CAISSA_WEB_SELF_CALENDARS = "http://group.caissa.com.cn/detailInfo/ajaxCalendar/?v=%s&product_db_id=%s&schedule_days=%s&schedule_nights=%s&departure=%s&salechannel=";

    //联合发团产品价格日历链接
    String CAISSA_WEB_CALENDARS = "http://group.caissa.com.cn/CollectInfo/ajaxCalendar/?v=%s&product_db_id=%s&departure=%s&salechannel=";

    Queue<SubDataDto> CAISSA_WEB_DETAIL_URL_LIST = new LinkedList<>();

    String SUPPLIER_FILE = "supplier_caissa.properties";

    String SPIDER_START = "1";

    String DEFAULT_PICTURE = "http://m.caissa.com.cn//static/img/h5/detail_smallpicnone.jpg";

    String LIST_PICTURE_LINK = "http://img8.caissa.com.cn/cloud/image/%s_260_200";

    String DETAIL_PICTURE_LINK = "http://img8.caissa.com.cn/cloud/image/%s_525_330";



    //redis key

    //要访问的列表url
    String LIST_PAGE_TO_VISIT = "list-page-to-visit";

    //要访问的日历url
    String CALENDAR_PAGE_TO_VISIT = "calendar-page-to-visit";

    //要访问的日历url
    String DETAIL_PAGE_TO_VISIT = "detail-page-to-visit";

    List<String> CROWD_LIST_QZ = Lists.newArrayList("亲子");



}
