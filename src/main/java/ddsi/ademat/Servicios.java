package ddsi.ademat;

import java.sql.*;
import java.util.Scanner;

public class Servicios {
    public static void bucleOpciones(Connection conn) {
        boolean terminar = false;
        Scanner scanner = new Scanner(System.in);

        while (!terminar) {
            System.out.println("\n--- Menú de Servicios ---");
            System.out.println("1. Añadir actividad");
            System.out.println("2. Eliminar actividad");
            System.out.println("3. Contratar actividad");
            System.out.println("4. Cancelar actividad");
            System.out.println("5. Mostrar todas actividades");
            System.out.println("6. Mostrar actividades con filtrado");
            System.out.println("0. Salir");

            System.out.print("Elige una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 1:

                    break;
                case 0:
                    terminar = true;
                    System.out.println("Saliendo del subsistema de Servicios...");
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    public static void resetearTablas(Connection conn) {
        try {
            Statement stmt = conn.createStatement();

            //Eliminar tabla contrata si existe
            stmt.executeUpdate(
                    "BEGIN " +
                            "   EXECUTE IMMEDIATE 'DROP TABLE contrata'; " +
                            "EXCEPTION " +
                            "   WHEN OTHERS THEN " +
                            "      IF SQLCODE != -942 THEN " +
                            "         RAISE; " +
                            "      END IF; " +
                            "END;");

            //Eliminar tabla Actividad si existe
            stmt.executeUpdate(
                    "BEGIN " +
                            "   EXECUTE IMMEDIATE 'DROP TABLE Actividad'; " +
                            "EXCEPTION " +
                            "   WHEN OTHERS THEN " +
                            "      IF SQLCODE != -942 THEN " +
                            "         RAISE; " +
                            "      END IF; " +
                            "END;");

            //Tabla Actividad
            stmt.executeUpdate("CREATE TABLE Actividad ("
                    + "id NUMBER NOT NULL,"
                    + "nombre VARCHAR2(255) NOT NULL,"
                    + "precio NUMBER(10, 2) NOT NULL,"
                    + "horario DATE  NOT NULL,"
                    + "aforo NUMBER NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")");

            //Tabla Contrata
            stmt.executeUpdate("CREATE TABLE contrata ("
                    + "dni VARCHAR2(20) NOT NULL,"
                    + "id NUMBER NOT NULL,"
                    + "PRIMARY KEY (dni, id),"
                    //+ "FOREIGN KEY (dni) REFERENCES Cliente(dni),"
                    + "FOREIGN KEY (id) REFERENCES Actividad(id)"
                    + ")");
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
