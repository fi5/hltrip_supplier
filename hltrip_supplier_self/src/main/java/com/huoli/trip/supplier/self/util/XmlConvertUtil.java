package com.huoli.trip.supplier.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/5/28<br>
 */
public class XmlConvertUtil {

    /**
     * xml转对象
     *
     * @param xml
     * @return
     * @throws JAXBException
     */
    public static <T> T convertToJava(String xml, Class<T> T) throws JAXBException {
        StringReader reader = new StringReader(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(T);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        T graphModel = (T) jaxbUnmarshaller.unmarshal(reader);
        return graphModel;
    }

    /**
     * 将对象转为XML
     *
     * @param T
     * @return
     * @throws JAXBException
     */
    public static <T> String convertToXML(Class<T> T) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(T);
        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(T, writer);
        String xmlStr = writer.toString();
        xmlStr = StringUtils.replace(xmlStr, "&quot;", "'");
        return xmlStr;
    }
}
