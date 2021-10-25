package netty;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class AuthService {

    private Connection connection;
    private String clientPass;


    public AuthService() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/users.db");
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "users", null);
            if (!tables.next() || (tables.next() && !tables.getString("TABLE_NAME").equals("users"))) {
                String createCommand = "CREATE TABLE users (Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, LOGIN TEXT NOT NULL, PASS TEXT NOT NULL)";
                Statement stmt = connection.createStatement();
                stmt.executeUpdate(createCommand);
                String insertCommand = "insert into users (login,pass)" +
                        "values (\"login1\",\"pass1\")," +
                        "(\"login2\",\"pass2\")," +
                        "(\"login3\",\"pass3\")" ;
                stmt.executeUpdate(insertCommand);
            }
        } catch (Exception e) {
            log.error("Ошибка при создании базы данных", e);
        }
    }

    public boolean authentication(String login, String pass) {
        try {
            String selectCommand = "select pass from users where login = ?";
            PreparedStatement queryStmt = connection.prepareStatement(selectCommand);
            queryStmt.setString(1,login);
            ResultSet res = queryStmt.executeQuery();
            clientPass = res.getString("PASS");
            return clientPass.equals(pass);
        } catch(SQLException e) {
            log.error("Ошибка при аутентификации", e);
            return false;
        }
    }

}
