package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 예외로 변경
 * MemberRepository 인터페이서 사용
 * throws SQLException 제거
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource;
    private final SQLExceptionTranslator exceptionTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO MEMBER(member_id, money) VALUES (?, ?)";

        Connection con = null;
        // PreparedStatement는 Statement의 상속을 받는데, SQL Injection 공격을 피하기 위해서는 PreparedStatement를 사용해 SQL 바인딩(?로 값넣기)으로 해줘야함
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId()); // sql에 ?를 한 파라미터
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw exceptionTranslator.translate("save", sql, e);
        } finally {
            // TCP/IP 커넥션으로 연결되기 때문에 자원 해제해줘야 함
            close(con, pstmt, null);
        }

    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from MEMBER where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            // Result Set 무조건 next 한 번 해줘야함
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else{
                throw new NoSuchElementException("member not found memberID = " + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw exceptionTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int i = pstmt.executeUpdate();
            log.info("resultSize = {}", i);
        } catch (SQLException e) {
            throw exceptionTranslator.translate("update", sql, e);
        } finally {
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "DELETE FROM MEMBER WHERE member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exceptionTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);

        }

    }
    private void close(Connection con, Statement stmt, ResultSet rs) {
        // connection pool에서 가져올 경우 close가 아니라 커넥션을 반환해줌
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        DataSourceUtils.releaseConnection(con, dataSource);
//        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야함
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}",con, con.getClass());
        return con;
    }
}
