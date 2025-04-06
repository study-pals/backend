package com.studypals.global.exceptions.exceptionHandler;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.studypals.testModules.testComponent.TestController;
import com.studypals.testModules.testComponent.TestErrorCode;

/**
 * {@link GlobalExceptionHandler} 에 대한 테스트. 일부 테스트 유틸리티를 사용하여진행하였다.
 *
 * @author jack8
 * @see GlobalExceptionHandler
 * @since 2025-04-01
 */
@WebMvcTest(TestController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "debug.message.print=false")
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("custom exception이 발생하여 CommonResponse로 매핑되어 응답")
    void handleBaseException() throws Exception {
        mockMvc.perform(get("/test/base"))
                .andExpect(hasStatus(TestErrorCode.TEST_ERROR_CODE))
                .andExpect(hasKey(TestErrorCode.TEST_ERROR_CODE));
    }

    @Test
    @DisplayName("예상치 못한 예외가 발생")
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(hasKey("code", "EIS-02"))
                .andExpect(hasKey("message", "unknown internal server error"));
    }
}
