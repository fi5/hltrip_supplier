package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.State;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:36
 */
@Data
public class BaseResponse implements Serializable {
    private State state;
}
