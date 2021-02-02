package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/2/110:18
 */
@Data
public class ReceiveVisaCompanyInfo implements Serializable {
    /**
     * 	公司编码
     */
    private Integer id;
    /**
     * 公司名称
     */
    private String name;
}
