package vo.basevo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br>业务实体返回基类
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private String serverDateTime;
    //请求处理后的返回数据
    private T data;
    //合作人Id（由【要】分配）
    private String partnerId;
    //是否处理成功
    private Boolean success;
    private String message;
    //请求业务处理状态
    private int statusCode;
    //数据总条数（分页情况会有返回）
    private int total;
}
