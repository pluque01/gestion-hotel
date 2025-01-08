package ddsi.ademat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class GestionReservas {
    public static void borrarYCrearTablas(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            // Borrar las tablas si existen
            stmt.executeUpdate("DROP TABLE IF EXISTS Incorpora");
            stmt.executeUpdate("DROP TABLE IF EXISTS Suplemento");
            stmt.executeUpdate("DROP TABLE IF EXISTS Habitacion");
            stmt.executeUpdate("DROP TABLE IF EXISTS Reserva");

            // Crear la tabla Habitacion
            stmt.executeUpdate("CREATE TABLE Habitacion ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "tipo ENUM('individual', 'doble', 'suite') NOT NULL,"
                    + "precioPorNoche DECIMAL(10,2) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")");

            // Crear la tabla Suplemento
            stmt.executeUpdate("CREATE TABLE Suplemento ("
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "cantidadDisponible INT NOT NULL,"
                    + "precio DECIMAL(10,2) NOT NULL,"
                    + "PRIMARY KEY (nombre)"
                    + ")");

            // Crear la tabla Reserva
            stmt.executeUpdate("CREATE TABLE Reserva ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "fechaInicio DATE NOT NULL,"
                    + "fechaFinal DATE NOT NULL,"
                    + "dni VARCHAR(9),"
                    + "habitacion INT NOT NULL,"
                    + "PRIMARY KEY (id),"
                    + "FOREIGN KEY (dni) REFERENCES Cliente(dni),"
                    + "FOREIGN KEY (habitacion) REFERENCES Habitacion(id)"
                    + ")");

            // Crear la tabla Incorpora
            stmt.executeUpdate("CREATE TABLE Incorpora ("
                    + "idReserva INT NOT NULL,"
                    + "nombreSuplemento VARCHAR(100) NOT NULL,"
                    + "PRIMARY KEY (idReserva, nombreSuplemento),"
                    + "FOREIGN KEY (idReserva) REFERENCES Reserva(id),"
                    + "FOREIGN KEY (nombreSuplemento) REFERENCES Suplemento(nombre)"
                    + ")");

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bucleInteractivo(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Subsistema de Gestión de Reservas ---");
            System.out.println("1. Reservar habitación");
            System.out.println("2. Cancelar reserva");
            System.out.println("3. Modificar reserva");
            System.out.println("4. Gestionar suplementos");
            System.out.println("5. Mostrar listado de reservas");
            System.out.println("0. Volver al menú principal");

            System.out.print("Elige una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            switch (opcion) {
                case 1:
                    reservarHabitacion(conn, scanner);
                    break;
                case 2:
                    cancelarReserva(conn, scanner);
                    break;
                case 3:
                    modificarReserva(conn, scanner);
                    break;
                case 4:
                    gestionarSuplementos(conn, scanner);
                    break;
                case 5:
                    mostrarListadoReservas(conn);
                    break;
                case 0:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    // Función para reservar habitación
    public static void reservarHabitacion(Connection conn, Scanner scanner) {
        try {
            // Pedir el DNI del cliente
            System.out.print("Introduce tu DNI (9 caracteres): ");
            String dni = scanner.nextLine();

            // Verificar si el DNI está registrado en la tabla Cliente
            String checkClienteSQL = "SELECT COUNT(*) FROM Cliente WHERE dni = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkClienteSQL)) {
                stmt.setString(1, dni);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("El DNI no está registrado.");
                    return;
                }
            }

            // Pedir las fechas de inicio y final de la estancia
            System.out.print("Introduce la fecha de inicio de la estancia (YYYY-MM-DD): ");
            String fechaInicio = scanner.nextLine();
            System.out.print("Introduce la fecha final de la estancia (YYYY-MM-DD): ");
            String fechaFinal = scanner.nextLine();

            // Pedir el tipo de habitación
            System.out.print("Introduce el tipo de habitación (individual, doble, suite): ");
            String tipoHabitacion = scanner.nextLine();

            // Verificar que existe una habitación disponible del tipo seleccionado
            String checkDisponibilidadSQL = "SELECT id FROM Habitacion WHERE tipo = ? AND id NOT IN (" +
                    "SELECT habitacion FROM Reserva WHERE (fechaInicio BETWEEN ? AND ? " +
                    "OR fechaFinal BETWEEN ? AND ?) AND habitacion IN (" +
                    "SELECT id FROM Habitacion WHERE tipo = ?)) LIMIT 1";
            int habitacionId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(checkDisponibilidadSQL)) {
                stmt.setString(1, tipoHabitacion);
                stmt.setString(2, fechaInicio);
                stmt.setString(3, fechaFinal);
                stmt.setString(4, fechaInicio);
                stmt.setString(5, fechaFinal);
                stmt.setString(6, tipoHabitacion);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    habitacionId = rs.getInt(1);
                }
            }

            if (habitacionId == -1) {
                System.out.println("No hay habitaciones disponibles para las fechas y tipo seleccionados.");
                return;
            }

            // Calcular el costo total de la reserva (precio por noche * número de noches)
            String getPrecioSQL = "SELECT precioPorNoche FROM Habitacion WHERE id = ?";
            double precioPorNoche = 0;
            try (PreparedStatement stmt = conn.prepareStatement(getPrecioSQL)) {
                stmt.setInt(1, habitacionId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    precioPorNoche = rs.getDouble(1);
                }
            }

            // Calcular número de noches
            Date inicio = Date.valueOf(fechaInicio);
            Date fin = Date.valueOf(fechaFinal);
            long dias = (fin.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24);

            double costoTotal = precioPorNoche * dias;

            // Insertar la reserva en la base de datos
            String insertarReservaSQL = "INSERT INTO Reserva (fechaInicio, fechaFinal, dni, habitacion, costoTotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertarReservaSQL)) {
                stmt.setString(1, fechaInicio);
                stmt.setString(2, fechaFinal);
                stmt.setString(3, dni);
                stmt.setInt(4, habitacionId);
                stmt.setDouble(5, costoTotal);
                stmt.executeUpdate();
            }

            System.out.println("Reserva realizada con éxito. Costo total: " + costoTotal + " EUR");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al realizar la reserva: " + e.getMessage());
        }
    }

    // Función para cancelar reserva
    public static void cancelarReserva(Connection conn, Scanner scanner) {
        try {
            // Pedir el ID de la reserva a cancelar
            System.out.print("Introduce el ID de la reserva a cancelar: ");
            int idReserva = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            // Verificar si la reserva existe en la tabla Reserva
            String checkReservaSQL = "SELECT COUNT(*) FROM Reserva WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkReservaSQL)) {
                stmt.setInt(1, idReserva);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("La reserva con ID " + idReserva + " no existe.");
                    return;
                }
            }

            // Eliminar los suplementos asociados a la reserva en la tabla Incorpora
            String deleteSuplementosSQL = "DELETE FROM Incorpora WHERE idReserva = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSuplementosSQL)) {
                stmt.setInt(1, idReserva);
                stmt.executeUpdate();
            }

            // Eliminar la factura asociada a la reserva en la tabla Factura
            String deleteFacturaSQL = "DELETE FROM Factura WHERE codReserva = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteFacturaSQL)) {
                stmt.setInt(1, idReserva);
                stmt.executeUpdate();
            }

            // Eliminar la reserva en la tabla Reserva
            String deleteReservaSQL = "DELETE FROM Reserva WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteReservaSQL)) {
                stmt.setInt(1, idReserva);
                stmt.executeUpdate();
            }

            System.out.println("Reserva con ID " + idReserva + " cancelada exitosamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al cancelar la reserva: " + e.getMessage());
        }
    }

    // Función para modificar reserva
    public static void modificarReserva(Connection conn, Scanner scanner) {
        try {
            // Pedir el ID de la reserva a modificar
            System.out.print("Introduce el ID de la reserva a modificar: ");
            int idReserva = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            // Verificar si la reserva existe en la tabla Reserva
            String checkReservaSQL = "SELECT COUNT(*) FROM Reserva WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkReservaSQL)) {
                stmt.setInt(1, idReserva);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("La reserva con ID " + idReserva + " no existe.");
                    return;
                }
            }

            // Pedir las nuevas fechas de inicio y final, o el nuevo tipo de habitación
            System.out.print("Introduce la nueva fecha de inicio (YYYY-MM-DD): ");
            String nuevaFechaInicio = scanner.nextLine();
            System.out.print("Introduce la nueva fecha final (YYYY-MM-DD): ");
            String nuevaFechaFinal = scanner.nextLine();

            // Pedir el nuevo tipo de habitación (opcional)
            System.out.print("Introduce el nuevo tipo de habitación (individual, doble, suite): ");
            String nuevoTipoHabitacion = scanner.nextLine();

            // Verificar disponibilidad de habitaciones del nuevo tipo para las nuevas
            // fechas
            String checkDisponibilidadSQL = "SELECT COUNT(*) FROM Habitacion h "
                    + "WHERE h.tipo = ? AND h.id NOT IN ("
                    + "SELECT r.habitacion FROM Reserva r "
                    + "WHERE r.fechaInicio < ? AND r.fechaFinal > ?)";

            try (PreparedStatement stmt = conn.prepareStatement(checkDisponibilidadSQL)) {
                stmt.setString(1, nuevoTipoHabitacion);
                stmt.setString(2, nuevaFechaFinal);
                stmt.setString(3, nuevaFechaInicio);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(
                            "No hay habitaciones disponibles del tipo " + nuevoTipoHabitacion + " para esas fechas.");
                    return;
                }
            }

            // Actualizar la reserva en la base de datos con las nuevas fechas o el nuevo
            // tipo de habitación
            String updateReservaSQL = "UPDATE Reserva SET fechaInicio = ?, fechaFinal = ?, habitacion = ("
                    + "SELECT id FROM Habitacion WHERE tipo = ? LIMIT 1) "
                    + "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateReservaSQL)) {
                stmt.setString(1, nuevaFechaInicio);
                stmt.setString(2, nuevaFechaFinal);
                stmt.setString(3, nuevoTipoHabitacion);
                stmt.setInt(4, idReserva);
                stmt.executeUpdate();
            }

            System.out.println("Reserva con ID " + idReserva + " modificada exitosamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al modificar la reserva: " + e.getMessage());
        }
    }

    // Función para gestionar suplementos
    public static void gestionarSuplementos(Connection conn, Scanner scanner) {
        try {
            // Menú interactivo para gestionar los suplementos
            System.out.println("Gestión de Suplementos");
            System.out.println("1. Mostrar suplementos disponibles");
            System.out.println("2. Añadir suplemento a reserva");
            System.out.println("3. Eliminar suplemento de reserva");
            System.out.println("0. Volver al menú principal");

            System.out.print("Elige una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            switch (opcion) {
                case 1:
                    mostrarSuplementosDisponibles(conn);
                    break;
                case 2:
                    anadirSuplementoReserva(conn, scanner);
                    break;
                case 3:
                    eliminarSuplementoReserva(conn, scanner);
                    break;
                case 0:
                    return; // Volver al menú principal
                default:
                    System.out.println("Opción inválida.");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al gestionar los suplementos: " + e.getMessage());
        }
    }

    private static void mostrarSuplementosDisponibles(Connection conn) {
        try {
            // Mostrar suplementos disponibles
            String sql = "SELECT nombre, cantidadDisponible, precio FROM Suplemento";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("Suplementos disponibles:");
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    int cantidad = rs.getInt("cantidadDisponible");
                    BigDecimal precio = rs.getBigDecimal("precio");
                    System.out.println(
                            "Nombre: " + nombre + ", Cantidad Disponible: " + cantidad + ", Precio: " + precio);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al mostrar los suplementos disponibles: " + e.getMessage());
        }
    }

    private static void anadirSuplementoReserva(Connection conn, Scanner scanner) {
        try {
            // Pedir el ID de la reserva y el suplemento a añadir
            System.out.print("Introduce el ID de la reserva: ");
            int idReserva = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            System.out.print("Introduce el nombre del suplemento: ");
            String nombreSuplemento = scanner.nextLine();

            // Verificar si la reserva existe
            String checkReservaSQL = "SELECT COUNT(*) FROM Reserva WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkReservaSQL)) {
                stmt.setInt(1, idReserva);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("La reserva con ID " + idReserva + " no existe.");
                    return;
                }
            }

            // Verificar si el suplemento existe y hay cantidad disponible
            String checkSuplementoSQL = "SELECT cantidadDisponible FROM Suplemento WHERE nombre = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSuplementoSQL)) {
                stmt.setString(1, nombreSuplemento);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int cantidadDisponible = rs.getInt("cantidadDisponible");
                    if (cantidadDisponible <= 0) {
                        System.out.println("No hay cantidad suficiente del suplemento " + nombreSuplemento + ".");
                        return;
                    }

                    // Si hay suficiente cantidad, añadir el suplemento a la reserva
                    String addSuplementoSQL = "INSERT INTO Incorpora (idReserva, nombreSuplemento) VALUES (?, ?)";
                    try (PreparedStatement stmtInsert = conn.prepareStatement(addSuplementoSQL)) {
                        stmtInsert.setInt(1, idReserva);
                        stmtInsert.setString(2, nombreSuplemento);
                        stmtInsert.executeUpdate();

                        // Decrementar la cantidad disponible del suplemento
                        String updateCantidadSQL = "UPDATE Suplemento SET cantidadDisponible = cantidadDisponible - 1 WHERE nombre = ?";
                        try (PreparedStatement stmtUpdate = conn.prepareStatement(updateCantidadSQL)) {
                            stmtUpdate.setString(1, nombreSuplemento);
                            stmtUpdate.executeUpdate();
                        }

                        System.out.println(
                                "Suplemento " + nombreSuplemento + " añadido a la reserva con ID " + idReserva);
                    }
                } else {
                    System.out.println("El suplemento " + nombreSuplemento + " no existe.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al añadir el suplemento: " + e.getMessage());
        }
    }

    private static void eliminarSuplementoReserva(Connection conn, Scanner scanner) {
        try {
            // Pedir el ID de la reserva y el suplemento a eliminar
            System.out.print("Introduce el ID de la reserva: ");
            int idReserva = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            System.out.print("Introduce el nombre del suplemento: ");
            String nombreSuplemento = scanner.nextLine();

            // Verificar si la reserva y el suplemento existen en la tabla Incorpora
            String checkSuplementoReservaSQL = "SELECT COUNT(*) FROM Incorpora WHERE idReserva = ? AND nombreSuplemento = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSuplementoReservaSQL)) {
                stmt.setInt(1, idReserva);
                stmt.setString(2, nombreSuplemento);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(
                            "No se encuentra el suplemento " + nombreSuplemento + " en la reserva con ID " + idReserva);
                    return;
                }

                // Eliminar el suplemento de la reserva
                String deleteSuplementoSQL = "DELETE FROM Incorpora WHERE idReserva = ? AND nombreSuplemento = ?";
                try (PreparedStatement stmtDelete = conn.prepareStatement(deleteSuplementoSQL)) {
                    stmtDelete.setInt(1, idReserva);
                    stmtDelete.setString(2, nombreSuplemento);
                    stmtDelete.executeUpdate();

                    // Incrementar la cantidad disponible del suplemento
                    String updateCantidadSQL = "UPDATE Suplemento SET cantidadDisponible = cantidadDisponible + 1 WHERE nombre = ?";
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(updateCantidadSQL)) {
                        stmtUpdate.setString(1, nombreSuplemento);
                        stmtUpdate.executeUpdate();
                    }

                    System.out
                            .println("Suplemento " + nombreSuplemento + " eliminado de la reserva con ID " + idReserva);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error al eliminar el suplemento: " + e.getMessage());
            }

        } catch (Exception e2) {
            e2.printStackTrace();
            System.out.println("Error al eliminar el suplemento de la reserva: " + e2.getMessage());
        }
    }

    // Función para mostrar listado de reservas
    public static void mostrarListadoReservas(Connection conn) {
        System.out.println("Listado de Reservas");

        String sql = "SELECT r.id, r.fechaInicio, r.fechaFinal, r.dni, r.habitacion, h.tipo, h.precioPorNoche, "
                + "(DATEDIFF(r.fechaFinal, r.fechaInicio) * h.precioPorNoche) AS precioFinal "
                + "FROM Reserva r "
                + "JOIN Habitacion h ON r.habitacion = h.id "
                + "ORDER BY r.fechaInicio";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println(
                    "ID Reserva | Fecha Inicio | Fecha Final | DNI Cliente | Habitación | Tipo Habitación | Precio Final");

            while (rs.next()) {
                int idReserva = rs.getInt("id");
                Date fechaInicio = rs.getDate("fechaInicio");
                Date fechaFinal = rs.getDate("fechaFinal");
                String dniCliente = rs.getString("dni");
                int numHabitacion = rs.getInt("habitacion");
                String tipoHabitacion = rs.getString("tipo");
                // BigDecimal precioPorNoche = rs.getBigDecimal("precioPorNoche");
                BigDecimal precioFinal = rs.getBigDecimal("precioFinal");

                System.out.println(idReserva + " | " + fechaInicio + " | " + fechaFinal + " | " + dniCliente + " | "
                        + numHabitacion + " | " + tipoHabitacion + " | " + precioFinal);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al mostrar el listado de reservas: " + e.getMessage());
        }
    }
}
