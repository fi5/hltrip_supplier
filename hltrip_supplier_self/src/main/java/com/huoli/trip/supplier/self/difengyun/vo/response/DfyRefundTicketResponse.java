package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1016:11
 */
@Data
public class DfyRefundTicketResponse implements Serializable {
    private static final long serialVersionUID = -8359701143979872757L;
    private String msg;
    /**
     * 1标识为警告，0无含义，默认为0
     */
    private String resultType;
}
