package com.studypals.testModules.testUtils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studypals.global.exceptions.errorCode.ErrorCode;
import com.studypals.global.responses.Response;

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
        for (ResultMatcher macher : matchers) {
            macher.match(result);
        }
    }

    /**
     * HTTP Status 검증 메서드
     *
     * @param errorCode 예상하는 ErrorCode
     * @return ResultMatcher
     */
    public static ResultMatcher hasStatus(ErrorCode errorCode) {
        return result -> {
            HttpStatus expectedStatus = errorCode.getHttpStatus();
            int actualStatus = result.getResponse().getStatus();
            assertThat(expectedStatus.value())
                    .withFailMessage("기대한 Http status 는 %d 였지만, 실제는 %d 였습니다.", expectedStatus.value(), actualStatus)
                    .isEqualTo(actualStatus);
        };
    }

    public static ResultMatcher hasKey(String key, String value) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(value));
        return new JsonFieldResultMatcher(matchers);
    }

    public static ResultMatcher hasKey(ErrorCode errorCode) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$.code").value(errorCode.getCode()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value("fail"));
        matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(errorCode.getMessage()));
        return new JsonFieldResultMatcher(matchers);
    }

    public static <T> ResultMatcher hasKey(Response<T> response) {
        List<ResultMatcher> matchers = new ArrayList<>();

        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value(response.getStatus()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.code").value(response.getCode()));
        if (response.getMessage() != null) {
            matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(response.getMessage()));
        }

        T data = response.getData();

        if (data == null) {
            matchers.add(MockMvcResultMatchers.jsonPath("$.data").value(IsNull.nullValue()));
        } else {
            matchers.addAll(buildMatchersForValue("data", data));
        }
        return new JsonFieldResultMatcher(matchers);
    }

    public static <T> ResultMatcher hasKey(Page<T> page) {
        List<ResultMatcher> matchers = new ArrayList<>();

        // 1. 메타 필드 검증
        matchers.add(MockMvcResultMatchers.jsonPath("$.totalElements").value(page.getTotalElements()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.totalPages").value(page.getTotalPages()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.size").value(page.getSize()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.number").value(page.getNumber()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(page.getNumberOfElements()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.first").value(page.isFirst()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.last").value(page.isLast()));
        matchers.add(MockMvcResultMatchers.jsonPath("$.empty").value(page.isEmpty()));

        // 2. content 검증 (리스트 재귀 처리)
        List<T> content = page.getContent();
        matchers.addAll(matcherForList("content", content));

        return new JsonFieldResultMatcher(matchers);
    }

    private static List<ResultMatcher> matcherForList(String root, List<?> values) {
        List<ResultMatcher> matchers = new ArrayList<>();

        matchers.add(MockMvcResultMatchers.jsonPath("$." + root + ".length()").value(values.size()));

        // 리스트가 1개면 내부 값도 재귀 검사
        if (values.size() == 1) {
            Object value = values.get(0);
            String elementPath = String.format("%s[0]", root);
            matchers.addAll(buildMatchersForValue("$." + elementPath, value));
        }

        // size >= 2 면 길이만 검사하고 내부는 생략

        return matchers;
    }

    @SuppressWarnings("unchecked")
    private static List<ResultMatcher> buildMatchersForValue(String path, Object value) {
        List<ResultMatcher> matchers = new ArrayList<>();

        if (value == null) { // if you don't want null check, change this
            matchers.add(MockMvcResultMatchers.jsonPath(path).value((Object) null));
        } else if (isSimpleValue(value)) {
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(value));
        } else if (value instanceof LocalDate) {
            String formatted = ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(formatted));
        } else if (value instanceof LocalTime) {
            String formatted = ((LocalTime) value).format(DateTimeFormatter.ISO_LOCAL_TIME);
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(formatted));
        } else if (value instanceof LocalDateTime) {
            String formatted = ((LocalDateTime) value)
                    .truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(formatted));
        } else if (value instanceof List<?> list) {
            matchers.addAll(matcherForList(path, list));
        } else {
            ObjectMapper mapper = createMapper();
            Map<String, Object> nestedMap = mapper.convertValue(value, Map.class);
            for (Map.Entry<String, Object> entry : nestedMap.entrySet()) {
                String nestedPath = path + "." + entry.getKey();
                matchers.addAll(buildMatchersForValue(nestedPath, entry.getValue()));
            }
        }

        return matchers;
    }

    private static boolean isSimpleValue(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
