package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    // jdbc 표준 인터페이스가 제공하는 커넥션
    public static Connection getConnection() {
        try {
            // 라이브러리에 있는 DB 드라이버를 찾아서 커넥션을 가져옴
            // DriverManager가 getConncetion으로 라이브러리에 있는 모든 드라이버에 던져봄.
            // url 패턴에 맞는 드라이버의 커넥션을 가져옴
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
