package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

public class Facturacion {
    public static void borrarYCrearTablas(Connection conn) {
        try {
            Statement stmt = conn.createStatement();

            //BORRA LA TABLA SI EXISTE, NO SOPORTA LA SENTENCIA IF EXIST
            stmt.executeUpdate(
                    "BEGIN " +
                            "   EXECUTE IMMEDIATE 'DROP TABLE Factura'; " +
                            "EXCEPTION " +
                            "   WHEN OTHERS THEN " +
                            "      IF SQLCODE != -942 THEN " +
                            "         RAISE; " +
                            "      END IF; " +
                            "END;");

            stmt.executeUpdate("CREATE TABLE Factura ("
                    + "id NUMBER NOT NULL,"    //NO PODEMOS USAR AUTO INCREMENT
                    + "fecha DATE NOT NULL,"
                    + "cliente NUMBER NOT NULL,"
                    + "PRIMARY KEY (id)"
                    //+ "FOREIGN KEY (cliente) REFERENCES Cliente(id)"
                    + ")");
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
        // Implementar
    }
}