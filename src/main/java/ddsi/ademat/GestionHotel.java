package ddsi.ademat;

import java.sql.*;

public class GestionHotel {
    // Función auxiliar para mostrar el contenido de una tabla
    public static void mostrarTabla(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT * FROM " + tableName;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + ": " + rs.getObject(i) + " | ");
                }
                System.out.println();
            }
        }
    }
}