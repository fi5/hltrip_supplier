package vo.order;

import lombok.Data;

/**
 * 描述: <br> 库存单元业务实体
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class StockItem {
    //元素编号（房型编号，票种编号，餐饮编号）
    private String itemId;
    //库存量（可购买产品的份数）
    private int stock;
}
