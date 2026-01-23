package com.studypals.global.utils;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-12-23
 */
@Component
public class StringUtils {

    private static final Pattern DISALLOWED = Pattern.compile("[^a-zA-Z0-9_\\s]");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern MULTI_UNDERSCORE = Pattern.compile("_+");

    /**
     * 주어진 문자열을 정규화합니다.
     * <p>
     * 동작 순서는 다음과 같습니다.
     * <ol>
     *     <li>{@code raw}가 {@code null}이면 {@code null}을 반환합니다.</li>
     *     <li>앞뒤 공백을 제거합니다.</li>
     *     <li>공백 제거 후 비어 있으면 {@code null}을 반환합니다.</li>
     *     <li>{@code #} 문자를 모두 제거합니다.</li>
     *     <li>영문자, 숫자, 공백, 밑줄({@code _})을 제외한 모든 문자를 제거합니다.</li>
     *     <li>모든 공백 문자를 밑줄({@code _})로 변환합니다.</li>
     *     <li>연속된 밑줄을 하나의 밑줄로 축소합니다.</li>
     *     <li>앞뒤의 밑줄을 제거합니다.</li>
     * </ol>
     *
     * @param raw 정규화할 원본 문자열. {@code null}일 수 있습니다.
     * @return 정규화된 문자열. 입력이 {@code null}이거나, 정규화 결과가 비어 있거나
     *         공백뿐인 경우에는 {@code null}을 반환합니다.
     */
    public String normalize(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        if (s.isEmpty()) return null;

        s = s.replace("#", "");

        // 특수문자 제거(문자/숫자/공백/_ 만 남김)
        s = DISALLOWED.matcher(s).replaceAll("");

        // 공백 -> _
        s = SPACES.matcher(s).replaceAll("_");

        // 연속 _ -> 하나로
        s = MULTI_UNDERSCORE.matcher(s).replaceAll("_");

        // 앞뒤 _ 제거(앞뒤 공백 제거의 효과)
        s = s.replaceAll("^_+|_+$", "");

        s = s.toLowerCase();
        if (s.isBlank()) return null;

        return s;
    }
}
