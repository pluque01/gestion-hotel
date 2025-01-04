package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

public class Trabajadores {
    public static void borrarYCrearTablas(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.executeUpdate("DROP TABLE Trabajadores");
            } catch (SQLException e) {
                // Ignorar el error si la tabla no existe
                if (!e.getMessage().contains("ORA-00942")) {
                    throw e;
                }
            }
            stmt.executeUpdate("CREATE TABLE Trabajadores ("
                    + "dni CHAR(9) NOT NULL,"
                    + "nombre VARCHAR(20) NOT NULL,"
                    + "apellidos VARCHAR(50) NOT NULL,"
                    + "domicilio VARCHAR(50) NOT NULL,"
                    + "telefono VARCHAR(20) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "puesto VARCHAR(20) NOT NULL CHECK (puesto IN ('ADMINISTRADOR', 'RECEPCIONISTA', 'LIMPIADOR')),"
                    + "nomina DECIMAL(10, 2) NOT NULL CHECK (nomina >= 0),"
                    + "PRIMARY KEY (dni)"
                    + ")");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bucleInteractivo(Connection conn, Scanner scanner) {
        boolean terminar = false;

        while (!terminar) {
            System.out.println("\n--- Menú de Gestión de Trabajadores ---");
            System.out.println("1. Dar de alta trabajador");
            System.out.println("2. Dar de baja trabajador");
            System.out.println("3. Modificar datos de trabajador");
            System.out.println("4. Consultar datos de trabajador");
            System.out.println("5. Mostrar listado de trabajadores");
            System.out.println("0. Salir");

            System.out.print("Elige una opción: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                switch (choice) {
                    case 1:
                        insertarTrabajador(conn, scanner);
                        break;
                    case 2:
                        eliminarTrabajador(conn, scanner);
                        break;
                    case 3:
                        modificarTrabajador(conn, scanner);
                        break;
                    case 4:
                        consultarTrabajador(conn, scanner);
                        break;
                    case 5:
                        mostrarTablas(conn);
                        break;
                    case 0:
                        terminar = true;
                        System.out.println("Saliendo del subsistema de Gestión de Trabajadores...");
                        break;
                    default:
                        System.out.println("Opción desconocida. Por favor, elija una opción válida.");
                }
            }
        }
    }

    public static void insertarTrabajador(Connection conn, Scanner scanner) {
        try {
            System.out.println("Introduzca los datos del trabajador:");
            System.out.print("DNI: ");
            String dni = scanner.nextLine();
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine();
            System.out.print("Apellidos: ");
            String apellidos = scanner.nextLine();
            System.out.print("Domicilio: ");
            String domicilio = scanner.nextLine();
            System.out.print("Teléfono: ");
            String telefono = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Puesto (ADMINISTRADOR, RECEPCIONISTA, LIMPIADOR): ");
            String puesto = scanner.nextLine();
            System.out.print("Nómina: ");
            double nomina = scanner.nextDouble();
            scanner.nextLine(); // Consumir el salto de línea

            String sql = "INSERT INTO Trabajadores (dni, nombre, apellidos, domicilio, telefono, email, puesto, nomina) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, dni);
                pstmt.setString(2, nombre);
                pstmt.setString(3, apellidos);
                pstmt.setString(4, domicilio);
                pstmt.setString(5, telefono);
                pstmt.setString(6, email);
                pstmt.setString(7, puesto);
                pstmt.setDouble(8, nomina);
                pstmt.executeUpdate();
                pstmt.close();
                System.out.println("Trabajador insertado correctamente.");
            }
        } catch (Exception e) {
            System.out.println("Error al insertar los datos: " + e.getMessage());
        }
    }

    public static void eliminarTrabajador(Connection conn, Scanner scanner) {
        try {
            System.out.print("Introduce el DNI del trabajador a eliminar: ");
            String dni = scanner.nextLine();

            String sql = "DELETE FROM Trabajadores WHERE dni = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, dni);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Trabajador eliminado correctamente.");
                } else {
                    System.out.println("No se encontró ningún trabajador con el DNI proporcionado.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al eliminar el trabajador: " + e.getMessage());
        }
    }

    public static void modificarTrabajador(Connection conn, Scanner scanner) {
        try {
            System.out.print("Introduce el DNI del trabajador a modificar: ");
            String dni = scanner.nextLine();

            String checkSql = "SELECT COUNT(*) FROM Trabajadores WHERE dni = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, dni);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    System.out.println("No se encontró ningún trabajador con el DNI proporcionado.");
                    return;
                }
            }

            System.out.println("Introduce los nuevos datos del trabajador (dejar en blanco para no modificar):");
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine();
            System.out.print("Apellidos: ");
            String apellidos = scanner.nextLine();
            System.out.print("Domicilio: ");
            String domicilio = scanner.nextLine();
            System.out.print("Teléfono: ");
            String telefono = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Puesto (ADMINISTRADOR, RECEPCIONISTA, LIMPIADOR): ");
            String puesto = scanner.nextLine();
            System.out.print("Nómina: ");
            String nominaStr = scanner.nextLine();

            StringBuilder sql = new StringBuilder("UPDATE Trabajadores SET ");
            boolean first = true;

            if (!nombre.isEmpty()) {
                sql.append("nombre = ?");
                first = false;
            }
            if (!apellidos.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("apellidos = ?");
                first = false;
            }
            if (!domicilio.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("domicilio = ?");
                first = false;
            }
            if (!telefono.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("telefono = ?");
                first = false;
            }
            if (!email.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("email = ?");
                first = false;
            }
            if (!puesto.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("puesto = ?");
                first = false;
            }
            if (!nominaStr.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("nomina = ?");
            }

            sql.append(" WHERE dni = ?");

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                int index = 1;
                if (!nombre.isEmpty()) pstmt.setString(index++, nombre);
                if (!apellidos.isEmpty()) pstmt.setString(index++, apellidos);
                if (!domicilio.isEmpty()) pstmt.setString(index++, domicilio);
                if (!telefono.isEmpty()) pstmt.setString(index++, telefono);
                if (!email.isEmpty()) pstmt.setString(index++, email);
                if (!puesto.isEmpty()) pstmt.setString(index++, puesto);
                if (!nominaStr.isEmpty()) pstmt.setDouble(index++, Double.parseDouble(nominaStr));
                pstmt.setString(index, dni);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Trabajador modificado correctamente.");
                } else {
                    System.out.println("No se pudo modificar el trabajador.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al modificar el trabajador: " + e.getMessage());
        }
    }

    public static void consultarTrabajador(Connection conn, Scanner scanner) {
        try {
            System.out.print("Introduce el DNI del trabajador a consultar: ");
            String dni = scanner.nextLine();

            String sql = "SELECT * FROM Trabajadores WHERE dni = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, dni);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    System.out.println("DNI: " + rs.getString("dni"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Apellidos: " + rs.getString("apellidos"));
                    System.out.println("Domicilio: " + rs.getString("domicilio"));
                    System.out.println("Teléfono: " + rs.getString("telefono"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Puesto: " + rs.getString("puesto"));
                    System.out.println("Nómina: " + rs.getDouble("nomina"));
                } else {
                    System.out.println("No se encontró ningún trabajador con el DNI proporcionado.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al consultar el trabajador: " + e.getMessage());
        }
    }

    public static void mostrarTablas(Connection conn) {
        try {
            System.out.println("Contenido de Trabajadores:");
            GestionHotel.mostrarTabla(conn, "Trabajadores");
        } catch (SQLException e) {
            System.out.println("Error al mostrar las tablas: " + e.getMessage());
        }
    }
}
