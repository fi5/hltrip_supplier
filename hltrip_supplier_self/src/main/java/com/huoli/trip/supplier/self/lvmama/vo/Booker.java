package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:17
 */
@Data
public class Booker implements Serializable {
    /**
     * 联系人姓名
     */
    private String name;
    /**
     * 联系人电话
     */
    private String mobile;
    private String email;
}
