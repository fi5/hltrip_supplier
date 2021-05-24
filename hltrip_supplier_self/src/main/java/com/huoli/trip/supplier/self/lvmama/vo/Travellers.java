package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/5/2416:30
 */
@Data
public class Travellers implements Serializable {
    List<Traveller> traveller;
}
