package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

public class GestionSuministros {

    public static void bucleInteractivo(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Subsistema de Gestión de Suministros ---");
            System.out.println("1. Añadir suministro");
            System.out.println("2. Modificar suministro");
            System.out.println("3. Eliminar suministro");
            System.out.println("4. Mostrar suministros");
            System.out.println("0. Volver al menú principal");

            System.out.print("Elige una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();  // Consumir la nueva línea

            switch (opcion) {
                case 1:
                    anadirSuministro(conn, scanner);
                    break;
                case 2:
                    modificarSuministro(conn, scanner);
                    break;
                case 3:
                    eliminarSuministro(conn, scanner);
                    break;
                case 4:
                    mostrarSuministros(conn);
                    break;
                case 0:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    public static void borrarYCrearTablas(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS suministro (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(100)," +
                "cantidad INT," +
                "proveedor VARCHAR(100)," +
                "ultima_fecha_reposicion DATE" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS suministro");
            stmt.executeUpdate(sql);
            System.out.println("Tabla 'suministro' creada correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla 'suministro': " + e.getMessage());
        }
    }

    public static void anadirSuministro(Connection conn, Scanner scanner) {
        System.out.print("Introduce el nombre del suministro: ");
        String nombre = scanner.nextLine();

        System.out.print("Introduce la cantidad: ");
        int cantidad = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Introduce el proveedor: ");
        String proveedor = scanner.nextLine();

        System.out.print("Introduce la última fecha de reposición (YYYY-MM-DD): ");
        String fecha = scanner.nextLine();

        String sql = "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, cantidad);
            pstmt.setString(3, proveedor);
            pstmt.setDate(4, Date.valueOf(fecha));
            pstmt.executeUpdate();
            System.out.println("Suministro añadido correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al añadir el suministro: " + e.getMessage());
        }
    }

    public static void modificarSuministro(Connection conn, Scanner scanner) {
        System.out.print("Introduce el ID del suministro a modificar: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Introduce el nuevo nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Introduce la nueva cantidad: ");
        int cantidad = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Introduce el nuevo proveedor: ");
        String proveedor = scanner.nextLine();

        System.out.print("Introduce la nueva última fecha de reposición (YYYY-MM-DD): ");
        String fecha = scanner.nextLine();

        String sql = "UPDATE suministro SET nombre = ?, cantidad = ?, proveedor = ?, ultima_fecha_reposicion = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, cantidad);
            pstmt.setString(3, proveedor);
            pstmt.setDate(4, Date.valueOf(fecha));
            pstmt.setInt(5, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Suministro modificado correctamente.");
            } else {
                System.out.println("No se encontró un suministro con ese ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error al modificar el suministro: " + e.getMessage());
        }
    }

    public static void eliminarSuministro(Connection conn, Scanner scanner) {
        System.out.print("Introduce el ID del suministro a eliminar: ");
        int id = scanner.nextInt();

        String sql = "DELETE FROM suministro WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Suministro eliminado correctamente.");
            } else {
                System.out.println("No se encontró un suministro con ese ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar el suministro: " + e.getMessage());
        }
    }

    public static void mostrarSuministros(Connection conn) {
        String sql = "SELECT * FROM suministro";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- Lista de Suministros ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Nombre: " + rs.getString("nombre") + ", Cantidad: " + rs.getInt("cantidad") + ", Proveedor: " + rs.getString("proveedor") + ", Última Fecha de Reposición: " + rs.getDate("ultima_fecha_reposicion"));
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar los suministros: " + e.getMessage());
        }
    }
}
