package com.studypals.testModules.testComponent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트 시 사용할 controller 메서드 집합
 *
 * @author jack8
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/base")
    public void throwBaseException() {
        throw new TestException();
    }

    @GetMapping("/unexpected")
    public void throwUnexpectedException() {
        throw new RuntimeException("예상 못한 예외");
    }
}
