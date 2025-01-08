package ddsi.ademat;

import java.lang.Thread.State;
import java.sql.*;
import java.util.Scanner;

public class GestionClientes {

    /*
    Función para comprobar que una cadena sea entera y que no salte una excepción
    que acabe con la ejecución de nuestro programa
     */
    static boolean esEntero(String cadena) {
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean esUnica(Statement stmt, String dni) {
        String sql = "select * from Cliente where dni = '" + dni + "'";
        boolean esUnica = true;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                esUnica = false;
            }
        } catch (Exception e) {

        }
        return esUnica;
    }

    public static void borrarYCrearTablas(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("BEGIN EXECUTE IMMEDIATE 'DROP TABLE Cliente'; EXCEPTION WHEN OTHERS THEN NULL; END;");
            stmt.executeUpdate("CREATE TABLE Cliente ("
                    + "nombre VARCHAR(20),"
                    + "apellidos VARCHAR(40),"
                    + "telefono VARCHAR(20),"
                    + "dni VARCHAR(9) CONSTRAINT dni_clave_primaria PRIMARY KEY,"
                    + "domicilio VARCHAR(60),"
                    + "email VARCHAR(20) CONSTRAINT email_clave_candidata UNIQUE NOT NULL"
                    + "puntos INT,"
                    + "rango ENUM('Inicial', 'Avanzado', 'VIP', 'Platino')"
                    + "tarjeta VARCHAR(20)"
                    + "FOREIGN KEY (cliente) REFERENCES Cliente(dni)"
                    + ")");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mostrarTablas(Connection conn) {
        try {
            System.out.println("Contenido de Clientes:");
            GestionHotel.mostrarTabla(conn, "Cliente");
        } catch (SQLException e) {
            System.out.println("Error al mostrar las tablas: " + e.getMessage());
        }
    }

    public static void bucleInteractivo(Connection conn) {
        boolean terminar = false;
        Scanner scanner = new Scanner(System.in);

        while (!terminar) {
            System.out.println("\n--- Menú de Clientes ---");
            System.out.println("1. Dar de alta cliente");
            System.out.println("2. Dar de baja cliente");
            System.out.println("3. Consultar información de cliente");
            System.out.println("4. Modificar información de cliente");
            System.out.println("5. Consultar rango de cliente");
            System.out.println("0. Salir");

            System.out.print("Elige una opción: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    darAltaCliente(conn);
                    break;
                case 2:
                    darBajaCliente(conn);
                    break;
                case 3:
                    consultarCliente(conn);
                    break;
                case 4:
                    modificarCliente(conn);
                    break;
                case 5:
                    consultarRangoCliente(conn);
                    break;
                case 0:
                    terminar = true;
                    System.out.println("Saliendo del subsistema de Cliente...");
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    public static void darAltaCliente(Connection conn) {
        // try {
        //     Statement stmt = conn.createStatement();
        //     stmt.executeUpdate("INSERT INTO Cliente VALUES ('Rafael','Cordoba Lopez','684848493','28394823G','','Mesones 54','rafacorlopg@gmail.com', 0, 'Inicial')");
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }

        String sql;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        boolean errores = false;
        System.out.println("\nPor favor indique el NOMBRE del Cliente, si quiere: \n");
        String nombreCliente;
        Scanner scan4 = new Scanner(System.in);
        nombreCliente = scan4.nextLine();
        while (nombreCliente.length() > 20) {
            System.out.println("\nNombre no válido, intentelo de nuevo\n");
            Scanner scanNombre = new Scanner(System.in);
            nombreCliente = scanNombre.nextLine();
        }
        System.out.println("\nPor favor indique los APELLIDOS del Cliente, si quiere: \n");
        String apellidoCliente;
        Scanner scanEmpleado = new Scanner(System.in);
        apellidoCliente = scanEmpleado.nextLine();
        while (nombreCliente.length() > 40) {
            System.out.println("\nApellidos no válido, intentelo de nuevo\n");
            Scanner scanAp = new Scanner(System.in);
            apellidoCliente = scanAp.nextLine();
        }

        System.out.println("\nPor favor indique el TELÉFONO del Cliente, si quiere: \n");
        String Telefono;
        Scanner scanTelefono = new Scanner(System.in);
        Telefono = scanTelefono.nextLine();
        while (Telefono.length() > 20 || (!esEntero(Telefono) && Telefono.length() > 1)) {
            System.out.println("\nTelefono no válido, intentelo de nuevo\n");
            Scanner scanTel = new Scanner(System.in);
            Telefono = scanTel.nextLine();
        }

        System.out.println("\nPor favor indique el DNI del Cliente: \n");
        String DNI;
        Scanner scanDNI = new Scanner(System.in);
        DNI = scanDNI.nextLine();
        while (DNI.length() > 9 && DNI.length() < 1) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIEmp = new Scanner(System.in);
            DNI = scanDNIEmp.nextLine();
        }

        System.out.println("\nPor favor indique el DOMICILIO del cliente, si quiere: \n");
        String Domicilio;
        Scanner scanDomicilio = new Scanner(System.in);
        Domicilio = scanDomicilio.nextLine();
        while (Domicilio.length() > 60) {
            System.out.println("\nDOMICILIO no válido, intentelo de nuevo\n");
            scanDomicilio = new Scanner(System.in);
            Domicilio = scanDomicilio.nextLine();
        }

        System.out.println("\nPor favor indique el CORREO del cliente: \n");
        String correo;
        Scanner scancorreo = new Scanner(System.in);
        correo = scancorreo.nextLine();
        while (correo.length() > 20 || (!esUnica(stmt, correo) && Telefono.length() > 1)) {
            System.out.println("\nCorreo no válido o ya existente, intentelo de nuevo\n");
            scancorreo = new Scanner(System.in);
            correo = scancorreo.nextLine();
        }
        if (esUnica(stmt, correo)) {
            System.out.println("\nEs Unica\n");
        }

        System.out.println("\nPor favor indique los PUNTOS del cliente, si quiere: \n");
        String puntos;
        Scanner scanPuntos = new Scanner(System.in);
        puntos = scanPuntos.nextLine();
        while (!esEntero(puntos)) {
            System.out.println("\n Puntos no válidos, intentelo de nuevo\n");
            scanPuntos = new Scanner(System.in);
            puntos = scanPuntos.nextLine();
        }

        System.out.println("\nPor favor indique el Rango del cliente, si quiere: \n");
        String rango;
        Scanner scanRango = new Scanner(System.in);
        rango = scanRango.nextLine();
        while (rango.length() > 20) {
            System.out.println("\n Rango no válido, intentelo de nuevo\n");
            scanRango = new Scanner(System.in);
            rango = scanRango.nextLine();
        }

        System.out.println("\nPor favor indique la TARJETA del cliente, si quiere: \n");
        String tarjeta;
        Scanner scanBanc = new Scanner(System.in);
        tarjeta = scanBanc.nextLine();
        while (tarjeta.length() > 20 && !esEntero(tarjeta)) {
            System.out.println("\n tarjeta no válida, intentelo de nuevo\n");
            scanBanc = new Scanner(System.in);
            tarjeta = scanBanc.nextLine();
        }

        sql = "insert into Cliente (nombre, apellidos, telefono, dni, domicilio, email, puntos, tarjeta) ";
        sql += "values ('";
        sql += nombreCliente;
        sql += "','";
        sql += apellidoCliente;
        sql += "','";
        sql += Telefono;
        sql += "','";
        sql += DNI;
        sql += "','";
        sql += Domicilio;
        sql += "','";
        sql += correo;
        sql += "','";
        sql += puntos;
        sql += "','";
        sql += tarjeta + "'" + ")";
        try {
            stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void darBajaCliente(Connection conn) {

        String sql;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("\nPor favor indique el DNI del cliente a eliminar: \n");
        String DNI;
        Scanner scanDNI = new Scanner(System.in);
        DNI = scanDNI.nextLine();
        while (DNI.length() > 20) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIEmp = new Scanner(System.in);
            DNI = scanDNIEmp.nextLine();
        }

        sql = "select * from Cliente where dni='" + DNI + "'";

        try {
            stmt.executeQuery(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("\nBorrando el cliente con DNI:" + rs.getString("DNI") + " " + " \n");
                sql = "delete from Cliente where DNI='" + DNI + "'";
                stmt.executeQuery(sql);
            } else {
                System.out.println("\nNo existe el Cliente\n");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: El cliente no existe o hay problemas en la conexión\n");
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void consultarCliente(Connection conn) {
        String sql;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("\nPor favor indique el DNI del cliente a consultar: \n");
        String DNI;
        Scanner scanDNI = new Scanner(System.in);
        DNI = scanDNI.nextLine();
        while (DNI.length() > 20) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIEmp = new Scanner(System.in);
            DNI = scanDNIEmp.nextLine();
        }

        sql = "select * from Cliente where dni='" + DNI + "'";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("\nNombre: " + rs.getString("nombre") + "\n");
                System.out.println("\nApellidos: " + rs.getString("npellidos") + "\n");
                System.out.println("\nTelefono: " + rs.getString("telefono") + "\n");
                System.out.println("\nDNI: " + rs.getString("dni") + "\n");
                System.out.println("\nDomicilio: " + rs.getString("domicilio") + "\n");
                System.out.println("\nCorreo: " + rs.getString("email") + "\n");
                System.out.println("\nPuntos: " + rs.getString("puntos") + "\n");
                System.out.println("\nRango: " + rs.getString("rango") + "\n");
                System.out.println("\nTarjeta: " + rs.getString("tarjeta") + "\n");
            } else {
                System.out.println("\nNo existe el Cliente\n");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: El cliente no existe o hay problemas en la conexión\n");
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void modificarCliente(Connection conn) {
        String sql;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("\nPor favor indique el DNI del cliente a modificar: \n");
        String DNI;
        Scanner scanDNI = new Scanner(System.in);
        DNI = scanDNI.nextLine();
        while (DNI.length() > 20) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIEmp = new Scanner(System.in);
            DNI = scanDNIEmp.nextLine();
        }

        sql = "select * from Cliente where dni='" + DNI + "'";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("\nNombre: " + rs.getString("nombre") + "\n");
                System.out.println("\nApellidos: " + rs.getString("apellidos") + "\n");
                System.out.println("\nTelefono: " + rs.getString("telefono") + "\n");
                System.out.println("\nDNI: " + rs.getString("dni") + "\n");
                System.out.println("\nDomicilio: " + rs.getString("domicilio") + "\n");
                System.out.println("\nCorreo: " + rs.getString("email") + "\n");
                System.out.println("\nPuntos: " + rs.getString("puntos") + "\n");
                System.out.println("\nRango: " + rs.getString("rango") + "\n");
                System.out.println("\nTarjeta: " + rs.getString("tarjeta") + "\n");
            } else {
                System.out.println("\nNo existe el Cliente\n");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: El cliente no existe o hay problemas en la conexión\n");
        }

        boolean errores = false;
        System.out.println("\nPor favor indique el NOMBRE del Cliente, si quiere: \n");
        String nombreCliente;
        Scanner scan4 = new Scanner(System.in);
        nombreCliente = scan4.nextLine();
        while (nombreCliente.length() > 20) {
            System.out.println("\nNombre no válido, intentelo de nuevo\n");
            Scanner scanNombre = new Scanner(System.in);
            nombreCliente = scanNombre.nextLine();
        }
        System.out.println("\nPor favor indique los APELLIDOS del Cliente, si quiere: \n");
        String apellidoCliente;
        Scanner scanEmpleado = new Scanner(System.in);
        apellidoCliente = scanEmpleado.nextLine();
        while (nombreCliente.length() > 40) {
            System.out.println("\nApellidos no válido, intentelo de nuevo\n");
            Scanner scanAp = new Scanner(System.in);
            apellidoCliente = scanAp.nextLine();
        }

        System.out.println("\nPor favor indique el TELÉFONO del Cliente, si quiere: \n");
        String Telefono;
        Scanner scanTelefono = new Scanner(System.in);
        Telefono = scanTelefono.nextLine();
        while (Telefono.length() > 20 || (!esEntero(Telefono) && Telefono.length() > 1)) {
            System.out.println("\nTelefono no válido, intentelo de nuevo\n");
            Scanner scanTel = new Scanner(System.in);
            Telefono = scanTel.nextLine();
        }

        System.out.println("\nPor favor indique el DNI del Cliente: \n");
        String DNIaux;
        Scanner scanDNIaux = new Scanner(System.in);
        DNIaux = scanDNIaux.nextLine();
        while (DNIaux.length() > 9 && DNIaux.length() < 1) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIe = new Scanner(System.in);
            DNIaux = scanDNIe.nextLine();
        }

        System.out.println("\nPor favor indique el DOMICILIO del cliente, si quiere: \n");
        String Domicilio;
        Scanner scanDomicilio = new Scanner(System.in);
        Domicilio = scanDomicilio.nextLine();
        while (Domicilio.length() > 60) {
            System.out.println("\nDOMICILIO no válido, intentelo de nuevo\n");
            scanDomicilio = new Scanner(System.in);
            Domicilio = scanDomicilio.nextLine();
        }

        System.out.println("\nPor favor indique el CORREO del cliente: \n");
        String correo;
        Scanner scancorreo = new Scanner(System.in);
        correo = scancorreo.nextLine();
        while (correo.length() > 20 || (!esUnica(stmt, correo) && Telefono.length() > 1)) {
            System.out.println("\nCorreo no válido o ya existente, intentelo de nuevo\n");
            scancorreo = new Scanner(System.in);
            correo = scancorreo.nextLine();
        }
        if (esUnica(stmt, correo)) {
            System.out.println("\nEs Unica\n");
        }

        System.out.println("\nPor favor indique los PUNTOS del cliente, si quiere: \n");
        String puntos;
        Scanner scanPuntos = new Scanner(System.in);
        puntos = scanPuntos.nextLine();
        while (!esEntero(puntos)) {
            System.out.println("\n Puntos no válidos, intentelo de nuevo\n");
            scanPuntos = new Scanner(System.in);
            puntos = scanPuntos.nextLine();
        }

        System.out.println("\nPor favor indique el Rango del cliente, si quiere: \n");
        String rango;
        Scanner scanRango = new Scanner(System.in);
        rango = scanRango.nextLine();
        while (rango.length() > 20) {
            System.out.println("\n Rango no válido, intentelo de nuevo\n");
            scanRango = new Scanner(System.in);
            rango = scanRango.nextLine();
        }

        System.out.println("\nPor favor indique la TARJETA del cliente, si quiere: \n");
        String tarjeta;
        Scanner scanBanc = new Scanner(System.in);
        tarjeta = scanBanc.nextLine();
        while (tarjeta.length() > 20 && !esEntero(tarjeta)) {
            System.out.println("\n tarjeta no válida, intentelo de nuevo\n");
            scanBanc = new Scanner(System.in);
            tarjeta = scanBanc.nextLine();
        }

        sql = "insert into Cliente (nombre, apellidos, telefono, dni, domicilio, email, puntos, tarjeta) ";
        sql += "values ('";
        sql += nombreCliente;
        sql += "','";
        sql += apellidoCliente;
        sql += "','";
        sql += Telefono;
        sql += "','";
        sql += DNI;
        sql += "','";
        sql += Domicilio;
        sql += "','";
        sql += correo;
        sql += "','";
        sql += puntos;
        sql += "','";
        sql += tarjeta + "'" + ")";
        try {
            stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
    }

    public static void consultarRangoCliente(Connection conn){
        String sql;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("\nPor favor indique el DNI del cliente a consultar: \n");
        String DNI;
        Scanner scanDNI = new Scanner(System.in);
        DNI = scanDNI.nextLine();
        while (DNI.length() > 20) {
            System.out.println("\nDNI no válido, intentelo de nuevo\n");
            Scanner scanDNIEmp = new Scanner(System.in);
            DNI = scanDNIEmp.nextLine();
        }

        sql = "select * from Cliente where dni='" + DNI + "'";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("\nRango: " + rs.getString("rango") + "\n");
            } else {
                System.out.println("\nNo existe el Cliente\n");
            }
        } catch (Exception e) {
            System.out.println("\nERROR: El cliente no existe o hay problemas en la conexión\n");
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


// //INSERTAR AQUI DATOS CLIENTE
// sql = "INSERT INTO cliente VALUES ('Rafael','Cordoba Lopez','684848493','28394823G','','Mesones 54','rafacorlopg@gmail.com', 0, 'Inicial')";
// stmt.executeQuery(sql);
// sql = "INSERT INTO cliente VALUES ('Nestor','Martinez Saez','764665788','78943659L','','Puentezuelas 12','nestormm@hotmail.es', 0, 'Inicial')";
// stmt.executeQuery(sql);
// sql = "INSERT INTO cliente VALUES ('Luis','Bonilla Perez','656874677','7852279D','','Camino de Ronda 133','luisbp@gmail.com', 0, 'Inicial')";
// stmt.executeQuery(sql);
// sql = "INSERT INTO cliente VALUES ('Marta','Ruiz Gomez','638572999','78482227Y','','Recogidas 42','martarg@gmail.com', 0, 'Inicial')";
// stmt.executeQuery(sql);
// sql = "INSERT INTO cliente VALUES ('Manuel','Fuertes Gonzalez','649837468','28846380R','','Arabial 23','manuelfg@hotmail.com', 0, 'Inicial')";
// stmt.executeQuery(sql);


// public void darAltaCliente(String DNI, String Nombre, String Apellidos, String Telefono, String Correo, int puntosCliente, String rango) {
    //     Cliente c = new Cliente(DNI, Nombre, Apellidos, Telefono, Correo, puntosCliente, rango);
    //     c.save();
    // }

    // public void darBajaCliente(String DNI) {
    //     Cliente c = Cliente.find(DNI);
    //     c.delete();
    // }

    // public void consultarCliente(String DNI) {
    //     Cliente c = Cliente.find(DNI);
    //     System.out.println(c);
    // }

    // public void modificarCliente(String DNI, String Nombre, String Apellidos, String Telefono, String Correo, int puntosCliente, String rango) {
    //     Cliente c = Cliente.find(DNI);
    //     c.setNombre(Nombre);
    //     c.setApellidos(Apellidos);
    //     c.setTelefono(Telefono);
    //     c.setCorreo(Correo);
    //     c.setPuntosCliente(puntosCliente);
    //     c.setRango(rango);
    //     c.save();
    // }

    // public void consultarRangoCliente(String DNI) {
    //     Cliente c = Cliente.find(DNI);
    //     System.out.println(c.getRango());
    // }
