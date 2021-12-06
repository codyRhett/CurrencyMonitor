import java.sql.*;

public class DbHandler {
    // Константа, в которой хранится адрес подключения
    private static final String CURRTABLE = "jdbc:sqlite:/home/artem/CurrencyMonitor/test.sqlite";

    // Объект, в котором будет храниться соединение с БД
    public static Connection connection;

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(CURRTABLE);
        System.out.println("База Подключена!");
    }

    public void addCurrency(String currency, float rate) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO test(`Currency`, `Rate`) " +
                        "VALUES(?, ?)")){
            statement.setObject(1, currency);
            statement.setObject(2, rate);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearTable() {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM test")){
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
