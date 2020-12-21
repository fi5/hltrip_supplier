package com.huoli.trip.supplier.self.difengyun.vo;

import com.huoli.trip.supplier.self.hllx.vo.HllxStockItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1514:45
 */
@Data
public class DfyBookSaleInfo implements Serializable {
    private static final long serialVersionUID = -7946636574802354001L;
    //价格与库存时间
    private Date date;
    //价格（财务结算单价，两位小数，30.00（单位：元））
    private BigDecimal price;
    //价格类型（1：底价模式）
    private int priceType;
    //总库存
    private int totalStock;
    //库存明细列表
    private List<HllxStockItem> stockList;

    private BigDecimal salePrice;
}
