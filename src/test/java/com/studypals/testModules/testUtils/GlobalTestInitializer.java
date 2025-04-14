package com.studypals.testModules.testUtils;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * 테스트 실행 전 최초 1회 실행
 *
 * @author jack8
 * @since 2025-04-14
 */
public class GlobalTestInitializer implements TestExecutionListener {

    private static boolean alreadyInitialized = false;

    @Override
    public void beforeTestClass(TestContext testContext) {
        if (!alreadyInitialized) {
            System.out.println("clean up all Data base");

            CleanUp cleanUp = testContext.getApplicationContext().getBean(CleanUp.class);
            cleanUp.all();

            alreadyInitialized = true;
        }
    }
}
