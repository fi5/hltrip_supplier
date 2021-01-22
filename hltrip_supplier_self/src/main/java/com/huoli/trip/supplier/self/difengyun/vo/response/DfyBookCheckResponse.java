package com.huoli.trip.supplier.self.difengyun.vo.response;

import com.huoli.trip.supplier.self.difengyun.vo.DfyBookSaleInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1514:47
 */
@Data
public class DfyBookCheckResponse implements Serializable {
    private static final long serialVersionUID = 7184852040493514204L;
    //产品编号
    private String productId;
    //价格库存列表
    private List<DfyBookSaleInfo> saleInfos;

}
