package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
        void unchecked_throw() {
            Service service = new Service();
            Assertions.assertThatThrownBy(() -> service.callThrow()).isInstanceOf(MyUncheckedException.class);
        }

    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class Repository {
        public void call() /* 언체크예외는 throws 생략가능*/{
            throw new MyUncheckedException("ex");
        }
    }

    /**
     * UnChecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 자빚 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 됨
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않아도, 자연스럽게 던져진다.
         * 체크 예외와 다르게 throws를 하지 않아도 된다.
         */
        public void callThrow() {
            repository.call();
        }
    }


}
