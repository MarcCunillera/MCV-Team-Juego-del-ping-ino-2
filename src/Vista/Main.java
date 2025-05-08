package Vista;

import Modelo.*;
import Controlador.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

	    public static void main(String[] args) {
	        Scanner sc = new Scanner(System.in);
	        Connection con = bbdd.conectarBaseDatos(); // Asumo que tienes esta función implementada correctamente

	        System.out.println("Bienvenido al Juego del Pingüino 🐧");
	        System.out.println("¿Qué quieres hacer?");
	        System.out.println("1. Iniciar nueva partida");
	        System.out.println("2. Cargar partida existente");
	        System.out.println("3. Salir");

	        int opcion = sc.nextInt();
	        sc.nextLine();

	        if (opcion == 3) {
	            System.out.println("Saliendo del juego. ¡Hasta pronto!");
	            bbdd.cerrarConexion(con);
	            return;
	        }

	        Tablero tableroJuego = new Tablero(null); // Asumo que esto inicializa el tablero
	        ArrayList<Casilla> tablero = tableroJuego.getTablero();
	        Eventos evento = new Eventos(1, 1, 1, 1, 1);
	        ArrayList<Pinguino> listaPinguinos = new ArrayList<>();

	        int idPartida = -1;

	        if (opcion == 1) {
	            // Crear nueva partida
	        	idPartida = bbdd.crearNuevaPartida(con);
	            System.out.println("¿Cuántos pingüinos quieres crear? (min 2 - max 4)");
	            int numPinguinos = sc.nextInt();
	            while (numPinguinos < 2 || numPinguinos > 4) {
	                System.out.println("Número no válido. Intenta de nuevo:");
	                numPinguinos = sc.nextInt();
	            }
	            sc.nextLine();

	            for (int i = 0; i < numPinguinos; i++) {
	                System.out.println("Introduce el nombre del pingüino/jugador " + (i + 1) + ":");
	                String nombre = sc.nextLine();

	                System.out.println("Introduce una contraseña para " + nombre + " (máximo 8 caracteres):");
	                String contrasena = sc.nextLine();
	                while (contrasena.length() > 8) {
	                    System.out.println("La contraseña es demasiado larga, vuelve a escribirla:");
	                    contrasena = sc.nextLine();
	                }

	                bbdd.crearJugador(con, nombre, contrasena);
	                int idJugador = bbdd.obtenerIdJugador(con, nombre);
	                bbdd.crearParticipacion(con, idPartida, idJugador);

	                Pinguino pingu = new Pinguino(i, nombre, 0, 0, 0, 0, 0, 0);
	                listaPinguinos.add(pingu);
	            }

	        } else if (opcion == 2) {
	            // Cargar partida existente
	            System.out.println("Introduce el número de la partida que quieres cargar:");
	            int numPartida = sc.nextInt();
	            sc.nextLine();

	            idPartida = bbdd.obtenerIdPartida(con, numPartida);
	            if (idPartida == -1) {
	                System.out.println("Partida no encontrada. Saliendo...");
	                bbdd.cerrarConexion(con);
	                return;
	            }

	            try {
	                String sql = "SELECT j.Nickname, p.Peces FROM Participaciones p " +
	                             "JOIN Jugadores j ON p.ID_jugador = j.ID_jugador " +
	                             "WHERE p.ID_Partida = " + idPartida;
	                ResultSet rs = bbdd.select(con, sql);

	                while (rs.next()) {
	                    String nombre = rs.getString("Nickname");
	                    int peces = rs.getInt("Peces");

	                    Pinguino pingu = new Pinguino(0, nombre, 0, 0, 0, 0, 0, 0); //!
	                    pingu.setPosicion(peces);
	                    listaPinguinos.add(pingu);
	                }

	                if (listaPinguinos.isEmpty()) {
	                    System.out.println("No hay jugadores en esta partida.");
	                    bbdd.cerrarConexion(con);
	                    return;
	                }

	            } catch (SQLException e) {
	                System.out.println("Error cargando los jugadores.");
	                e.printStackTrace();
	                bbdd.cerrarConexion(con);
	                return;
	            }
	        } else {
	            System.out.println("Opción no válida. Saliendo...");
	            bbdd.cerrarConexion(con);
	            return;
	        }

	        System.out.println("\n--- ¡Comienza la partida! ---\n");

	        boolean partidaActiva = true;
	        while (partidaActiva) {
	            Pinguino pinguActual = listaPinguinos.get(tableroJuego.getTurno());

	            System.out.println("\nTurno de: " + pinguActual.getNombre() +
	                    " (Posición actual: " + pinguActual.getPosicion() + ")");
	            System.out.println("¿Qué dado quieres lanzar? (0 = normal, 3 = rápido, 4 = lento) o (-1 para salir y guardar): ");
	            int dadoSeleccionado = sc.nextInt();

	            if (dadoSeleccionado == -1) {
	                System.out.println("Guardando partida y saliendo...");
	                break;
	            }

	            if (listaPinguinos.isEmpty()) {
	                System.out.println("No hay pingüinos para mover.");
	                break;
	            } else {
	                tableroJuego.MoverPinguino(listaPinguinos, dadoSeleccionado);

	                Pinguino pinguDespues = listaPinguinos.get(
	                    tableroJuego.getTurno() == 0 ? listaPinguinos.size() - 1 : tableroJuego.getTurno() - 1
	                );
	                int nuevaPosicion = pinguDespues.getPosicion();

	                bbdd.actualizarParticipacion(con, idPartida, pinguDespues.getNombre(), nuevaPosicion);

	                if (nuevaPosicion >= 49) {
	                    System.out.println("\n🎉 ¡El pingüino " + pinguDespues.getNombre() + " ha ganado la partida! 🏆");
	                    partidaActiva = false;
	                }
	            }
	        }

	        bbdd.cerrarConexion(con);
	        System.out.println("Fin del juego. Gracias por jugar 🐧");
	    }
	}




