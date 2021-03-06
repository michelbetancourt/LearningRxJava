package util;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.common.base.Stopwatch;

public abstract class AbstractTest {

    @Rule
    public TestNameWatcher testWatcher = new TestNameWatcher();

    protected Stopwatch watch;
    protected long speedInMillis;
    protected int repeatingCount;
    protected List<String> strings;
    protected List<List<String>> listofStrings;
    
    @Before
    public void before() {
        speedInMillis = 0;
        repeatingCount = 0;
        watch = Stopwatch.createStarted();
    }
    
    @After
    public void after() {
        System.out.println(">>Duration [" + testName() + "] " + watch);
    }
    
    protected String testName() {
        return testWatcher.testName;
    }
    
    protected void showDeferThreadName() {
        System.out.println(">" + testName() + " (defer)-> " + Thread.currentThread());
    }
    
    protected void showCallableThreadName() {
        System.out.println(">" + testName() + " (callable)-> " + Thread.currentThread());
    }
    
    protected void showObserverThreadName() {
        System.out.println(">" + testName() + " (observer)-> " + Thread.currentThread());
    }
    
    protected UUID callSlowService() {
        return callService(speedInMillis);
    }
    
    protected UUID callService(long milliSpeed) {
        try {
            Thread.sleep(milliSpeed);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        
        return UUID.randomUUID();
    }
    
    protected void throwTestException(String message) {
        throw new MockTestException(message);
    }
    
    
    private static class TestNameWatcher extends TestWatcher {
        private String testName;
        
        protected void starting(Description description) {
            testName = description.getMethodName();
        }
    }
    
    public static class MockTestException extends RuntimeException {

        private static final long serialVersionUID = 2654650727761433819L;

        public MockTestException() {
            super();
        }

        public MockTestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public MockTestException(String message, Throwable cause) {
            super(message, cause);
        }

        public MockTestException(String message) {
            super(message);
        }

        public MockTestException(Throwable cause) {
            super(cause);
        }
        
    }
}
