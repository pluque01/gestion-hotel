package ddsi.ademat;

import java.lang.Thread.State;
import java.sql.*;
import java.util.Scanner;

public class Facturacion {
    public static void borrarYCrearTablas(Connection conn) {
        // Creación de la tabla Factura
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS Factura");
            stmt.executeUpdate("CREATE TABLE Factura ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "concepto VARCHAR(255) NOT NULL,"
                    + "fecha DATE NOT NULL,"
                    + "codReserva INT NOT NULL,"
                    + "PRIMARY KEY (id),"
                    + "FOREIGN KEY (codReserva) REFERENCES Reserva(codReserva)"
                    + ")");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Inserción de datos en la tabla Factura
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO Factura (concepto, fecha, codReserva) VALUES ('Habitación doble', '2021-05-01', 1)");
            stmt.executeUpdate(
                    "INSERT INTO Factura (concepto, fecha, codReserva) VALUES ('Habitación individual', '2021-05-02', 2)");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mostrarTablas(Connection conn) {
        try {
            System.out.println("Contenido de Stock:");
            GestionHotel.mostrarTabla(conn, "Stock");
        } catch (SQLException e) {
            System.out.println("Error al mostrar las tablas: " + e.getMessage());
        }
    }

    public static void bucleInteractivo(Connection conn) {
        boolean terminar = false;
        Scanner scanner = new Scanner(System.in);

        while (!terminar) {
            System.out.println("\n--- Menú de Facturación ---");
            System.out.println("1. Añadir método de pago");
            System.out.println("2. Eliminar método de pago");
            System.out.println("3. Generar factura");
            System.out.println("4. Consultar factura");
            System.out.println("5. Reembolsar factura");
            System.out.println("0. Salir");

            System.out.print("Elige una opción: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    añadirMetodoPago(conn);
                    break;
                case 0:
                    terminar = true;
                    System.out.println("Saliendo del subsistema de Facturación...");
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    public static void añadirMetodoPago(Connection conn) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nIndique el NIF del cliente:");
        String nif = scanner.nextLine();
        System.out.print("Indique el número de la tarjeta:");
        String numTarjeta = scanner.nextLine();

        try {
            Statement stmt = conn.createStatement();
            stmt.executeQuery("SELECT * FROM Cliente WHERE nif = '" + nif + "'");
            ResultSet rs = stmt.getResultSet();
            if (!rs.next()) {
                System.out.println("El cliente no existe.");
                return;
            } else if (rs.getString("numTarjeta") != null) {
                System.out.println("El cliente ya tiene un método de pago asociado.");
                return;
            }
            stmt.executeUpdate("UPDATE Cliente SET numTarjeta = '" + numTarjeta + "' WHERE nif = '" + nif + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}