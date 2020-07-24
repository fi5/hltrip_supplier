package com.huoli.trip.supplier.web.config;

import brave.Span;
import brave.Tracer;
import com.huoli.eagle.BraveTrace;
import com.huoli.eagle.eye.core.HuoliAtrace;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.eagle.report.SleuthSpanESReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import zipkin2.internal.HexCodec;


@Configuration
@PropertySource("classpath:atrace.properties")
public class TraceConfig {

    @Value("${dptCode}")
    private String dptCode;
    @Value("${bizCode}")
    private String bizCode;
    @Value("${appName}")
    private String appName;

    @Bean
    public HuoliAtrace huoliAtrace(HuoliTrace huoliTrace) {
        HuoliAtrace huoliAtrace = new HuoliAtrace.Builder()
                .withDptCode(dptCode)
                .withBizCode(bizCode)
                .withAppname(appName)
                .withHuoliTrace(huoliTrace) // optional
                .build();
        return huoliAtrace;
    }

    @Bean
    public BraveTrace huoliTrace(Tracer tracer) {
        return new BraveTrace(tracer);
    }
    @Bean
    public SleuthSpanESReporter sleuthSpanESReporter() {
        return new SleuthSpanESReporter();
    }

    @SuppressWarnings("unchecked")
    public static Object createSpan(String name, BraveTrace huoliTrace, String traceId) {
        Object newSpan;
        Span currentSpan = huoliTrace.currentSpan();
        if (currentSpan != null) {
            newSpan = huoliTrace.createSpan(name, currentSpan);
        } else {
            newSpan = huoliTrace.createSpan(name, brave.internal.HexCodec.lowerHexToUnsignedLong(traceId), brave.internal.HexCodec.lowerHexToUnsignedLong(traceId));
        }
        return newSpan;
    }

    public static void createNewSpan(String traceId, Span parent) {
        parent.context().toBuilder().traceId(HexCodec.lowerHexToUnsignedLong(traceId)).build();
    }
}
