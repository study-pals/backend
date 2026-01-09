package com.studypals.global.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 *
 * @author jack8
 * @see
 * @since 2026-01-06
 */
class StringUtilsTest {
    private final StringUtils stringUtils = new StringUtils();

    @DisplayName("null/blank 입력은 null 반환")
    @ParameterizedTest
    @MethodSource("nullOrBlankCases")
    void normalize_null_or_blank_returns_null(String raw) {
        assertNull(stringUtils.normalize(raw));
    }

    static Stream<Arguments> nullOrBlankCases() {
        return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "), Arguments.of("\n\t  "));
    }

    @DisplayName("앞뒤 공백 trim + 최종 소문자 변환")
    @ParameterizedTest
    @MethodSource("trimAndLowerCases")
    void normalize_trim_and_lowercase(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> trimAndLowerCases() {
        return Stream.of(
                Arguments.of("  Hello  ", "hello"),
                Arguments.of("\nHeLLo\t", "hello"),
                Arguments.of("JAVA", "java"),
                Arguments.of("jAvA", "java"));
    }

    @DisplayName("공백은 '_'로 치환, 연속 공백은 '_' 1개로 압축")
    @ParameterizedTest
    @MethodSource("spacesToUnderscoreCases")
    void normalize_spaces_to_underscore(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> spacesToUnderscoreCases() {
        return Stream.of(
                Arguments.of("hello world", "hello_world"),
                Arguments.of("hello   world", "hello_world"),
                Arguments.of("hello\t\tworld", "hello_world"),
                Arguments.of("hello \n world", "hello_world"),
                Arguments.of("  hello   world  ", "hello_world"),
                Arguments.of("a    b    c", "a_b_c"));
    }

    @DisplayName("앞뒤 '_' 제거(앞뒤 공백이 '_'로 바뀐 경우 포함)")
    @ParameterizedTest
    @MethodSource("trimUnderscoreCases")
    void normalize_trim_underscores(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> trimUnderscoreCases() {
        return Stream.of(
                Arguments.of(" hello ", "hello"),
                Arguments.of("  hello   ", "hello"),
                Arguments.of("   hello   world   ", "hello_world"),
                // 공백 -> _ 된 뒤 앞뒤 _ 제거 확인
                Arguments.of("   a   ", "a"),
                Arguments.of("   a   b   ", "a_b"));
    }

    @DisplayName("해시(#) 제거")
    @ParameterizedTest
    @MethodSource("hashRemovalCases")
    void normalize_remove_hash(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> hashRemovalCases() {
        return Stream.of(
                Arguments.of("#Hello", "hello"),
                Arguments.of("##Hello", "hello"),
                Arguments.of("###Hello###", "hello"),
                Arguments.of("#hello #world", "hello_world"),
                Arguments.of("###  Hello   World  ###", "hello_world"));
    }

    @DisplayName("특수문자 제거(문자/숫자/공백/_ 만 남김) + 소문자")
    @ParameterizedTest
    @MethodSource("specialCharRemovalCases")
    void normalize_remove_special_chars(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> specialCharRemovalCases() {
        return Stream.of(
                Arguments.of("hello!@#$%^&*()", "hello"),
                Arguments.of("he-llo", "hello"),
                Arguments.of("he+llo", "hello"),
                Arguments.of("he.llo", "hello"),
                Arguments.of("he/llo", "hello"),
                Arguments.of("hello(world)", "helloworld"),
                Arguments.of("hello[]{}<>", "hello"),
                Arguments.of("hello:world", "helloworld"),
                Arguments.of("hello, world", "hello_world"), // 콤마 제거 후 공백은 _
                Arguments.of("a_b", "a_b"), // '_'는 유지
                Arguments.of("A_B", "a_b") // '_' 유지 + 소문자
                );
    }

    @DisplayName("숫자는 유지되고, 공백 치환/특수문자 제거 규칙이 함께 적용")
    @ParameterizedTest
    @MethodSource("numberCases")
    void normalize_numbers(String raw, String expected) {
        assertEquals(expected, stringUtils.normalize(raw));
    }

    static Stream<Arguments> numberCases() {
        return Stream.of(
                Arguments.of("ver 2", "ver_2"),
                Arguments.of("Ver 2.0", "ver_20"), // '.' 제거
                Arguments.of("v2.0.1", "v201"), // '.' 제거
                Arguments.of("  123  ", "123"),
                Arguments.of("a 1 b 2", "a_1_b_2"));
    }

    @DisplayName("연속 '_'는 하나로 압축")
    @Test
    void normalize_multi_underscore_to_single() {
        assertEquals("a_b", stringUtils.normalize("a___b"));
        assertEquals("a_b", stringUtils.normalize("a__  __b")); // 공백 -> _까지 포함해도 최종 압축
    }

    @DisplayName("특수문자만 있는 경우: 제거 후 blank면 null")
    @Test
    void normalize_only_specials_returns_null() {
        // "!!!" -> "" -> blank => null
        assertNull(stringUtils.normalize("!!!"));
        assertNull(stringUtils.normalize("###")); // '#' 제거 후 빈 문자열
        assertNull(stringUtils.normalize("   ###   ")); // trim -> ### -> 제거 -> blank
    }

    @DisplayName("결합 시나리오: # + 특수문자 + 다중 공백 + 대소문자")
    @Test
    void normalize_combined_case() {
        assertEquals("spring_boot_jpa", stringUtils.normalize("  ###Spring   Boot!!   JPA###  "));
    }
}
