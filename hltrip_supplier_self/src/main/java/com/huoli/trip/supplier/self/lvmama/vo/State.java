package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:20
 */
@Data
public class State implements Serializable {
    private String code;
    private String message;
    private String solution;
}
