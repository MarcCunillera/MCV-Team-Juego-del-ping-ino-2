package Controlador;

import java.util.Scanner;
import java.sql.*;

public class bbdd {
	
	public static Connection conectarBaseDatos() {
	    Connection con = null;
	    Scanner scan = new Scanner(System.in);

	    System.out.println("Intentando conectarse a la base de datos");
	    System.out.println("Selecciona centro o fuera de centro: (CENTRO/FUERA)");
	    String s = scan.nextLine().toLowerCase();

	    String URL = s.equals("centro")
	        ? "jdbc:oracle:thin:@192.168.3.26:1521/XEPDB2"
	        : "jdbc:oracle:thin:@oracle.ilerna.com:1521/XEPDB2";

	    while (con == null) {
	        //System.out.println("¿Usuario?");
	        //String USER = scan.nextLine();
	        String USER = "DW2425_PIN_GRUP07";
	        //System.out.println("¿Contraseña?");
	        //String PWD = scan.nextLine();
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

    public static int crearNuevaPartida(Connection con) {
        int idPartida = -1;
        try {
            // Agregamos un estado por defecto ("EN_CURSO") y null para los campos adicionales
            String sql = "INSERT INTO Partidas " +
                         "(ID_Partida, Num_Partida, Estado, Hora, Data, ID_Casilla, Nom_Casilla) " +
                         "VALUES (partidas_seq.NEXTVAL, partidas_seq.CURRVAL, 'EN_CURSO', CURRENT_TIMESTAMP, CURRENT_DATE, NULL, NULL)";
            insert(con, sql);

            ResultSet rs = select(con, "SELECT MAX(ID_Partida) AS ID FROM Partidas");
            if (rs.next()) {
                idPartida = rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idPartida;
    }


    public static void crearJugador(Connection con, String nombre, String contrasena) {
        String sql = "INSERT INTO Jugadores (ID_jugador, Nickname, Contrasena, N_partidas) " +
                     "VALUES (jugadores_seq.NEXTVAL, ?, ?, 0)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, contrasena);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

    public static void crearParticipacion(Connection con, int idPartida, int idJugador) {
    	String sql = "INSERT INTO Participaciones (ID_Participacion, ID_Partida, ID_jugador, Dado_Lento, Dado_Rapido, Peces, Bolas_Nieve) " +
    	             "VALUES (participaciones_seq.NEXTVAL, " + idPartida + ", " + idJugador + ", 0, 0, 0, 0)";

        insert(con, sql);
    }

    public static int obtenerIdPartida(Connection con, int numPartida) {
        int id = -1;
        try {
            ResultSet rs = select(con, "SELECT ID_Partida FROM Partidas WHERE Num_Partida = " + numPartida);
            if (rs.next()) {
                id = rs.getInt("ID_Partida");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static void actualizarParticipacion(Connection con, int idPartida, String nombre, int nuevaPosicion) {
        int idJugador = obtenerIdJugador(con, nombre);
        if (idJugador != -1) {
            String sql = "UPDATE Participaciones SET Peces = " + nuevaPosicion +
                         " WHERE ID_Partida = " + idPartida + " AND ID_jugador = " + idJugador;
            update(con, sql);
        }
    }

    // 👇 Asegúrate de que estas funciones están implementadas en tu clase (o añádelas si no están):
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
    
    
    //metodos para guardar tablero y jugadores (mrc)
    public static int insertarPartida(Connection con, String estado, Integer[] idCasillas) throws SQLException {
        String sql = "INSERT INTO Partidas (ID_Partida, Num_Partida, Estado, Hora, Data, ID_Casilla) " +
                     "VALUES (seq_partida.NEXTVAL, ?, ?, SYSTIMESTAMP, SYSDATE, ?)";

        Array array = con.createArrayOf("NUMBER", idCasillas); // Oracle específico: usa STRUCT/ARRAY si es VARRAY
        PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_Partida"});
        ps.setInt(1, generarNumeroPartida(con));
        ps.setString(2, estado);
        ps.setArray(3, array); // Oracle puede requerir tipo específico (ver nota abajo)
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            throw new SQLException("No se pudo generar ID_Partida");
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

    private static int generarNumeroPartida(Connection con) throws SQLException {
        // Método simple para contar partidas actuales + 1
        String sql = "SELECT NVL(MAX(Num_Partida), 0) + 1 AS nextNum FROM Partidas";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt("nextNum");
        return 1;
    }
}

