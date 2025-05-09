package Controlador;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import Modelo.Pinguino;
import Vista.PantallaJuegoController.TipoCasilla;

public class bbdd {

    public static Connection conectarBaseDatos() {
        Connection con = null;
        Scanner scan = new Scanner(System.in);

        System.out.println("Intentando conectarse a la base de datos");
        System.out.println("Selecciona centro o fuera de centro: (CENTRO/FUERA)");
        //String s = scan.nextLine().toLowerCase();
        String s = "fuera";
        String URL = s.equals("centro")
                ? "jdbc:oracle:thin:@192.168.3.26:1521/XEPDB2"
                : "jdbc:oracle:thin:@oracle.ilerna.com:1521/XEPDB2";

        while (con == null) {
            String USER = "DW2425_PIN_GRUP07";
            String PWD = "ACMV007";
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                con = DriverManager.getConnection(URL, USER, PWD);
            } catch (SQLException e) {
                System.out.println("Usuario o contraseña incorrectos. Inténtalo de nuevo.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error al cargar el driver JDBC.");
                break;
            }
        }

        if (con != null) {
            System.out.println("Conectado a la base de datos.");
        }

        return con;
    }

    public static void cerrarConexion(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int generarNumeroPartida(Connection con) throws SQLException {
        String sql = "SELECT NVL(MAX(Num_Partida), 0) + 1 AS nextNum FROM Partidas";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt("nextNum");
        return 1;
    }

    public static void insertarPartida(Connection con, int idPartida, String estado, String[] casillas) throws SQLException {
        String sql = "INSERT INTO Partidas (ID_Partida, Num_Partida, Estado, Hora, Data, " +
                     "ID_Casilla_1, ID_Casilla_2, ID_Casilla_3, ID_Casilla_4, ID_Casilla_5, ID_Casilla_6, " +
                     "ID_Casilla_7, ID_Casilla_8, ID_Casilla_9, ID_Casilla_10, ID_Casilla_11, ID_Casilla_12, " +
                     "ID_Casilla_13, ID_Casilla_14, ID_Casilla_15, ID_Casilla_16, ID_Casilla_17, ID_Casilla_18, " +
                     "ID_Casilla_19, ID_Casilla_20, ID_Casilla_21, ID_Casilla_22, ID_Casilla_23, ID_Casilla_24, " +
                     "ID_Casilla_25, ID_Casilla_26, ID_Casilla_27, ID_Casilla_28, ID_Casilla_29, ID_Casilla_30, " +
                     "ID_Casilla_31, ID_Casilla_32, ID_Casilla_33, ID_Casilla_34, ID_Casilla_35, ID_Casilla_36, " +
                     "ID_Casilla_37, ID_Casilla_38, ID_Casilla_39, ID_Casilla_40, ID_Casilla_41, ID_Casilla_42, " +
                     "ID_Casilla_43, ID_Casilla_44, ID_Casilla_45, ID_Casilla_46, ID_Casilla_47, ID_Casilla_48, " +
                     "ID_Casilla_49, ID_Casilla_50) " +
                     "VALUES (?, ?, ?, SYSTIMESTAMP, SYSDATE, " +
                     "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";  // 50 placeholders

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPartida);
            ps.setInt(2, idPartida); // o usa un número de partida independiente
            ps.setString(3, estado);

            for (int i = 0; i < 50; i++) {
                ps.setString(4 + i, casillas[i]);  // Empieza en el índice 4
            }

            ps.executeUpdate();
        }
    }

    public static int obtenerIdJugador(Connection con, String nombre) {
        int id = -1;
        try {
            ResultSet rs = select(con, "SELECT ID_jugador FROM Jugadores WHERE Nickname = '" + nombre + "'");
            if (rs.next()) {
                id = rs.getInt("ID_jugador");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static void crearJugador(Connection conn, String nickname, String contrasena) {
        PreparedStatement pstmt = null;
        
        contrasena = "12345";
        if (contrasena.length() > 8) {
            System.err.println("Error: La contraseña excede los 8 caracteres permitidos.");
            return;
        }

        try {
            String sql = "INSERT INTO Jugadores (ID_jugador, Nickname, Contrasena, N_partidas) " +
                         "VALUES (jugadores_seq.NEXTVAL, ?, ?, 0)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            pstmt.setString(2, contrasena);
            pstmt.executeUpdate();
            System.out.println("Jugador creado correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al crear jugador: " + e.getMessage());
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { /* ignored */ }
        }
    }


    public static void insertarParticipacion(Connection con, int idPartida, int idJugador, int posicion, int dadoLento, int dadoRapido, int peces, int bolasNieve) throws SQLException {
        String sql = "INSERT INTO Participaciones (ID_Participacion, ID_Partida, ID_Jugador, Jugador_Pos, Dado_Lento, Dado_Rapido, Peces, Bolas_Nieve) " +
                     "VALUES (seq_participacion.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idPartida);
        ps.setInt(2, idJugador);
        ps.setInt(3, posicion);
        ps.setInt(4, dadoLento);
        ps.setInt(5, dadoRapido);
        ps.setInt(6, peces);
        ps.setInt(7, bolasNieve);
        ps.executeUpdate();
    }

    public static void actualizarParticipacion(Connection con, int idPartida, String nombre, int nuevaPosicion) {
        int idJugador = obtenerIdJugador(con, nombre);
        if (idJugador != -1) {
            String sql = "UPDATE Participaciones SET Peces = " + nuevaPosicion +
                         " WHERE ID_Partida = " + idPartida + " AND ID_jugador = " + idJugador;
            update(con, sql);
        }
    }

    public static void insert(Connection con, String sql) {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(Connection con, String sql) {
        insert(con, sql); // misma implementación que insert
    }

    public static ResultSet select(Connection con, String sql) throws SQLException {
        Statement stmt = con.createStatement();
        return stmt.executeQuery(sql);
    }
    
    public static void crearParticipacion(Connection conn, int idPartida, int idJugador, int posicion, int dadoLento, int dadoRapido, int peces, int bolasNieve) {
    	PreparedStatement pstmt = null;

    	try {
    		String sql = "INSERT INTO Participaciones (ID_Participacion, ID_Partida, ID_Jugador, Jugador_Pos, Dado_Lento, Dado_Rapido, Peces, Bolas_Nieve) " +
    				"VALUES (PARTICIPACION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setInt(1, idPartida);
    		pstmt.setInt(2, idJugador);
    		pstmt.setInt(3, posicion);
    		pstmt.setInt(4, dadoLento);
    		pstmt.setInt(5, dadoRapido);
    		pstmt.setInt(6, peces);
    		pstmt.setInt(7, bolasNieve);
    		pstmt.executeUpdate();
    		System.out.println("Participación creada correctamente.");
    	} catch (SQLException e) {
    		System.err.println("Error al crear participación: " + e.getMessage());
    	} finally {
    		try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { /* ignored */ }
    	}
    }

    
    public static int crearNuevaPartida(Connection con) throws SQLException {
        int idPartida = -1;

        // Obtener el siguiente valor de la secuencia sin insertar aún
        ResultSet rs = con.createStatement().executeQuery("SELECT partidas_seq.NEXTVAL FROM DUAL");
        if (rs.next()) {
            idPartida = rs.getInt(1);
        }

        return idPartida;
    }


    public static int obtenerIdPartida(Connection con, int numPartida) {
        int idPartida = -1;
        try {
            // Ejecutamos la consulta para obtener el ID de la partida con el número dado
            String sql = "SELECT ID_Partida FROM Partidas WHERE Num_Partida = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numPartida); // Establecemos el número de partida como parámetro
            ResultSet rs = ps.executeQuery();

            // Si encontramos la partida, devolvemos el ID
            if (rs.next()) {
                idPartida = rs.getInt("ID_Partida");
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idPartida;
    }
    
    public List<Casilla> obtenerCasillasDePartida(Connection con, int idPartida) {
        List<Casilla> casillas = new ArrayList<>();
        String query = "SELECT posicion, tipo FROM casillas WHERE id_partida = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, idPartida);  // Establecemos el ID de la partida como parámetro
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int posicion = rs.getInt("posicion");
                    String tipoStr = rs.getString("tipo");
                    TipoCasilla tipo = TipoCasilla.valueOf(tipoStr);  // Convertimos el tipo de String a TipoCasilla
                    
                    // Crear el objeto Casilla y agregarlo a la lista
                    Casilla casilla = new Casilla(posicion, tipo);
                    casillas.add(casilla);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return casillas;  // Devolvemos la lista de casillas
    }

    
    public static List<Pinguino> obtenerPinguinosDePartida(Connection con, int idPartida) {
        List<Pinguino> pinguinos = new ArrayList<>();
        String query = "SELECT id, nombre, posicion, dadoNormal, dadoLento, dadoRapido, bolasNieve, pescado FROM pinguinos WHERE id_partida = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, idPartida);  // Establecemos el ID de la partida como parámetro
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    int posicion = rs.getInt("posicion");
                    int dadoNormal = rs.getInt("dadoNormal");
                    int dadoLento = rs.getInt("dadoLento");
                    int dadoRapido = rs.getInt("dadoRapido");
                    int bolasNieve = rs.getInt("bolasNieve");
                    int pescado = rs.getInt("pescado");
                    
                    // Crear el objeto Pinguino con los valores obtenidos
                    Pinguino pinguino = new Pinguino(id, nombre, posicion, dadoNormal, dadoLento, dadoRapido, bolasNieve, pescado);
                    pinguinos.add(pinguino);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return pinguinos;  // Devolvemos la lista de pingüinos
    }
    
}
