package com.studypals.testModules.testUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studypals.global.exceptions.errorCode.ErrorCode;

/**
 * ResultMatcher에 대한 구현 클래스. discussion 및 노션 참조 필요
 *
 * @author jack8
 * @since 2025-04-01
 */
public class JsonFieldResultMatcher implements ResultMatcher {
    private final List<ResultMatcher> matchers;

    public JsonFieldResultMatcher(List<ResultMatcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public void match(MvcResult result) throws Exception {
        for (ResultMatcher matcher : matchers) {
            matcher.match(result);
        }
    }

    public static ResultMatcher hasStatus(ErrorCode errorCode) {
        return result -> {
            HttpStatus expectedStatus = errorCode.getHttpStatus();
            int actualStatus = result.getResponse().getStatus();
            Assertions.assertEquals(
                    expectedStatus.value(),
                    actualStatus,
                    "Expected status code " + expectedStatus.value() + " but got " + actualStatus);
        };
    }

    public static ResultMatcher hasKey(String key, String value) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(value));
        return new JsonFieldResultMatcher(matchers);
    }

    public static ResultMatcher hasKey(String outputString) {
        return result ->
                Assertions.assertEquals(outputString, result.getResponse().getContentAsString());
    }

    public static ResultMatcher hasKey(ErrorCode errorCode) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$.code").value(errorCode.getCode()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value("fail"));
        matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(errorCode.getMessage()));
        return new JsonFieldResultMatcher(matchers);
    }

    public static ResultMatcher hasKey(Object dto) {
        List<ResultMatcher> matchers = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = mapper.convertValue(dto, Map.class);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;
            if (value instanceof String
                    || value instanceof Integer
                    || value instanceof Long
                    || value instanceof Boolean) {
                matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(value));
            } else if (value instanceof LocalDate) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                String formattedDate = ((LocalDate) value).format(formatter);
                matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(formattedDate));
            } else if (value instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String formattedDateTime =
                        ((LocalDateTime) value).truncatedTo(ChronoUnit.SECONDS).format(formatter);
                matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(formattedDateTime));
            } else {
                matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(value));
            }
        }

        return new JsonFieldResultMatcher(matchers);
    }

    public static ResultMatcher hasKey(List<?> dtos) {
        ObjectMapper mapper = new ObjectMapper();
        List<ResultMatcher> matchers = new ArrayList<>();

        // 리스트의 크기를 검증합니다.
        matchers.add(MockMvcResultMatchers.jsonPath("$.length()").value(dtos.size()));

        for (int i = 0; i < dtos.size(); i++) {
            Object dto = dtos.get(i);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.convertValue(dto, Map.class);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                if (value != null) {
                    if (value instanceof LocalDate) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                        String formattedDate = ((LocalDate) value).format(formatter);
                        matchers.add(MockMvcResultMatchers.jsonPath("$[" + i + "]." + entry.getKey())
                                .value(formattedDate));
                    } else if (value instanceof LocalDateTime) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        String formattedDateTime = ((LocalDateTime) value)
                                .truncatedTo(ChronoUnit.SECONDS)
                                .format(formatter);
                        matchers.add(MockMvcResultMatchers.jsonPath("$[" + i + "]." + entry.getKey())
                                .value(formattedDateTime));
                    } else {
                        matchers.add(MockMvcResultMatchers.jsonPath("$[" + i + "]." + entry.getKey())
                                .value(value));
                    }
                }
            }
        }

        return new JsonFieldResultMatcher(matchers);
    }

    public static ResultMatcher hasKey(Page<?> page) {
        ObjectMapper mapper = new ObjectMapper();

        List<ResultMatcher> matchers = new ArrayList<>();

        matchers.add(MockMvcResultMatchers.jsonPath("$.totalElements").value(page.getTotalElements()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.totalPages").value(page.getTotalPages()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.size").value(page.getSize()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.number").value(page.getNumber()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(page.getNumberOfElements()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.first").value(page.isFirst()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.last").value(page.isLast()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.empty").value(page.isEmpty()));

        List<?> content = page.getContent();
        matchers.add(MockMvcResultMatchers.jsonPath("$.content.length()").value(content.size()));

        for (int i = 0; i < content.size(); i++) {
            Object dto = content.get(i);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.convertValue(dto, Map.class);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                if (value != null) {
                    if (value instanceof LocalDate) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                        String formattedDate = ((LocalDate) value).format(formatter);
                        matchers.add(MockMvcResultMatchers.jsonPath("$.content[" + i + "]." + entry.getKey())
                                .value(formattedDate));
                    } else if (value instanceof LocalDateTime) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        String formattedDateTime = ((LocalDateTime) value)
                                .truncatedTo(ChronoUnit.SECONDS)
                                .format(formatter);
                        matchers.add(MockMvcResultMatchers.jsonPath("$.content[" + i + "]." + entry.getKey())
                                .value(formattedDateTime));
                    } else {
                        matchers.add(MockMvcResultMatchers.jsonPath("$.content[" + i + "]." + entry.getKey())
                                .value(value));
                    }
                }
            }
        }

        return new JsonFieldResultMatcher(matchers);
    }
}
