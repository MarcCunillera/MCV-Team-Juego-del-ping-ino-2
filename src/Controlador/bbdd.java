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

    public static void insertarPartida(Connection con, int idPartida, String estado, Integer[] casillas) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO Partidas (ID_Partida, Num_Partida, Estado, Hora, Data");

        // Añadir nombres de columnas de casillas
        for (int i = 1; i <= 50; i++) {
            sql.append(", ID_Casilla_").append(i);
        }
        sql.append(") VALUES (?, ?, ?, SYSTIMESTAMP, SYSDATE");

        // Añadir signos de interrogación para los valores de casillas
        for (int i = 1; i <= 50; i++) {
            sql.append(", ?");
        }
        sql.append(")");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setInt(1, idPartida);
        ps.setInt(2, idPartida); // Num_Partida igual que ID por simplicidad
        ps.setString(3, estado);

        // Insertar los IDs de las casillas
        for (int i = 0; i < 50; i++) {
            ps.setString(4 + i, casillas[i] != null ? casillas[i].toString() : null);
        }

        ps.executeUpdate();
        ps.close();
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

    
    public static int crearNuevaPartida(Connection con) {
        int idPartida = -1;
        final int numCasillas = 50;

        try {
            String[] casillas = new String[numCasillas];

            // Inicializamos con tipo Normal por defecto
            Arrays.fill(casillas, "Normal");

            // Posiciones especiales
            casillas[0] = "Normal"; // INICIO
            casillas[numCasillas - 1] = "Meta"; // META

            // Definimos los límites de cada tipo especial
            Map<String, Integer> limites = new HashMap<>();
            limites.put("Agujero", 4);
            limites.put("Oso", 3);
            limites.put("Interrogante", 7);
            limites.put("Trineo", 4);

            List<Integer> posiciones = new ArrayList<>();
            for (int i = 1; i < numCasillas - 1; i++) {
                posiciones.add(i); // Posiciones del 2 al 49 (índices 1 a 48)
            }

            Collections.shuffle(posiciones); // Aleatorizamos las posiciones

            Random rand = new Random();
            int index = 0;

            // Asignar los tipos limitados
            for (Map.Entry<String, Integer> entry : limites.entrySet()) {
                String tipo = entry.getKey();
                int cantidad = entry.getValue();
                for (int i = 0; i < cantidad; i++) {
                    casillas[posiciones.get(index++)] = tipo;
                }
            }

            // Armamos la sentencia SQL
            StringBuilder sql = new StringBuilder("INSERT INTO Partidas " +
                "(ID_Partida, Num_Partida, Estado, Hora, Data");

            for (int i = 1; i <= numCasillas; i++) {
                sql.append(", ID_Casilla_").append(i);
            }
            sql.append(") VALUES (PARTIDAS_SEQ.NEXTVAL, partidas_seq.CURRVAL, 'EN_CURSO', CURRENT_TIMESTAMP, CURRENT_DATE");

            for (int i = 0; i < numCasillas; i++) {
                sql.append(", ?");
            }
            sql.append(")");

            PreparedStatement ps = con.prepareStatement(sql.toString());

            for (int i = 0; i < numCasillas; i++) {
                ps.setString(i + 1, casillas[i]);
            }

            ps.executeUpdate();
            ps.close();

            // Recuperar el ID de la partida
            ResultSet rs = select(con, "SELECT MAX(ID_Partida) AS ID FROM Partidas");
            if (rs.next()) {
                idPartida = rs.getInt("ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
