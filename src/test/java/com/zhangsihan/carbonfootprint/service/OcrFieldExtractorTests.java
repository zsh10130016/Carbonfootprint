package com.zhangsihan.carbonfootprint.service;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OcrFieldExtractorTests {

    private final OcrFieldExtractor extractor = new OcrFieldExtractor();

    @Test
    void shouldExtractTrainTicketDistance() {
        Map<String, String> fields = extractor.extractFields("TRANSPORT_TICKET", List.of(
                "高铁票 G7331",
                "杭州东 到 上海虹桥",
                "行程 168.5 km"
        ));

        assertThat(fields).containsEntry("activityType", "TRANSPORT")
                .containsEntry("subType", "TRAIN")
                .containsEntry("amount", "168.5")
                .containsEntry("unit", "km");
    }

    @Test
    void shouldExtractElectricityBillAmount() {
        Map<String, String> fields = extractor.extractFields("UTILITY_BILL", List.of(
                "国网电费账单",
                "本期用电量 86.20 度",
                "账单金额 48.12 元"
        ));

        assertThat(fields).containsEntry("activityType", "HOME_ENERGY")
                .containsEntry("subType", "ELECTRICITY")
                .containsEntry("amount", "86.2")
                .containsEntry("unit", "kWh");
    }

    @Test
    void shouldExtractWaterBillAmount() {
        Map<String, String> fields = extractor.extractFields("UTILITY_BILL", List.of(
                "水费通知单",
                "本期用水量 12 吨"
        ));

        assertThat(fields).containsEntry("activityType", "HOME_ENERGY")
                .containsEntry("subType", "WATER")
                .containsEntry("amount", "12")
                .containsEntry("unit", "ton");
    }
}
