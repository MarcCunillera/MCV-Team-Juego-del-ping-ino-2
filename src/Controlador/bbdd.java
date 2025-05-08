package Controlador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class bbdd {
    private static final Logger logger = Logger.getLogger(BBDD.class.getName());
    
    public static Connection conectarBaseDatos() {
        Connection con = null;
        Scanner scan = new Scanner(System.in);

        try {
            System.out.println("Intentando conectarse a la base de datos");
            System.out.println("Selecciona centro o fuera de centro: (CENTRO/FUERA)");
            String s = scan.nextLine().toLowerCase();

            String URL = s.equals("centro")
                ? "jdbc:oracle:thin:@192.168.3.26:1521/XEPDB2"
                : "jdbc:oracle:thin:@oracle.ilerna.com:1521/XEPDB2";

            // Credenciales hardcodeadas (considera usar configuración externa)
            String USER = "DW2425_PIN_GRUP07";
            String PWD = "ACMV007";

            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(URL, USER, PWD);
            
            if (con != null) {
                System.out.println("Conectado a la base de datos.");
            }
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error al cargar el driver JDBC", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexión a la base de datos", e);
        }

        return con;
    }

    public static void cerrarConexion(Connection con) {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                    System.out.println("Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error al cerrar la conexión", e);
            }
        }
    }

    public static int crearNuevaPartida(Connection con) throws SQLException {
        if (con == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }

        int idPartida = -1;
        boolean autoCommit = con.getAutoCommit();
        
        try {
            con.setAutoCommit(false);
            
            // Usamos PreparedStatement para evitar inyección SQL
            String sql = "INSERT INTO Partidas " +
                         "(ID_Partida, Num_Partida, Estado, Hora, Data, ID_Casilla, Nom_Casilla) " +
                         "VALUES (partidas_seq.NEXTVAL, partidas_seq.CURRVAL, 'EN_CURSO', CURRENT_TIMESTAMP, CURRENT_DATE, NULL, NULL)";
            
            try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID_Partida"})) {
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPartida = rs.getInt(1);
                    }
                }
            }
            
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            logger.log(Level.SEVERE, "Error al crear nueva partida", e);
            throw e;
        } finally {
            con.setAutoCommit(autoCommit);
        }
        
        return idPartida;
    }

    public static boolean crearJugador(Connection con, String nombre, String contrasena) throws SQLException {
        if (con == null || nombre == null || contrasena == null) {
            throw new IllegalArgumentException("Parámetros no pueden ser nulos");
        }

        String sql = "INSERT INTO Jugadores (ID_jugador, Nickname, Contrasena, N_partidas) " +
                     "VALUES (jugadores_seq.NEXTVAL, ?, ?, 0)";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, contrasena);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static int obtenerIdJugador(Connection con, String nombre) throws SQLException {
        if (con == null || nombre == null) {
            throw new IllegalArgumentException("Parámetros no pueden ser nulos");
        }

        String sql = "SELECT ID_jugador FROM Jugadores WHERE Nickname = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("ID_jugador") : -1;
            }
        }
    }

    public static boolean crearParticipacion(Connection con, int idPartida, int idJugador) throws SQLException {
        if (con == null || idPartida <= 0 || idJugador <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }

        String sql = "INSERT INTO Participaciones " +
                     "(ID_Participacion, ID_Partida, ID_jugador, Dado_Lento, Dado_Rapido, Peces, Bolas_Nieve) " +
                     "VALUES (participaciones_seq.NEXTVAL, ?, ?, 0, 0, 0, 0)";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, idPartida);
            pstmt.setInt(2, idJugador);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static int obtenerIdPartida(Connection con, int numPartida) throws SQLException {
        if (con == null || numPartida <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }

        String sql = "SELECT ID_Partida FROM Partidas WHERE Num_Partida = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, numPartida);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("ID_Partida") : -1;
            }
        }
    }

    public static boolean actualizarParticipacion(Connection con, int idPartida, String nombre, int nuevaPosicion) throws SQLException {
        if (con == null || nombre == null || idPartida <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }

        int idJugador = obtenerIdJugador(con, nombre);
        if (idJugador == -1) {
            return false;
        }

        String sql = "UPDATE Participaciones SET Jugador_Pos = ? " +
                     "WHERE ID_Partida = ? AND ID_jugador = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, nuevaPosicion);
            pstmt.setInt(2, idPartida);
            pstmt.setInt(3, idJugador);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Métodos mejorados para guardar el estado del juego
    public static int guardarEstadoJuego(Connection con, String estado, List<Integer> idCasillas, 
                                       List<JugadorEstado> jugadores) throws SQLException {
        if (con == null || estado == null || idCasillas == null || jugadores == null) {
            throw new IllegalArgumentException("Parámetros no pueden ser nulos");
        }

        boolean autoCommit = con.getAutoCommit();
        int idPartida = -1;
        
        try {
            con.setAutoCommit(false);
            
            // Crear nueva partida
            idPartida = crearNuevaPartida(con);
            if (idPartida == -1) {
                throw new SQLException("No se pudo crear la partida");
            }

            // Guardar estado de los jugadores
            for (JugadorEstado jugador : jugadores) {
                int idJugador = obtenerIdJugador(con, jugador.getNombre());
                if (idJugador == -1) {
                    crearJugador(con, jugador.getNombre(), "tempPassword");
                    idJugador = obtenerIdJugador(con, jugador.getNombre());
                }

                crearParticipacion(con, idPartida, idJugador);
                actualizarEstadoJugador(con, idPartida, idJugador, jugador);
            }
            
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            logger.log(Level.SEVERE, "Error al guardar estado del juego", e);
            throw e;
        } finally {
            con.setAutoCommit(autoCommit);
        }
        
        return idPartida;
    }

    private static boolean actualizarEstadoJugador(Connection con, int idPartida, int idJugador, 
                                                 JugadorEstado jugador) throws SQLException {
        String sql = "UPDATE Participaciones SET " +
                     "Jugador_Pos = ?, Dado_Lento = ?, Dado_Rapido = ?, Peces = ?, Bolas_Nieve = ? " +
                     "WHERE ID_Partida = ? AND ID_jugador = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, jugador.getPosicion());
            pstmt.setInt(2, jugador.getDadoLento());
            pstmt.setInt(3, jugador.getDadoRapido());
            pstmt.setInt(4, jugador.getPeces());
            pstmt.setInt(5, jugador.getBolasNieve());
            pstmt.setInt(6, idPartida);
            pstmt.setInt(7, idJugador);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Clase auxiliar para representar el estado de un jugador
    public static class JugadorEstado {
        private String nombre;
        private int posicion;
        private int dadoLento;
        private int dadoRapido;
        private int peces;
        private int bolasNieve;

        // Constructor, getters y setters
        // ...
    }
}