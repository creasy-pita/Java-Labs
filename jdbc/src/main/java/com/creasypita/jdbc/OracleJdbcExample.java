package com.creasypita.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OracleJdbcExample {

    // 数据库连接信息 - 请根据实际情况修改
    private static final String URL = "jdbc:oracle:thin:@192.168.11.163:1521:orcl";
    private static final String USER = "gisqbpm";
    private static final String PASSWORD = "gisqbpm";

    public static void main(String[] args) {
        Connection conn = null;

        try {
            // 1. 加载Oracle驱动
            // Oracle ojdbc14，要手动加载驱动：这是较旧的驱动版本（对应Oracle 10g），没有实现自动注册机制。
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Oracle驱动加载成功");

            // 2. 建立数据库连接
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("数据库连接成功");

            // 3. 创建测试表
            createTestTable(conn);

            // 4. 检查并创建序列（如果不存在）
            createSequenceIfNotExists(conn, "test_users_seq");

            // 4. 使用普通SQL插入数据（有SQL注入风险）
            insertWithPlainSQL(conn);

            // 5. 使用PreparedStatement和占位符插入数据（安全）
            insertWithPreparedStatement(conn);

            // 6. 查询数据
            queryData(conn);

            // 7. 使用PreparedStatement更新数据
            updateWithPreparedStatement(conn);

            // 8. 再次查询数据
            queryData(conn);

            // 9. 使用PreparedStatement删除数据
            deleteWithPreparedStatement(conn);

            // 10. 最终查询数据
            queryData(conn);

            // 11. 使用PreparedStatement进行条件查询
            conditionalQueryWithPreparedStatement(conn);

        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC驱动未找到");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("程序执行出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("数据库连接已关闭");
                } catch (Exception e) {
                    System.out.println("关闭连接时出错: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 创建测试表
     */
    private static void createTestTable(Connection conn) throws Exception {
        // 先尝试删除表（如果存在）
        String dropTableSQL = "BEGIN " +
                "   EXECUTE IMMEDIATE 'DROP TABLE test_users'; " +
                "EXCEPTION " +
                "   WHEN OTHERS THEN " +
                "      IF SQLCODE != -942 THEN " +
                "         RAISE; " +
                "      END IF; " +
                "END;";

        String createTableSQL = "CREATE TABLE test_users (" +
                "id NUMBER PRIMARY KEY, " +
                "name VARCHAR2(50) NOT NULL, " +
                "email VARCHAR2(100) NOT NULL, " +
                "age NUMBER(3), " +
                "create_date DATE DEFAULT SYSDATE" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            // 执行删除表
            stmt.execute(dropTableSQL);
            System.out.println("清理旧表完成");

            // 创建新表
            stmt.execute(createTableSQL);
            System.out.println("测试表创建成功");
        }
    }

    /**
     * 检查序列是否存在，如果不存在则创建
     */
    private static void createSequenceIfNotExists(Connection conn, String sequenceName) throws Exception {
        // 检查序列是否已存在
        if (!isSequenceExists(conn, sequenceName)) {
            createSequence(conn, sequenceName);
        } else {
            System.out.println("序列 " + sequenceName + " 已存在，跳过创建");
        }
    }

    /**
     * 检查序列是否存在
     */
    private static boolean isSequenceExists(Connection conn, String sequenceName) throws Exception {
        String checkSequenceSQL = "SELECT COUNT(*) FROM user_sequences WHERE sequence_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(checkSequenceSQL)) {
            pstmt.setString(1, sequenceName.toUpperCase()); // Oracle数据字典通常是大写

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 创建序列
     */
    private static void createSequence(Connection conn, String sequenceName) throws Exception {
        String createSequenceSQL = "CREATE SEQUENCE " + sequenceName + " " +
                "START WITH 1 " +
                "INCREMENT BY 1 " +
                "NOMAXVALUE " +
                "NOCACHE " +
                "NOCYCLE";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSequenceSQL);
            System.out.println("序列 " + sequenceName + " 创建成功");
        }
    }

    /**
     * 使用普通SQL插入数据（有SQL注入风险）
     */
    private static void insertWithPlainSQL(Connection conn) throws Exception {
        String name = "张三";
        String email = "zhangsan@example.com";
        int age = 25;

        // 注意：这种方式有SQL注入风险，仅用于演示
        String sql = "INSERT INTO test_users (id, name, email, age) VALUES " +
                "(test_users_seq.NEXTVAL, '" + name + "', '" + email + "', " + age + ")";

        try (Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("普通SQL插入数据成功，影响行数: " + rows);
        }
    }

    /**
     * 使用PreparedStatement和占位符插入数据（安全）
     */
    private static void insertWithPreparedStatement(Connection conn) throws Exception {
        String sql = "INSERT INTO test_users (id, name, email, age) VALUES " +
                "(test_users_seq.NEXTVAL, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 插入第一条数据
            pstmt.setString(1, "李四");
            pstmt.setString(2, "lisi@example.com");
            pstmt.setInt(3, 30);
            int rows1 = pstmt.executeUpdate();
            System.out.println("PreparedStatement插入数据1成功，影响行数: " + rows1);

            // 插入第二条数据 - 重用同一个PreparedStatement
            pstmt.setString(1, "王五");
            pstmt.setString(2, "wangwu@example.com");
            pstmt.setInt(3, 28);
            int rows2 = pstmt.executeUpdate();
            System.out.println("PreparedStatement插入数据2成功，影响行数: " + rows2);
        }
    }

    /**
     * 查询所有数据
     */
    private static void queryData(Connection conn) throws Exception {
        String sql = "SELECT id, name, email, age, create_date FROM test_users ORDER BY id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== 查询结果 ===");
            System.out.println("ID\t姓名\t邮箱\t\t年龄\t创建时间");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                int age = rs.getInt("age");
                java.sql.Date createDate = rs.getDate("create_date");

                System.out.println(id + "\t" + name + "\t" + email + "\t" + age + "\t" + createDate);
            }
            System.out.println("============================================================");
        }
    }

    /**
     * 使用PreparedStatement更新数据
     */
    private static void updateWithPreparedStatement(Connection conn) throws Exception {
        String sql = "UPDATE test_users SET age = ? WHERE name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 35);  // 设置新的年龄
            pstmt.setString(2, "李四");  // 设置要更新的用户名

            int rows = pstmt.executeUpdate();
            System.out.println("PreparedStatement更新数据成功，影响行数: " + rows);
        }
    }

    /**
     * 使用PreparedStatement删除数据
     */
    private static void deleteWithPreparedStatement(Connection conn) throws Exception {
        String sql = "DELETE FROM test_users WHERE name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "王五");  // 设置要删除的用户名

            int rows = pstmt.executeUpdate();
            System.out.println("PreparedStatement删除数据成功，影响行数: " + rows);
        }
    }

    /**
     * 使用PreparedStatement进行条件查询
     */
    private static void conditionalQueryWithPreparedStatement(Connection conn) throws Exception {
        String sql = "SELECT id, name, email, age FROM test_users WHERE age > ? ORDER BY age DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 20);  // 设置查询条件：年龄大于20

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n=== 条件查询结果（年龄 > 20）===");
                System.out.println("ID\t姓名\t邮箱\t\t年龄");
                System.out.println("----------------------------------------");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    int age = rs.getInt("age");

                    System.out.println(id + "\t" + name + "\t" + email + "\t" + age);
                }
                System.out.println("========================================");
            }
        }
    }

    /**
     * 批量插入示例（使用PreparedStatement）
     */
    private static void batchInsertWithPreparedStatement(Connection conn) throws Exception {
        String sql = "INSERT INTO test_users (id, name, email, age) VALUES " +
                "(test_users_seq.NEXTVAL, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 添加批量操作
            pstmt.setString(1, "赵六");
            pstmt.setString(2, "zhaoliu@example.com");
            pstmt.setInt(3, 40);
            pstmt.addBatch();

            pstmt.setString(1, "孙七");
            pstmt.setString(2, "sunqi@example.com");
            pstmt.setInt(3, 22);
            pstmt.addBatch();

            pstmt.setString(1, "周八");
            pstmt.setString(2, "zhouba@example.com");
            pstmt.setInt(3, 33);
            pstmt.addBatch();

            // 执行批量操作
            int[] results = pstmt.executeBatch();
            System.out.println("批量插入完成，影响行数: " + java.util.Arrays.toString(results));
        }
    }
}
