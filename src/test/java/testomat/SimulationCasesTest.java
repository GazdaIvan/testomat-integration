package testomat;

import io.testomat.annotations.TestId;
import io.testomat.annotations.Title;
import io.testomat.extension.TestomatExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.TestAbortedException;

@ExtendWith(TestomatExtension.class)
public class SimulationCasesTest {

    @Test
    @TestId("T001")
    @Title("Check addition works")
    void testTwoPlusTwo() {
        assert(2 + 2 == 4);
    }

    @Test
    @TestId("T002")
    @Title("Failing test example")
    void testFailViaRuntimeException() {
        throw new RuntimeException("Intentional failure");
    }

    @Test
    void testSkippedViaTestAbortedException() {
        throw new TestAbortedException("Skipped for demo");
    }

    @Test
    @Disabled("Disabled for demo")
    @TestId("T003")
    @Title("Disabled test example")
    void testDisabledViaDisabledAnnotation() {
        // This test will be skipped by the @Disabled annotation
    }

    @Test
    @TestId("T004")
    @Title("Timeout test example")
    void testTimeoutViaThreadSleep() throws InterruptedException {
        // Force a hang to simulate a timeout
        Thread.sleep(3000);
    }

    @Test
    @TestId("T005")
    @Title("Test with expected exception")
    void testExpectedExceptionViaIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        });
    }
}
