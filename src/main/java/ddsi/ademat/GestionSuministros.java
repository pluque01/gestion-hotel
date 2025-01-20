package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

public class GestionSuministros {

    public static void bucleInteractivo(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        try {
            conn.setAutoCommit(false);

            while (!salir) {
                System.out.println("\n--- Subsistema de Gestión de Suministros ---");
                System.out.println("1. Añadir suministro");
                System.out.println("2. Modificar suministro");
                System.out.println("3. Eliminar suministro");
                System.out.println("4. Mostrar suministros");
                System.out.println("5. Filtrar productos por proveedor");
                System.out.println("6. Descartar cambios");
                System.out.println("0. Guardar cambios y salir");

                System.out.print("Elige una opción: ");
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea

                switch (opcion) {
                    case 1:
                        anadirSuministro(conn, scanner);
                        break;
                    case 2:
                        if (!haySuministros(conn)) {
                            System.out.println("No hay suministros para modificar.");
                        } else {
                            modificarSuministro(conn, scanner);
                        }
                        break;
                    case 3:
                        if (!haySuministros(conn)) {
                            System.out.println("No hay suministros para eliminar.");
                        } else {
                            eliminarSuministro(conn, scanner);
                        }
                        break;
                    case 4:
                        mostrarTablas(conn);
                        break;
                    case 5:
                        filtrarProductosPorProveedor(conn, scanner);
                        break;
                    case 6:
                        try {
                            conn.rollback();
                            System.out.println("Se han descartado todos los cambios no guardados.");
                        } catch (SQLException e) {
                            System.out.println("Error al descartar los cambios: " + e.getMessage());
                        }
                        break;
                    case 0:
                        try {
                            conn.commit();
                            System.out.println("Cambios guardados correctamente. Saliendo...");
                            salir = true;
                        } catch (SQLException e) {
                            System.out.println("Error al guardar los cambios: " + e.getMessage());
                        }
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al configurar la transacción: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error al restaurar el modo de autocommit: " + e.getMessage());
            }
        }
    }

    public static void filtrarProductosPorProveedor(Connection conn, Scanner scanner) {
        System.out.print("Introduce el nombre del proveedor: ");
        String proveedor = scanner.nextLine();

        String sql = "SELECT id, nombre, cantidad, ultima_fecha_reposicion FROM suministro WHERE proveedor = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, proveedor);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hayResultados = false;
                System.out.println("\n--- Productos del proveedor: " + proveedor + " ---");
                while (rs.next()) {
                    hayResultados = true;
                    System.out.printf("ID: %d, Nombre: %s, Cantidad: %d, Última Fecha de Reposición: %s\n",
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDate("ultima_fecha_reposicion"));
                }
                if (!hayResultados) {
                    System.out.println("No se encontraron productos para el proveedor especificado.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al filtrar productos por proveedor: " + e.getMessage());
        }
    }

    public static void crearTablas(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String sqlTabla = "CREATE TABLE suministro (" +
                    "id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
                    "nombre VARCHAR2(100)," +
                    "cantidad NUMBER," +
                    "proveedor VARCHAR2(100)," +
                    "ultima_fecha_reposicion DATE" +
                    ")";
            stmt.executeUpdate(sqlTabla);
    
            String sqlTrigger = "CREATE OR REPLACE TRIGGER trg_suministro_duplicado " +
                    "BEFORE INSERT OR UPDATE ON suministro " +
                    "FOR EACH ROW " +
                    "DECLARE " +
                    "v_count NUMBER; " +
                    "BEGIN " +
                    "SELECT COUNT(*) INTO v_count " +
                    "FROM suministro " +
                    "WHERE nombre = :NEW.nombre AND proveedor = :NEW.proveedor; " +
                    "IF v_count > 0 THEN " +
                    "RAISE_APPLICATION_ERROR(-20001, 'Error: Producto duplicado con el mismo nombre y proveedor.'); " +
                    "END IF; " +
                    "END;";
            stmt.executeUpdate(sqlTrigger);
            System.out.println("Disparador 'trg_suministro_duplicado' creado o reemplazado correctamente.");
    
            // System.out.println("Tabla 'suministro' y trigger creados correctamente.");
    
            // Insertar suministros significativos
            try {
                stmt.executeUpdate(
                        "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) " +
                        "VALUES ('Papel A4', 500, 'Papeleria Central', TO_DATE('2025-01-01', 'YYYY-MM-DD'))");
                stmt.executeUpdate(
                        "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) " +
                        "VALUES ('Toner Negro', 50, 'TonerPlus', TO_DATE('2025-01-05', 'YYYY-MM-DD'))");
                stmt.executeUpdate(
                        "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) " +
                        "VALUES ('Lapices HB', 300, 'Articulos Escolares', TO_DATE('2025-01-10', 'YYYY-MM-DD'))");
                stmt.executeUpdate(
                        "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) " +
                        "VALUES ('Cafe en Grano', 100, 'Cafeteria La Mejor', TO_DATE('2025-01-15', 'YYYY-MM-DD'))");
                stmt.executeUpdate(
                        "INSERT INTO suministro (nombre, cantidad, proveedor, ultima_fecha_reposicion) " +
                        "VALUES ('Carpetas A4', 200, 'Papeleria Central', TO_DATE('2025-01-20', 'YYYY-MM-DD'))");
    
                conn.commit(); // Confirmar todos los cambios
                // System.out.println("Suministros iniciales añadidos correctamente.");
            } catch (SQLException e) {
                System.out.println("Error al insertar suministros iniciales: " + e.getMessage());
                try {
                    conn.rollback(); // Revertir en caso de error
                    System.out.println("Se han revertido los cambios debido a un error.");
                } catch (SQLException ex) {
                    System.out.println("Error al intentar revertir los cambios: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar o crear la tabla 'suministro': " + e.getMessage());
        }
    }
    

    public static boolean haySuministros(Connection conn) {
        String sql = "SELECT COUNT(*) FROM suministro";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar si hay suministros: " + e.getMessage());
        }
        return false;
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

    public static void mostrarTablas(Connection conn) {
        try {
            System.out.println("Contenido de Suministro:");
            GestionHotel.mostrarTabla(conn, "suministro");
        } catch (SQLException e) {
            System.out.println("Error al mostrar las tablas: " + e.getMessage());
        }
    }
}
