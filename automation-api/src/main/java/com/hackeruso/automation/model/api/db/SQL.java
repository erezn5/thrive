package com.hackeruso.automation.model.api.db;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.ssh.Shell;

import java.sql.*;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class SQL {
    private static final String SSH_USER = EnvConf.getProperty("aws.username");
    private static final String SSH_HOST = EnvConf.getProperty("aws.hostname");
    private static final String RDS_HOST_NAME = EnvConf.getProperty("rds.hostname");
    private static final String BASTION_PEM_FILE = EnvConf.getProperty("bastion.pem.file.location");
    private static final int SQL_LOGIN_TIMEOUT_IN_SECONDS = 60;
    private static int FORWARDED_PORT;
    private Connection con;
    private static final String DB_NAME = EnvConf.getProperty("rds.db.name");
    private static final String USERNAME = EnvConf.getProperty("rds.username");
    private static final String PASSWORD = EnvConf.getProperty("rds.password");
    private static final String OS_NAME = System.getProperty("os.name");
    private static String JDBC_CONNECTION_STRING;

    public SQL() throws Exception {
        if(!OS_NAME.equals("Linux")) {
            establishSSHConnection();
            JDBC_CONNECTION_STRING = "jdbc:mysql://localhost:" + FORWARDED_PORT + "/" + DB_NAME + "?user=" + USERNAME + "&password=" + PASSWORD;
        }else{
            JDBC_CONNECTION_STRING = "jdbc:mysql://" + RDS_HOST_NAME + ":3306/" + DB_NAME + "?user=" + USERNAME + "&password=" + PASSWORD;
        }
    }

    public int executeUpdate(String query) throws SQLException {
        validateSQLConnectionCertainty();
        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate(query);
        Log.i("Query result of [%s] is [%s]", query, result);
        return result;
    }

    // e.g for example - "select * from users;"
    public ResultSet executeQuery(String query) {
        Statement stmt;
        ResultSet rs = null;
        try {
            validateSQLConnectionCertainty();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            Log.i("Query result=[%s]", rs);

        } catch (SQLException e) {
            Log.e(e.getMessage());
        }
        return rs;
    }

    private void validateSQLConnectionCertainty() throws SQLException {
        if (con == null) {
            con = getJDBCConnection();
        } else {
            if (con.isClosed()) {
                con = getJDBCConnection();
            }
        }
    }

    private static void establishSSHConnection() throws Exception {
        Shell.ChannelHandler channelHandler =
                Shell.builder()
                        .setBastionPemFile(BASTION_PEM_FILE)
                        .setHost(SSH_HOST).setUser(SSH_USER).build();
        FORWARDED_PORT = channelHandler.portForwarding(0, RDS_HOST_NAME, 3306);
    }

    private static Connection getJDBCConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(SQL_LOGIN_TIMEOUT_IN_SECONDS);
            Connection con = DriverManager.getConnection(JDBC_CONNECTION_STRING);
            Log.info("Connection by [" + JDBC_CONNECTION_STRING + "] to DB is successful.");
            return con;
        } catch (ClassNotFoundException | SQLException e) {
            Log.e(e.getMessage());
        }
        return null;
    }

    public void setNotFirstLogin(String userEmail) throws SQLException {
        String SET_NOT_FIRST_LOGIN_QUERY = "update users SET is_first_login=0 where users.email='%s';";
        executeUpdate(String.format(SET_NOT_FIRST_LOGIN_QUERY, userEmail));
    }

    public void closeDBConnection() throws SQLException {
        if (!con.isClosed()) {
            con.close();
        }
    }

    public String getQuerySingleValue(String sqlStatement) throws SQLException {
        ResultSet resultset = executeQuery(sqlStatement);
        return resultset.next() ? resultset.getString(1) : "";
    }
}
