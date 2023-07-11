package com.creasypita.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Created by lujq on 6/30/2022.
 */
public class DruidDataSourceTest {

//    static final String DB_URL = "jdbc:postgresql://192.168.100.66:5432/zwzt?currentSchema=apolloconfigdbold&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
//    static final String USER = "postgres";
//    static final String PASS = "postgres";
//    static final String QUERY = "SELECT * FROM app";

//    static final String DB_URL = "jdbc:oracle:thin:@192.168.11.42:1521:orcl";
//    static final String USER = "platform";
//    static final String PASS = "PLATFORMzjzs1234";

    static final String DB_URL = "jdbc:oracle:thin:@192.168.100.30:1521:orcl";
    static final String USER = "bdcdj";
    static final String PASS = "bdcdj";

//    static final String INSERT = "insert into T_XM_FILEBAG (FBINST_GUID, BIZINST_GUID, BDFB_GUID, DISPLAY_TYPE, ATTACHFILENAME, DISPLAY_TEXT, P_FBINST_GUID, LINK_URL, ADD_USER, ADD_DATE, SEQ_ORDER, FB_CODE, BIZDEF_GUID, AUTO_EXPAND, REV_, GXSJ) " +
//            "values ('%s', '9aa769b4-866d-11e8-999c-7cd30a54be7a', '55c4df8e-516d-11e7-9a40-000c2999fc6a', 'form', null, '产权回溯', '9b1b398c-866d-11e8-999c-7cd30a54be7a', '{cptPath}/realestate/ReportServer?reportlet=realestate/common/ComPreInfo.cpt', 'system', to_date('13-07-2018 15:23:14', 'dd-mm-yyyy hh24:mi:ss'), 8, null, 'c8a1591e-4a59-11e5-ba75-000c2948487c', 0, 1, to_date('13-07-2018 15:23:14', 'dd-mm-yyyy hh24:mi:ss'))";
    static final String QUERY = "insert into T_XM_FILEBAG (FBINST_GUID, BIZINST_GUID, BDFB_GUID, DISPLAY_TYPE, ATTACHFILENAME, DISPLAY_TEXT, P_FBINST_GUID, LINK_URL, ADD_USER, ADD_DATE, SEQ_ORDER, FB_CODE, BIZDEF_GUID, AUTO_EXPAND) " +
        "values ('222122223994-866d-11e8-999c-7cd30a54be7a', '9aa769b4-866d-11e8-999c-7cd30a54be7a', '55c4df8e-516d-11e7-9a40-000c2999fc6a', 'form', null, '产权回溯', '9b1b398c-866d-11e8-999c-7cd30a54be7a', '{cptPath}/realestate/ReportServer?reportlet=realestate/common/ComPreInfo.cpt', 'system', to_date('13-07-2018 15:23:14', 'dd-mm-yyyy hh24:mi:ss'), 8, null, 'c8a1591e-4a59-11e5-ba75-000c2948487c', 0)";
    static final String INSERT = "insert into T_XM_FILEBAG (FBINST_GUID, BIZINST_GUID, BDFB_GUID, DISPLAY_TYPE, ATTACHFILENAME, DISPLAY_TEXT, P_FBINST_GUID, LINK_URL, ADD_USER, ADD_DATE, SEQ_ORDER, FB_CODE, BIZDEF_GUID, AUTO_EXPAND) " +
            "values ('%s', '9aa769b4-866d-11e8-999c-7cd30a54be7a', '55c4df8e-516d-11e7-9a40-000c2999fc6a', 'form', null, '产权回溯', '9b1b398c-866d-11e8-999c-7cd30a54be7a', '{cptPath}/realestate/ReportServer?reportlet=realestate/common/ComPreInfo.cpt', 'system', to_date('13-07-2018 15:23:14', 'dd-mm-yyyy hh24:mi:ss'), 8, null, 'c8a1591e-4a59-11e5-ba75-000c2948487c', 0)";
    static final String DELETE = "delete from T_XM_FILEBAG where FBINST_GUID='%s'";
    static final String DRIVERCLASS = "oracle.jdbc.driver.OracleDriver";
    static final DruidDataSource ds = new DruidDataSource() ;



static {
    ds.setUrl(DB_URL);
    ds.setUsername(USER);
    ds.setPassword(PASS);
    ds.setDriverClassName(DRIVERCLASS);
    ds.setMinIdle(10);
    ds.setInitialSize(10);
    ds.setMaxActive(20);
//        ds.setMaxIdle(10);
    ds.setMaxOpenPreparedStatements(100);
}


    public static void main(String[] args) throws InterruptedException {
        insert();
        //Thread.sleep(20000);
    }

    public static void main1(String[] args) throws InterruptedException {
        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    query();
                }
            }).start();
        }
        //Thread.sleep(20000);
    }

    public static void query(){
        try(Connection conn = ds.getConnection()) {
            Thread.sleep(1);
            try(Statement stmt = conn.createStatement()) {
                try(ResultSet rs = stmt.executeQuery(QUERY)) {

                    // Extract data from result set
                    while (rs.next()) {
                        // Retrieve by column name
                        System.out.println("1:" + rs.getString(0));
//                System.out.print("ID: " + rs.getInt("user_id"));
//                System.out.print(", NAME: " + rs.getInt("user_name"));
                    }
                }
            }

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void insert(){
        try(Connection conn = ds.getConnection()) {
            try(Statement stmt = conn.createStatement()) {
                for (int i = 0; i < 10; i++) {
                    UUID uuid = UUID.randomUUID();
                    String insertSql = String.format(INSERT, uuid);
                    String deleteSql = String.format(DELETE, uuid);
                    System.out.println("insertSql:" + insertSql);
                    System.out.println("deleteSql:" + deleteSql);
                    Long startTime = System.currentTimeMillis();
                    // 你的业务代码
                    int rows = stmt.executeUpdate(INSERT);
                    Long endTime = System.currentTimeMillis();
                    Long elapsedTime = (endTime - startTime) ;
                    System.out.println("该段总共耗时：" + elapsedTime + "ms");
                    System.out.println("affect rows:" + rows);
                    stmt.executeUpdate(DELETE);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
