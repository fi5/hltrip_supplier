package com.huoli.trip.supplier.web.aop;


import org.springframework.util.CollectionUtils;

import javax.validation.*;
import java.util.Objects;
import java.util.Set;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/7/21<br>
 */
public class ValidatorUtil {

    private ValidatorUtil() {
    }

    private static Validator validator;

    public static <T> void validate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = getValidator().validate(obj);
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return;
        }
        StringBuffer paramMsg = new StringBuffer();
        constraintViolations.forEach(violation -> paramMsg.append(violation.getMessage()).append("；"));
        throw new ValidationException(paramMsg.toString());
    }

    private static Validator getValidator() {
        if (Objects.isNull(validator)) {
            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            validator = validatorFactory.getValidator();
        }
        return validator;
    }

}
