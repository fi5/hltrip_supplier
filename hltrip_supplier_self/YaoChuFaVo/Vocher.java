package com.huoli.trip.common.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Vocher implements Serializable {
    private String vocherNo;
    private String vocherUrl;
    private String vocherExtendInfos;
}
