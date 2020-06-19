package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class YcfVocher implements Serializable {
    //电子凭证码
    private String vocherNo;
    //二维码地址
    private String vocherUrl;
    //凭证其他信息(1-手机尾号四位）
    private Map<Integer,String> vocherExtendInfos;
}
