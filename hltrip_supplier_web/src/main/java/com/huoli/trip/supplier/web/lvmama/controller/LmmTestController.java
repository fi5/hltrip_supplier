package com.huoli.trip.supplier.web.lvmama.controller;

import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import com.huoli.trip.supplier.web.lvmama.task.LmmTicketTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/5/17<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/lmm/test")
public class LmmTestController {

    @Autowired
    private LmmSyncService lmmSyncService;

    @Autowired
    private LmmTicketTask lmmTicketTask;

    @PostMapping(path = "/sync/product/add")
    public DfyBaseResult syncProductAdd() {
        lmmTicketTask.syncNewProduct();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/product/update")
    public DfyBaseResult syncProductUpdate() {
        lmmTicketTask.syncUpdateProduct();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/add")
    public DfyBaseResult syncScenicAdd() {
        lmmTicketTask.syncNewScenic();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/update")
    public DfyBaseResult syncScenicUpdate() {
        lmmTicketTask.syncUpdateScenic();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/update/id")
    public DfyBaseResult syncScenicUpdateById(@RequestBody String scenicId) {
        lmmSyncService.syncScenicListById(scenicId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/product/update/id")
    public DfyBaseResult syncProductUpdateById(@RequestBody String productId) {
        lmmSyncService.syncProductListById(productId, 0);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/goods/update/id")
    public DfyBaseResult syncGoodsUpdateById(@RequestBody String goodsId) {
        lmmSyncService.syncGoodsListById(goodsId, 0);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/product/update/v2")
    public DfyBaseResult syncProductUpdateV2() {
        lmmTicketTask.syncUpdateProductV2();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/update/v2")
    public DfyBaseResult syncScenicUpdateV2() {
        lmmTicketTask.syncUpdateScenicV2();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/goods/update/id/v2")
    public DfyBaseResult syncGoodsUpdateByIdV2(@RequestBody String goodsId) {
        lmmSyncService.syncGoodsListByIdV2(goodsId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/product/update/id/v2")
    public DfyBaseResult syncProductUpdateByIdV2(@RequestBody String productId) {
        lmmSyncService.syncProductListByIdV2(productId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/update/id/v2")
    public DfyBaseResult syncScenicUpdateByIdV2(@RequestBody String scenicId) {
        lmmSyncService.syncScenicListByIdV2(scenicId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/scenic/update/all/v2")
    public DfyBaseResult syncScenicUpdateByIdV2() {
        LmmScenicListRequest request = new LmmScenicListRequest();
        int i = 1;
        while (true) {
            request.setCurrentPage(i++);
            boolean b = lmmSyncService.syncScenicListV2(request);
            if(!b){
                break;
            }
        }
        return DfyBaseResult.success();
    }
}
