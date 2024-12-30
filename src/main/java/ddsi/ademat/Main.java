package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("URL");
        String user = dotenv.get("USER");
        String password = dotenv.get("PASSWORD");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexión establecida con éxito a la base de datos");

            boolean exit = false;
            Scanner scanner = new Scanner(System.in);

            while (!exit) {
                System.out.println("\n---- Menú Principal ----");
                System.out.println("1. Borrar y crear tablas");
                System.out.println("2. Dar de alta nuevo pedido");
                System.out.println("3. Mostrar contenido de las tablas");
                System.out.println("4. Salir");

                System.out.print("Elige una opción: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        borrarYCrearTablas(conn);
                        break;
                    case 2:
                        darAltaPedido(conn);
                        break;
                    case 3:
                        mostrarTablas(conn);
                        break;
                    case 4:
                        exit = true;
                        System.out.println("Cerrando aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
            scanner.close();
        } catch (SQLException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
        }
    }

    public static void borrarYCrearTablas(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Borrar las tablas si existen
            try {
                stmt.executeUpdate("DROP TABLE Detalle_Pedido");
                stmt.executeUpdate("DROP TABLE Pedido");
                stmt.executeUpdate("DROP TABLE Stock");
            } catch (SQLException e) {
                System.out.println("Error al eliminar las tablas: " + e.getMessage());
            }

            // Crear las tablas
            stmt.executeUpdate("CREATE TABLE Stock (Cproducto VARCHAR2(10) PRIMARY KEY, cantidad NUMBER)");
            stmt.executeUpdate(
                    "CREATE TABLE Pedido (Cpedido VARCHAR2(10) PRIMARY KEY, Ccliente VARCHAR2(50), Fecha_Pedido DATE)");
            stmt.executeUpdate(
                    "CREATE TABLE Detalle_Pedido (Cpedido VARCHAR2(10), Cproducto VARCHAR2(10), Cantidad NUMBER, " +
                            "FOREIGN KEY (Cpedido) REFERENCES Pedido(Cpedido), " +
                            "FOREIGN KEY (Cproducto) REFERENCES Stock(Cproducto))");

            // Insertar datos de prueba en Stock
            for (int i = 1; i <= 10; i++) {
                String sql = "INSERT INTO Stock (Cproducto, cantidad) VALUES ('P" + i + "', " + (i * 10) + ")";
                stmt.executeUpdate(sql);
            }

            System.out.println("Tablas creadas e inicializadas con datos de prueba.");
        } catch (SQLException e) {
            System.out.println("Error al crear las tablas: " + e.getMessage());
        }
    }

    public static void darAltaPedido(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        Savepoint savepoint1 = null;

        try {
            conn.setAutoCommit(false);

            System.out.print("Ingrese el código del pedido: ");
            String cPedido = scanner.nextLine();

            System.out.print("Ingrese el código del cliente: ");
            String cCliente = scanner.nextLine();

            System.out.print("Ingrese la fecha del pedido (YYYY-MM-DD): ");
            String fechaPedido = scanner.nextLine();

            String sqlInsertPedido = "INSERT INTO Pedido (Cpedido, Ccliente, Fecha_Pedido) VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertPedido)) {
                pstmt.setString(1, cPedido);
                pstmt.setString(2, cCliente);
                pstmt.setString(3, fechaPedido);
                pstmt.executeUpdate();
            }

            savepoint1 = conn.setSavepoint("Savepoint1");

            boolean continuar = true;
            while (continuar) {
                System.out.println("\nContenido de table Pedido:");
                mostrarTabla(conn, "Pedido");
                System.out.println("\nContenido de tabla Detalle_Pedido:");
                mostrarTabla(conn, "Detalle_Pedido");
                System.out.println("1. Añadir detalle de producto");
                System.out.println("2. Eliminar todos los detalles de producto");
                System.out.println("3. Cancelar pedido");
                System.out.println("4. Finalizar pedido");
                System.out.print("Elige una opción: ");
                int opcion = scanner.nextInt();

                switch (opcion) {
                    case 1:
                        añadirDetalleProducto(conn, cPedido);
                        break;
                    case 2:
                        eliminarDetallesPedido(conn, cPedido, savepoint1);
                        break;
                    case 3:
                        cancelarPedido(conn, cPedido, savepoint1);
                        continuar = false;
                        break;
                    case 4:
                        conn.commit();
                        System.out.println("Pedido guardado exitosamente.");
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en alta de pedido: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
        } finally {
            // scanner.close();
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }

    // Función auxiliar para añadir un detalle de producto
    private static void añadirDetalleProducto(Connection conn, String cPedido) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el código del producto: ");
        String cProducto = scanner.nextLine();
        System.out.print("Ingrese la cantidad: ");
        int cantidad = scanner.nextInt();

        // Verifica el stock antes de insertar
        String sqlStock = "SELECT cantidad FROM Stock WHERE Cproducto = ?";
        try (PreparedStatement pstmtStock = conn.prepareStatement(sqlStock)) {
            pstmtStock.setString(1, cProducto);
            ResultSet rs = pstmtStock.executeQuery();

            if (rs.next()) {
                int stockDisponible = rs.getInt("cantidad");
                if (stockDisponible >= cantidad) {
                    // Inserta en Detalle_Pedido
                    String sqlDetalle = "INSERT INTO Detalle_Pedido (Cpedido, Cproducto, Cantidad) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle)) {
                        pstmtDetalle.setString(1, cPedido);
                        pstmtDetalle.setString(2, cProducto);
                        pstmtDetalle.setInt(3, cantidad);
                        pstmtDetalle.executeUpdate();

                        // Actualiza el stock
                        String sqlUpdateStock = "UPDATE Stock SET cantidad = cantidad - ? WHERE Cproducto = ?";
                        try (PreparedStatement pstmtUpdateStock = conn.prepareStatement(sqlUpdateStock)) {
                            pstmtUpdateStock.setInt(1, cantidad);
                            pstmtUpdateStock.setString(2, cProducto);
                            pstmtUpdateStock.executeUpdate();
                        }
                    }
                } else {
                    System.out.println("Stock insuficiente para el producto " + cProducto);
                }
            } else {
                System.out.println("Producto no encontrado.");
            }
        }
    }

    // Función auxiliar para eliminar los detalles del pedido actual
    private static void eliminarDetallesPedido(Connection conn, String cPedido, Savepoint savepoint1)
            throws SQLException {
        conn.rollback(savepoint1);
    }

    // Función auxiliar para cancelar el pedido
    private static void cancelarPedido(Connection conn, String cPedido, Savepoint savepoint1) throws SQLException {
        conn.rollback();
    }

    public static void mostrarTablas(Connection conn) {
        try {
            System.out.println("Contenido de Stock:");
            mostrarTabla(conn, "Stock");

            System.out.println("\nContenido de Pedido:");
            mostrarTabla(conn, "Pedido");

            System.out.println("\nContenido de Detalle_Pedido:");
            mostrarTabla(conn, "Detalle_Pedido");

        } catch (SQLException e) {
            System.out.println("Error al mostrar las tablas: " + e.getMessage());
        }
    }

    // Función auxiliar para mostrar el contenido de una tabla
    private static void mostrarTabla(Connection conn, String tableName) throws SQLException {
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