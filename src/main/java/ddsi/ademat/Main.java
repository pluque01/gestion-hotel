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
                System.out.println("2. Subsistema de Gestión de Habitaciones");
                System.out.println("3. Subsistema de Facturación");
                System.out.println("4. Subsistema de Gestión de Trabajadores");
                System.out.println("5. Subsistema de Gestión de Suministros");
                System.out.println("6. Subsistema de Gestión de Clientes");
                System.out.println("7. Subsistema de Gestión de Servicios");
                System.out.println("8. Mostrar contenido de las tablas");
                System.out.println("0. Salir");

                System.out.print("Elige una opción: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    // TODO: añadir cada uno su método interactivo
                    case 1:
                        borrarYCrearTablas(conn);
                        break;
                    case 2:
                        GestionReservas.bucleInteractivo(conn);
                        break;
                    case 3:
                        Facturacion.bucleInteractivo(conn);
                        break;
                    case 4:
                        Trabajadores.bucleInteractivo(conn, scanner);
                        break;
                    case 5:
                        GestionSuministros.bucleInteractivo(conn);
                        break;
                    case 6:
                        GestionClientes.bucleInteractivo(conn);
                        break;
                    case 7:
                        Servicios.bucleOpciones(conn);
                        break;
                    case 8:
                        mostrarTablas(conn);
                        break;
                    case 0:
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
        GestionHotel.borrarTablas(conn);
        GestionClientes.crearTablas(conn);
        GestionReservas.crearTablas(conn);
        Trabajadores.crearTablas(conn);
        Facturacion.crearTablas(conn);
        Servicios.crearTablas(conn);
        GestionSuministros.crearTablas(conn);
        System.out.println("Tablas borradas y creadas con éxito");
        // TODO: Añadir cada uno su función de borrar y crear tablas
    }

    public static void mostrarTablas(Connection conn) {
        GestionClientes.mostrarTablas(conn);
        GestionReservas.mostrarTablas(conn);
        Trabajadores.mostrarTablas(conn);
        Facturacion.mostrarTablas(conn);
        Servicios.mostrarTablas(conn);
        GestionSuministros.mostrarSuministros(conn);
        // TODO: Añadir cada uno su función de mostrar tablas
    }
}