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
                         "VALUES (JUGADORES_SEQ.NEXTVAL, ?, ?, 0)";
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


    public static void insertarParticipacion(Connection con, int idPartida, ArrayList<Pinguino> pinguinos) throws SQLException {
        // Verificar que hay exactamente 4 pingüinos
        if (pinguinos.size() != 4) {
            throw new SQLException("Debe haber exactamente 4 pingüinos para insertar la participación");
        }

        String sql = "INSERT INTO DW2425_PIN_GRUP07_PARTICIPACIONES " +
                    "(ID_PARTICIPACION, ID_PARTIDA, ID_JUGADOR, " +
                    "JUGADOR_POS_1, JUGADOR_POS_2, JUGADOR_POS_3, JUGADOR_POS_4, " +
                    "DABO_LENTO_1, DABO_LENTO_2, DABO_LENTO_3, DABO_LENTO_4, " +
                    "DABO_RAPIDO_1, DABO_RAPIDO_2, DABO_RAPIDO_3, DABO_RAPIDO_4, " +
                    "PECES_1, PECES_2, PECES_3, PECES_4, " +  // Nota: No existe PECES_1
                    "BOLAS_NIEVE_1, BOLAS_NIEVE_2, BOLAS_NIEVE_3, BOLAS_NIEVE_4) " +
                    "VALUES (PARTICIPACIONES_SEQ.NEXTVAL, ?, ?, " +
                    "?, ?, ?, ?, " +    // 4 posiciones (total: 7)
                    "?, ?, ?, ?, " +    // 4 dados lentos (total: 11)
                    "?, ?, ?, ?, " +    // 4 dados rápidos (total: 15)
                    "?, ?, ?, " +       // 4 peces (total: 18)
                    "?, ?, ?, ?)";      // 4 bolas de nieve (total: 22)

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // Contador de parámetros
            int paramIndex = 1;
            
            // ID_PARTIDA (parámetro 1)
            ps.setInt(paramIndex++, idPartida);
            
            // ID_JUGADOR (parámetro 2)
            ps.setInt(paramIndex++, pinguinos.get(0).getID());
            
            // Posiciones (parámetros 3-6)
            for (int i = 0; i < 4; i++) {
                ps.setInt(paramIndex++, pinguinos.get(i).getPosicion());
            }
            
            // Dados lentos (parámetros 7-10)
            for (int i = 0; i < 4; i++) {
                ps.setInt(paramIndex++, pinguinos.get(i).getDadoLento());
            }
            
            // Dados rápidos (parámetros 11-14)
            for (int i = 0; i < 4; i++) {
                ps.setInt(paramIndex++, pinguinos.get(i).getDadoRapido());
            }
            
            // Peces (parámetros 15-17)
            for (int i = 0; i < 4; i++) { 
                ps.setInt(paramIndex++, pinguinos.get(i).getPescado());
            }
            
            // Bolas de nieve (parámetros 18-21)
            for (int i = 0; i < 4; i++) {
                ps.setInt(paramIndex++, pinguinos.get(i).getBolasNieve());
            }
            
            ps.executeUpdate();
        }
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
    		String sql = "INSERT INTO Participaciones (ID_Participacion, ID_Partida, ID_Jugador, Jugador_Pos_1, Dado_Lento_1, Dado_Rapido_1, Peces_1, Bolas_Nieve_1) " +
    				"VALUES (PARTICIPACIONES_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
    		pstmt = conn.prepareStatement(sql);
    		pstmt.setInt(1, idPartida);
    		pstmt.setInt(2, idJugador);
    		pstmt.setInt(3, posicion);
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
            Arrays.fill(casillas, "Normal");
            casillas[0] = "Normal"; // INICIO
            casillas[numCasillas - 1] = "Meta"; // META

            // Tipos de casillas especiales y sus límites
            Map<String, Integer> limites = new HashMap<>();
            limites.put("Agujero", 4);
            limites.put("Oso", 3);
            limites.put("Interrogante", 7);
            limites.put("Trineo", 4);

            // Generar posiciones aleatorias para las casillas especiales
            List<Integer> posiciones = new ArrayList<>();
            for (int i = 1; i < numCasillas - 1; i++) {
                posiciones.add(i);
            }
            Collections.shuffle(posiciones);
            int index = 0;

            for (Map.Entry<String, Integer> entry : limites.entrySet()) {
                String tipo = entry.getKey();
                int cantidad = entry.getValue();
                for (int i = 0; i < cantidad; i++) {
                    casillas[posiciones.get(index++)] = tipo;
                }
            }

            // Paso 1: Obtener el siguiente ID de la secuencia
            PreparedStatement seqStmt = con.prepareStatement("SELECT PARTIDAS_SEQ.NEXTVAL FROM dual");
            ResultSet seqRs = seqStmt.executeQuery();
            if (seqRs.next()) {
                idPartida = seqRs.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el valor NEXTVAL de PARTIDAS_SEQ");
            }
            seqRs.close();
            seqStmt.close();

            // Paso 2: Crear sentencia INSERT
            StringBuilder sql = new StringBuilder("INSERT INTO Partidas (ID_Partida, Num_Partida, Estado, Hora, Data");
            for (int i = 1; i <= numCasillas; i++) {
                sql.append(", ID_Casilla_").append(i);
            }
            sql.append(") VALUES (?, ?, 'EN_CURSO', CURRENT_TIMESTAMP, CURRENT_DATE");
            for (int i = 0; i < numCasillas; i++) {
                sql.append(", ?");
            }
            sql.append(")");

            // Paso 3: Preparar y ejecutar sentencia
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setInt(1, idPartida); // ID_Partida
            ps.setInt(2, idPartida); // Num_Partida (reutilizamos mismo ID)

            for (int i = 0; i < numCasillas; i++) {
                ps.setString(i + 3, casillas[i]); // Parámetros desde el índice 3
            }

            ps.executeUpdate();
            ps.close();

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
        String query = "SELECT ";

        for (int i = 1; i <= 50; i++) {
            query += "ID_Casilla_" + i;
            if (i < 50) query += ", ";
        }
        query += " FROM Partidas WHERE id_partida = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, idPartida);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (int i = 1; i <= 50; i++) {
                        String tipoStr = rs.getString("ID_Casilla_" + i);
                        TipoCasilla tipo = TipoCasilla.valueOf(tipoStr);  // Asumiendo que el String es un enum válido
                        Casilla casilla = new Casilla(i, tipo);  // El índice de la casilla es su número (1 a 50)
                        casillas.add(casilla);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return casillas;
    }
    //restaurar la lista de pinguinos
    public static List<Pinguino> obtenerPinguinosDePartida(Connection con, int idPartida) {
        List<Pinguino> pinguinos = new ArrayList<>();
        String query = "SELECT j.id_jugador, j.nickname, p.Jugador_pos, p.dado_Lento_1, p.dado_Rapido_1, p.peces_1, p.Bolas_nieve_1 FROM Jugadores j, Participaciones p WHERE j.id_jugador = p.id_jugador AND p.id_partida";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, idPartida);  // Establecemos el ID de la partida como parámetro
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_jugador");
                    String nombre = rs.getString("nickname");
                    int posicion = rs.getInt("Jugador_pos_1");
                    int dadoLento = rs.getInt("dado_Lento_1");
                    int dadoRapido = rs.getInt("dado_Rapido_1");
                    int pescado = rs.getInt("peces_1");
                    int bolasNieve = rs.getInt("Bolas_nieve_1");
                    
                    // Crear el objeto Pinguino con los valores obtenidos
                    Pinguino pinguino = new Pinguino(id, nombre, posicion, dadoLento, dadoRapido, bolasNieve, pescado);
                    pinguinos.add(pinguino);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return pinguinos;  // Devolvemos la lista de pingüinos
    }
    
    //métodos para guardar partida
    public static void actualizarPartida(Connection con, int idPartida, String estado, Integer[] casillas) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Partidas SET Estado = ?, ");

        // Actualizar los valores de las casillas
        for (int i = 1; i <= 50; i++) {
            sql.append("ID_Casilla_").append(i).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2);  // Eliminar la última coma
        sql.append(" WHERE ID_Partida = ?");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setString(1, estado);

        // Insertar los valores de las casillas
        for (int i = 0; i < 50; i++) {
            ps.setInt(2 + i, casillas[i]);
        }

        ps.setInt(52, idPartida);  // ID de la partida a actualizar
        ps.executeUpdate();
        ps.close();
    }

    public static void guardarOActualizarParticipacionUnica(Connection con, int idPartida, int idJugadorPrimerPinguino) throws SQLException {
        if (Pinguino.ListaPinguinos.size() != 4) {
            throw new IllegalStateException("Se necesitan exactamente 4 pingüinos en la lista para guardar la participación.");
        }

        // Comprobar si ya existe la participación con el idPartida e idJugadorPrimerPinguino
        String checkSql = "SELECT COUNT(*) FROM Participaciones WHERE ID_Partida = ? AND ID_Jugador = ?";
        try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
            checkStmt.setInt(1, idPartida);
            checkStmt.setInt(2, idJugadorPrimerPinguino);
            try (ResultSet rs = checkStmt.executeQuery()) {
                boolean existe = false;
                if (rs.next()) {
                    existe = rs.getInt(1) > 0;
                }

                if (existe) {
                    // UPDATE
                    String updateSql = "UPDATE Participaciones SET " +
                            "Jugador_Pos_1 = ?, Jugador_Pos_2 = ?, Jugador_Pos_3 = ?, Jugador_Pos_4 = ?, " +
                            "Dado_Lento_1 = ?, Dado_Lento_2 = ?, Dado_Lento_3 = ?, Dado_Lento_4 = ?, " +
                            "Dado_Rapido_1 = ?, Dado_Rapido_2 = ?, Dado_Rapido_3 = ?, Dado_Rapido_4 = ?, " +
                            "Peces_1 = ?, Peces_2 = ?, Peces_3 = ?, Peces_4 = ?, " +
                            "Bolas_Nieve_1 = ?, Bolas_Nieve_2 = ?, Bolas_Nieve_3 = ?, Bolas_Nieve_4 = ? " +
                            "WHERE ID_Partida = ? AND ID_Jugador = ?";

                    try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                        int i = 1;
                        // Insertamos los datos de los 4 pingüinos desde ListaPinguinos
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            updateStmt.setInt(i++, p.getPosicion());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            updateStmt.setInt(i++, p.getDadoLento());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            updateStmt.setInt(i++, p.getDadoRapido());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            updateStmt.setInt(i++, p.getPescado());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            updateStmt.setInt(i++, p.getBolasNieve());
                        }

                        updateStmt.setInt(i++, idPartida);
                        updateStmt.setInt(i++, idJugadorPrimerPinguino);

                        updateStmt.executeUpdate();
                    }

                } else {
                    // INSERT
                    // Generar nuevo ID_Participacion
                    int nuevoId;
                    try (Statement stmt = con.createStatement();
                         ResultSet rs2 = stmt.executeQuery("SELECT NVL(MAX(ID_Participacion), 0) + 1 FROM Participaciones")) {
                        rs2.next();
                        nuevoId = rs2.getInt(1);
                    }

                    String insertSql = "INSERT INTO Participaciones (" +
                            "ID_Participacion, ID_Partida, ID_Jugador, " +
                            "Jugador_Pos_1, Jugador_Pos_2, Jugador_Pos_3, Jugador_Pos_4, " +
                            "Dado_Lento_1, Dado_Lento_2, Dado_Lento_3, Dado_Lento_4, " +
                            "Dado_Rapido_1, Dado_Rapido_2, Dado_Rapido_3, Dado_Rapido_4, " +
                            "Peces_1, Peces_2, Peces_3, Peces_4, " +
                            "Bolas_Nieve_1, Bolas_Nieve_2, Bolas_Nieve_3, Bolas_Nieve_4) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                        int i = 1;
                        insertStmt.setInt(i++, nuevoId);
                        insertStmt.setInt(i++, idPartida);
                        insertStmt.setInt(i++, idJugadorPrimerPinguino);

                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            insertStmt.setInt(i++, p.getPosicion());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            insertStmt.setInt(i++, p.getDadoLento());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            insertStmt.setInt(i++, p.getDadoRapido());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            insertStmt.setInt(i++, p.getPescado());
                        }
                        for (Pinguino p : Pinguino.ListaPinguinos) {
                            insertStmt.setInt(i++, p.getBolasNieve());
                        }

                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }


}
