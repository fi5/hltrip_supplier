package com.huoli.trip.supplier.self.difengyun.constant;

import com.huoli.trip.common.constant.Certificate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lunatic
 * @Title: DfyCertificateType
 * @Package
 * @Description: 笛风云证件类型
 * @date 2020/12/2114:50
 */
public enum DfyCertificateType {
    ID_CARD(1,"身份证"),
    PASSPORT(2,"护照"),
    HKM_PASS(4,"港澳通行证"),
    //TW_PASS(3,"台湾通行证"),
    //HOME_CARD(4,"回乡证"),
    //TW_CARD(5,"台胞证"),
    SOLDIERS(7,"台胞证"),
    OFFICER(3,"军官证"),
    OTHER(99,"其他");

    @Getter
    private int code;
    @Getter
    private String desc;

    DfyCertificateType() {
    }

    DfyCertificateType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getCertificateDescByCode(int code){
        DfyCertificateType[] certificates = DfyCertificateType.values();
        for(DfyCertificateType typeConstant : certificates){
            if(typeConstant.getCode() == code){
                return typeConstant.getDesc();
            }
        }
        return null;
    }

    public static DfyCertificateType getCertificateByCode(int code){
        DfyCertificateType[] certificates = DfyCertificateType.values();
        for(DfyCertificateType typeConstant : certificates){
            if(typeConstant.getCode() == code){
                return typeConstant;
            }
        }
        return null;
    }

    public static List<DfyCertificateType> getCertificateByCodeS(List<Integer> codes) {
        List<DfyCertificateType> certificateList = null;
        DfyCertificateType[] certificates = DfyCertificateType.values();
        for (Integer s : codes) {
            for (DfyCertificateType typeConstant : certificates) {
                if (typeConstant.getCode() == s) {
                    if (certificateList == null) {
                        certificateList = new ArrayList<>();
                    }
                    certificateList.add(typeConstant);
                }
            }
        }
        return certificateList;
    }

}
