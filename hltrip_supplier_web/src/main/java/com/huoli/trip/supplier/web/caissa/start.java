package com.huoli.trip.supplier.web.caissa;

import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.supplier.web.caissa.constant.Constant;
import com.huoli.trip.supplier.web.caissa.service.ParseService;
import com.huoli.trip.supplier.web.caissa.util.RedisQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Objects;

import static java.lang.String.format;

//@Component
public class start implements ApplicationRunner {

    private ParseService parseService;
    private RedisQueue redisQueue;

    @Autowired
    public start(ParseService parseService, RedisQueue redisQueue) {
        this.parseService = parseService;
        this.redisQueue = redisQueue;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //把种子url加入到要访问的列表页队列中
        int page = 1;
        String listPageToVisitKey = Constant.LIST_PAGE_TO_VISIT;
        RedisQueue.addForSet(listPageToVisitKey, format(Constant.CAISSA_APP_LIST, page));
        //循环列表页
        while (true) {
            String spider = ConfigGetter.getByFileItemString(Constant.SUPPLIER_FILE, "caissa.spider");
            if (!Objects.equals(spider, Constant.SPIDER_START)) {
                break;
            }
            //解析列表页
            parseService.getList(RedisQueue.popForSet(listPageToVisitKey));


            //解析详情页

            //解析价格日历

            //将下一页url加入到要访问的列表页队列中
            RedisQueue.addForSet(listPageToVisitKey, format(Constant.CAISSA_APP_LIST, page++));
        }

    }

    public static void main(String[] args) {
        String format = format(Constant.CAISSA_APP_LIST, 1);
        System.out.println(format);

    }
}
