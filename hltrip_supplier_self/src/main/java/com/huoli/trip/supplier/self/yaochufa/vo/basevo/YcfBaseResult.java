package com.huoli.trip.supplier.self.yaochufa.vo.basevo;

import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import lombok.Data;

import java.io.Serializable;

@Data
public class YcfBaseResult<T> implements Serializable {
    /**
     * 是否处理成功
     * boolean
     * 必填
     * true:处理成功
     * false:处理失败
     */
    private Boolean success;

    /**
     * 请求处理状态
     * int
     * 必填
     * 200: 成功
     * 400: 请求错误
     * 500:服务错误
     */
    private String statusCode;

    /**
     * 错误信息
     * String
     * 非必填
     * success=false的错误原因
     */
    private String message;

    /**
     * 合作人Id（由【要】分配）
     * 必填
     */
    private String partnerId;
    /**
     * 请求处理后的返回数据，具体对应每个接口返回的具体结构
     */
    private T data;

    /**
     * 数据总条数
     * 用于分页，返回数据总条数2
     */
    private Long total;

    public YcfBaseResult(){

    }

    public YcfBaseResult(Boolean success, int code, T data, String partnerId){
        this.setStatusCode(String.valueOf(code));
        this.setSuccess(success);
        this.setPartnerId(partnerId);
        this.setData(data);
    }

    public static <T> YcfBaseResult<T> success(){
        return new YcfBaseResult<>(YcfConstants.RESULT_STATUS_SUCCESS, YcfConstants.RESULT_CODE_SUCCESS,
                null, ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConfigConstants.CONFIG_ITEM_PARTNER_ID));
    }

    public static <T> YcfBaseResult<T> success(T data){
        return new YcfBaseResult<>(YcfConstants.RESULT_STATUS_SUCCESS, YcfConstants.RESULT_CODE_SUCCESS,
                data, ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConfigConstants.CONFIG_ITEM_PARTNER_ID));
    }

    public static <T> YcfBaseResult<T> fail(T data){
        return new YcfBaseResult<>(YcfConstants.RESULT_STATUS_FAIL, YcfConstants.RESULT_CODE_SERVICE_ERROR,
                data, ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConfigConstants.CONFIG_ITEM_PARTNER_ID));
    }

    public static <T> YcfBaseResult<T> fail(){
        return new YcfBaseResult<>(YcfConstants.RESULT_STATUS_FAIL, YcfConstants.RESULT_CODE_SERVICE_ERROR,
                null, ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConfigConstants.CONFIG_ITEM_PARTNER_ID));
    }

}
