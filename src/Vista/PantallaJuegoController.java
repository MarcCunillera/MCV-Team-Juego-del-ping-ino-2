package Vista;

import java.util.Random;

import javax.swing.SwingWorker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import Controlador.*;
import Modelo.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.text.Font;

public class PantallaJuegoController {
	
    // Menu items
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // Buttons
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // Texts
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private Text eventos;

    // Game board and player pieces
    @FXML private GridPane tablero;
    @FXML private Circle P1;
    @FXML private Circle P2;
    @FXML private Circle P3;
    @FXML private Circle P4;
    
    private Popup popup;
    
    // Llamar esto desde cualquier método para mostrar el mensaje
    public void mostrarPopup(String mensaje) {
        Platform.runLater(() -> {
            if (popup != null && popup.isShowing()) {
                popup.hide();
            }

            Label label = new Label(mensaje);
            label.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 10;");
            label.setFont(Font.font(20));
            StackPane content = new StackPane(label);
            content.setStyle("-fx-background-radius: 10;");
            popup = new Popup();
            popup.getContent().add(content);
            popup.setAutoHide(true);

            // Obtener la ventana desde cualquier nodo
            Window window = dado.getScene().getWindow(); // usa cualquier nodo del FXML con fx:id
            popup.show(window, window.getX() + 500, window.getY() + 600); // Ajusta posición si hace falta
        });
    }
    
    Random rn = new Random();
    
    public enum TipoCasilla {
    	Normal,
    	Agujero,
    	Oso, 
    	Trineo,
    	Interrogante,
    	Moto,
    	Quebradizo,
    	Meta
    }
    
    private final int COLUMNS = 5; //Pruevas
    
    private static final int numCasillas = 50; //cadena constante
    private TipoCasilla[] tableroCasillas = new TipoCasilla[numCasillas]; //generar las casillas
    private IntegerProperty cantidadPeces = new SimpleIntegerProperty();
    private IntegerProperty cantidadNieve = new SimpleIntegerProperty();
    private IntegerProperty cantidadLento = new SimpleIntegerProperty();
    private IntegerProperty cantidadRapido = new SimpleIntegerProperty();
    
    //metodo para tomar el id de casilla
    public Integer[] getCasillasId() {
        Integer[] ids = new Integer[tableroCasillas.length];
        
        // Recorremos el tablero y agregamos el índice de cada casilla
        for (int i = 0; i < tableroCasillas.length; i++) {
            // Dependiendo del tipo de casilla, agregamos su índice como ID
            // Si necesitas filtrar ciertos tipos, puedes hacerlo aquí
            if (tableroCasillas[i] != TipoCasilla.Normal) {
                ids[i] = i; // Usamos el índice como ID
            } else {
                ids[i] = null; // Si es "Normal" dejamos el valor como null (opcional)
            }
        }
        
        // Filtrar valores null (si es necesario)
        // Este paso es opcional si no deseas tener valores nulos en el array final
        List<Integer> idList = new ArrayList<>();
        for (Integer id : ids) {
            if (id != null) {
                idList.add(id);
            }
        }
        
        return idList.toArray(new Integer[0]); // Convertimos de vuelta a un array de Integer
    }

    
    private int turno = 0;
    private ArrayList<Pinguino> pingus = new ArrayList<>();
    private Connection con;
    private int idPartida;
    
    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    @FXML
    private void initialize() throws SQLException {
    
        con = bbdd.conectarBaseDatos(); // Aquí inicializas la conexión
        eventos.setText("¡El juego ha comenzado!");
        peces_t.textProperty().bind(Bindings.concat("Peces: ", cantidadPeces.asString()));
        nieve_t.textProperty().bind(Bindings.concat("Bolas de nieve: ", cantidadNieve.asString()));
        rapido_t.textProperty().bind(Bindings.concat("Dados rápidos: ", cantidadRapido.asString()));
        lento_t.textProperty().bind(Bindings.concat("Dados lentos: ", cantidadLento.asString()));
        
        //añadir la lista de pinguinos
        //pingus = Pinguino.getListaPinguinos();
        pingus.add(new Pinguino(1, "Azul", 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(2, "Rojo", 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(3, "Verde", 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(4, "Amarillo", 0, 0, 0, 0, 0));
        
        handleNewGame();
    }
    
    //inicializar tablero
    private void iniciarTablero() {
    	Arrays.fill(tableroCasillas, TipoCasilla.Normal);
    	
    	//meter casillas especiales aleatorias
    	colocarCasillasEspeciales(TipoCasilla.Agujero, 4);
    	colocarCasillasEspeciales(TipoCasilla.Oso, 3);
    	colocarCasillasEspeciales(TipoCasilla.Interrogante, 7);
    	colocarCasillasEspeciales(TipoCasilla.Trineo, 4);
    	colocarCasillasEspeciales(TipoCasilla.Quebradizo, 2);
    	colocarCasillasEspeciales(TipoCasilla.Moto, 1);
    	
    	//meter casilla inicio y fin (fijas)
    	tableroCasillas[0] = TipoCasilla.Normal;
    	tableroCasillas[49] = TipoCasilla.Meta;
    	
    	//vaciar imágenes anteriores (si hay)
    	eliminarImagenesEspeciales();
    	
    	//imagenes
    	mostrarImgAgujero();
    	mostrarImagenesInterrogante();
    	mostrarImagenesOso();
    	mostrarImagenesTrineo();
    	mostrarImagenesMoto();
    	mostrarImagenesQuebradizo();
    }
    
    //método para avanzar turno
    private void siguienteTurno() {
    	//mover turno
    	turno = (turno + 1) % pingus.size();
    	Pinguino pingu = pingus.get(turno);
    	actualizarInventario();
    	
    	//alerta
    	mostrarPopup("turno del pingüino: " + pingu.getNombre());
    }
    
    //método para actualizar el inventario
    private void actualizarInventario() {
    	Pinguino pingu = pingus.get(turno);
    	cantidadPeces.set(pingu.getPescado());
    	cantidadNieve.set(pingu.getBolasNieve());
    	cantidadLento.set(pingu.getDadoLento());
    	cantidadRapido.set(pingu.getDadoRapido());
    }
    
    //metodo para colocar las casillas especiales
    private void colocarCasillasEspeciales(TipoCasilla tipo, int cantidad) {
    	for (int i = 0; i < cantidad; i++) {
			int position;
			do {
				position = rn.nextInt(tableroCasillas.length -1) +1;
			} while (tableroCasillas[position] != TipoCasilla.Normal);
			
			tableroCasillas[position] = tipo;
			
		}
    }
    
    //metodo para aplicar los efectos de las casillas
    public void efectoCasilla(int position) {
    	TipoCasilla casilla = tableroCasillas[position]; //almacenar la posicion de la casilla
    	Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
    	int posicion = pingu.getPosicion();
        
    	switch(casilla) {
    	//caso del oso
    	case Oso:
    		if (cantidadPeces.get() >= 2) {
    			Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Alerta! oso a la vista");
                    alert.setHeaderText("quieres sobornar al oso?");
                    alert.setContentText("Peces para sobornarlo: " + cantidadPeces.get());
                    
                    ButtonType siELec = new ButtonType("Sí", ButtonBar.ButtonData.YES);
                    ButtonType noElec = new ButtonType("No", ButtonBar.ButtonData.NO);
                    alert.getButtonTypes().setAll(siELec, noElec);
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == siELec) {
                        cantidadPeces.set(cantidadPeces.get() - 2);
                        pingu.setPescado(pingu.getPescado() - 2);
                        eventos.setText("Has sobornado al oso con 2 pezes");
                    } else {
                        //volver al inicio
                    	alInicio();
                    }
                });
            } else {
                //volver al inicio
            	alInicio();
            }
            break;
			//caso del agujero
    	case Agujero:
    		int agujAnt = 0;
            boolean encontradoA = false;
            for (int i = pingu.getPosicion() - 1; i >= 0 && !encontradoA; i--) {
                if (tableroCasillas[i] == TipoCasilla.Agujero) { //si el tipo de casilla es agujero
                    encontradoA = true;
                    agujAnt = i;
                }
            }
            if (encontradoA) {
                eventos.setText(pingu.getID() + " cayó en un agujero 🕳 y retrocedió a la casilla " + agujAnt);
                pingu.setPosicion(agujAnt);
                finalUpdatePosition();
            } else {
                eventos.setText("El pinguino no se mueve de su posición");
            }
    		break;
    		//caso de interrogante
    	case Interrogante:
    		int elec = rn.nextInt(2) +1;
    		if (elec == 1) {
    		    if (rn.nextBoolean()) {
    		        // Bolas de Nieve
    		        if (cantidadNieve.get() >= 6) {
    		            cantidadNieve.set(6);
    		            pingu.setBolasNieve(6);
    		            eventos.setText("Ya tienes el máximo de Nieve posible " + cantidadNieve.get());
    		        } else {
    		            int nieve = rn.nextInt(3) + 1;
    		            cantidadNieve.set(cantidadNieve.get() + nieve);
    		            pingu.setBolasNieve(pingu.getBolasNieve() + nieve);
    		            eventos.setText("Has conseguido " + nieve + " Bolas de Nieve!!!");
    		        }
    		    } else {
    		        // Peces
    		        if (cantidadPeces.get() >= 5) {
    		            eventos.setText("Ya tienes el máximo de peces " + cantidadPeces.get());
    		        } else {
    		            cantidadPeces.set(cantidadPeces.get() + 1);
    		            pingu.setPescado(pingu.getPescado() + 1);
    		            eventos.setText("Has conseguido 1 Pez!!!");
    		        }
    		    }
    		} else {
    		    boolean dadoRapido = !rn.nextBoolean() && rn.nextInt(4) + 1 == 1;

    		    if (dadoRapido) {
    		        // Dado Rápido
    		        if (cantidadRapido.get() >= 4) {
    		            eventos.setText("Ya tienes el máximo de dados rápidos");
    		        } else {
    		            cantidadRapido.set(cantidadRapido.get() + 1);
    		            pingu.setDadoRapido(pingu.getDadoRapido() + 1);
    		            eventos.setText("Has conseguido 1 dado rápido!!!");
    		        }
    		    } else {
    		        // Dado Lento
    		        if (cantidadLento.get() >= 4) {
    		            cantidadLento.set(4);
    		            eventos.setText("Ya tienes el máximo de dados lentos " + cantidadLento.get());
    		        } else {
    		            cantidadLento.set(cantidadLento.get() + 1);
    		            pingu.setDadoLento(pingu.getDadoLento() + 1);
    		            eventos.setText("Has conseguido 1 dado lento");
    		        }
    		    }
    		}
    		
    		break;
    		//Caso trineo
    	case Trineo:
    		int siguienteTrineo = encontrarSiguienteTrineo(posicion);
    		
    		if(siguienteTrineo > posicion) {
    			pingu.setPosicion(siguienteTrineo);
    			finalUpdatePosition();
    			eventos.setText("Avanzas al siguiente Treino");
    		} else {
    			eventos.setText("Te encuentras en el último trineo");
    		}
    		break;
            
            //en caso de caer a la casilla del final
    	case Meta:
    		Alert alert = new Alert(AlertType.INFORMATION);
    		alert.setTitle("El pinguino: " + pingu.getNombre() + " ha llegado a la meta!!");
    		alert.setHeaderText("El juego termina");
    		alert.showAndWait();
    		
    		//deshabilitar botones
    		dado.setDisable(true);
    		rapido.setDisable(true);
    		lento.setDisable(true);
    		peces.setDisable(true);
    		nieve.setDisable(true);
    		break;
    	case Normal:
    		eventos.setText("Casilla normal, todo tranquilo");
    		break;
    	}
    	//saltar turno al acabar de verificar la casilla
    	siguienteTurno();
    }
    //método para encontrar al siguiente trieno
    private int encontrarSiguienteTrineo(int posActual) {
        boolean encontradoActual = false;

        for (int i = 0; i < tableroCasillas.length; i++) {
            if (tableroCasillas[i] == TipoCasilla.Trineo) {
                if (!encontradoActual && i == posActual) {
                    encontradoActual = true; // hemos encontrado el trineo actual
                } else if (encontradoActual) {
                    return i; // este es el siguiente trineo, nos detenemos aquí
                }
            }
        }
        return posActual;
    }
    
    //metodo para volver al inicio
    private void alInicio() {
    	Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
        
        pingu.setPosicion(0);
        GridPane.setRowIndex(pinguCircle, 0);
        GridPane.setColumnIndex(pinguCircle, 0);
        
        eventos.setText("Te ha atrapado un oso, al inicio ;(");
    }
    
    private void alInicioNew() {
    	turno = 0;
    	for (Pinguino pinguino : pingus) {
    		Circle pinguCircle = getPinguinCircle(turno);
    		turno++;
    		pinguino.setPosicion(0);
            GridPane.setRowIndex(pinguCircle, 0);
            GridPane.setColumnIndex(pinguCircle, 0);
		}
    	
    	turno = 0;
    	dado.setDisable(false);
		rapido.setDisable(false);
		lento.setDisable(false);
		peces.setDisable(false);
		nieve.setDisable(false);
    	
    }

    // Button and menu actions

    @FXML
    private void handleNewGame() {
        System.out.println("Nueva partida.");
        try {
            // Generar tablero y guardar partida en base de datos
            idPartida = bbdd.crearNuevaPartida(con); // ¡Aquí ya se genera el tablero aleatorio dentro!
            
            if (idPartida == -1) {
                eventos.setText("Error al crear la partida. Verifica la conexión o la base de datos.");
                return;
            }

            eventos.setText("Nueva partida creada con ID: " + idPartida);

            // Crear participación para cada jugador (pingüino)
            for (Pinguino pingu : pingus) {
                int idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());

                if (idJugador == -1) {
                    // Si no existe el jugador, lo crea
                    bbdd.crearJugador(con, pingu.getNombre(), "defaultPwd"); // Mejora esto en producción
                    idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                }

                // Obtener datos del pingüino
                int posicion = pingu.getPosicion(); // Posición inicial
                int dadoLento = pingu.getDadoLento();
                int dadoRapido = pingu.getDadoRapido();
                int peces = pingu.getPescado();
                int bolasNieve = pingu.getBolasNieve();

                // Crear la participación en la partida
                bbdd.crearParticipacion(con, idPartida, idJugador, posicion, dadoLento, dadoRapido, peces, bolasNieve);
                //iniciar tablero nuevo
                
                
            }

        } catch (Exception e) {
            e.printStackTrace();
            eventos.setText("Error al crear nueva partida.");
        }
        alInicioNew();
        iniciarTablero();
    }


    
    @FXML
    public void handleSaveGame() {
        // Mostrar alerta de "guardando" antes de comenzar
        Alert savingAlert = new Alert(AlertType.INFORMATION);
        savingAlert.setTitle("Guardando partida");
        savingAlert.setHeaderText(null);
        savingAlert.setContentText("Guardando el estado del juego...");
        savingAlert.show();

        // Ejecutar en segundo plano
        new Thread(() -> {
            try {
                // 1. Obtener estado actual del tablero
                Integer[] estadoCasillas = new Integer[50];
                for (int i = 0; i < tableroCasillas.length; i++) {
                    estadoCasillas[i] = tableroCasillas[i].ordinal();
                }

                // 2. Actualizar partida en BD
                bbdd.actualizarPartida(con, idPartida, "EN CURSO", estadoCasillas);

                // 3. Actualizar participaciones
                for (Pinguino pingu : pingus) {
                    int idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                    if (idJugador == -1) {
                        bbdd.crearJugador(con, pingu.getNombre(), "defaultPwd");
                        idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                    }
                    
                    bbdd.guardarOActualizarParticipacionUnica(
                        con, idPartida, idJugador
                    );
                }

                // Mostrar éxito en el hilo de JavaFX
                Platform.runLater(() -> {
                    savingAlert.close();
                    eventos.setText("Partida guardada correctamente (ID: " + idPartida + ")");
                    new Alert(AlertType.INFORMATION, "Juego guardado exitosamente!").show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    savingAlert.close();
                    new Alert(AlertType.ERROR, "Error al guardar: " + e.getMessage()).show();
                    eventos.setText("Error al guardar la partida");
                });
            }
        }).start();
    }
    
    //método para obtener el id de la partida actual
    public static int obtenerIdPartidaActual(Connection con) throws SQLException {
        String sql = "SELECT ID_Partida FROM Partidas WHERE Estado = 'EN CURSO' ORDER BY ID_Partida DESC FETCH FIRST 1 ROWS ONLY";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("ID_Partida");
            } else {
                throw new SQLException("No se encontró ninguna partida activa (EN CURSO).");
            }
        }
    }


    // Método para obtener el estado de las casillas (tablero)
    private Integer[] obtenerEstadoCasillas() {
        // Este método debe devolver el estado actual de las casillas del tablero
        Integer[] casillas = new Integer[50];
        // Ejemplo de asignación: Casilla 1 = 'INICIO', Casilla 50 = 'META', etc.
        // Esto debe ser ajustado según cómo manejas las casillas en el juego
        for (int i = 0; i < 50; i++) {
            casillas[i] = i + 1; //sponer la lógica de asignación real
        }
        return casillas;
    }
    
    //metodo para obtener el número de partida
    private int obtenerNumeroPartidaDesdeInput() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cargar partida");
        dialog.setHeaderText("Carga de partida");
        dialog.setContentText("Introduce el número de partida:");;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                return Integer.parseInt(result.get());
            } catch (NumberFormatException e) {
                eventos.setText("Número inválido. Usa solo dígitos.");
            }
        } else {
            eventos.setText("Carga cancelada.");
        }

        return -1;
    }
    
    @FXML
    public void handleLoadGame() {
        System.out.println("Loaded game.");
        int numeroPartida = obtenerNumeroPartidaDesdeInput();

        if (numeroPartida != -1) {
            try {
                idPartida = bbdd.obtenerIdPartida(con, numeroPartida);
                if (idPartida != -1) {
                    eventos.setText("Partida cargada con ID: " + idPartida);
                    
                    // Restaurar tablero y pingüinos
                    restaurarTablero();
                    restaurarPinguinos();
                    updateAllPenguinPosition();
                } else {
                    eventos.setText("No se encontró la partida con ese número.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                eventos.setText("Error al cargar la partida.");
            }
        }
    }


    private void restaurarTablero() throws SQLException {
        String query = "SELECT ID_CASILLA_1, ID_CASILLA_2, ID_CASILLA_3, ID_CASILLA_4, ID_CASILLA_5,\r\n"
        		+ "       ID_CASILLA_6, ID_CASILLA_7, ID_CASILLA_8, ID_CASILLA_9, ID_CASILLA_10,\r\n"
        		+ "       ID_CASILLA_11, ID_CASILLA_12, ID_CASILLA_13, ID_CASILLA_14, ID_CASILLA_15,\r\n"
        		+ "       ID_CASILLA_16, ID_CASILLA_17, ID_CASILLA_18, ID_CASILLA_19, ID_CASILLA_20,\r\n"
        		+ "       ID_CASILLA_21, ID_CASILLA_22, ID_CASILLA_23, ID_CASILLA_24, ID_CASILLA_25,\r\n"
        		+ "       ID_CASILLA_26, ID_CASILLA_27, ID_CASILLA_28, ID_CASILLA_29, ID_CASILLA_30,\r\n"
        		+ "       ID_CASILLA_31, ID_CASILLA_32, ID_CASILLA_33, ID_CASILLA_34, ID_CASILLA_35,\r\n"
        		+ "       ID_CASILLA_36, ID_CASILLA_37, ID_CASILLA_38, ID_CASILLA_39, ID_CASILLA_40,\r\n"
        		+ "       ID_CASILLA_41, ID_CASILLA_42, ID_CASILLA_43, ID_CASILLA_44, ID_CASILLA_45,\r\n"
        		+ "       ID_CASILLA_46, ID_CASILLA_47, ID_CASILLA_48, ID_CASILLA_49, ID_CASILLA_50\r\n"
        		+ "FROM PARTIDAS\r\n"
        		+ "WHERE ID_PARTIDA = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, idPartida);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            for (int i = 1; i <= 50; i++) {
                String columna = "ID_Casilla_" + i;
                String valor = rs.getString(columna);

                TipoCasilla tipo;

                try {
                    // Intenta interpretar el valor como índice
                    int index = Integer.parseInt(valor);
                    tipo = TipoCasilla.values()[index];
                } catch (NumberFormatException e) {
                    // Si falla, asume que es un nombre del enum
                    try {
                        tipo = TipoCasilla.valueOf(valor);
                    } catch (IllegalArgumentException ex) {
                        // Valor desconocido: puedes asignar uno por defecto o lanzar excepción
                        tipo = TipoCasilla.Normal; // por ejemplo
                        System.err.println("Valor desconocido para " + columna + ": " + valor);
                    }
                }

                tableroCasillas[i - 1] = tipo;
                eliminarImagenesEspeciales();
                mostrarImagenesInterrogante();
                mostrarImagenesOso();
                mostrarImagenesTrineo();
                mostrarImgAgujero();
                mostrarImagenesMoto();
                mostrarImagenesQuebradizo();
            }
        }

        rs.close();
        stmt.close();
    }





    public void restaurarPinguinos() {
        try {
            String query = "SELECT j.ID_Jugador, j.Nickname, " +
                    "p.Jugador_pos_1, p.Jugador_pos_2, p.Jugador_pos_3, p.Jugador_pos_4, " +
                    "p.dado_lento_1, p.dado_lento_2, p.dado_lento_3, p.dado_lento_4, " +
                    "p.dado_rapido_1, p.dado_rapido_2, p.dado_rapido_3, p.dado_rapido_4, " +
                    "p.peces_1, p.peces_2, p.peces_3, p.peces_4, " +
                    "p.bolas_nieve_1, p.bolas_nieve_2, p.bolas_nieve_3, p.bolas_nieve_4 " +
                    "FROM Participaciones p " +
                    "JOIN Jugadores j ON j.ID_Jugador = p.ID_Jugador " +
                    "WHERE p.ID_Partida = ?";

            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, idPartida);
                try (ResultSet rs = stmt.executeQuery()) {
                    pingus.clear(); // Limpia la lista actual antes de restaurar

                    if (rs.next()) {
                        pingus.add(new Pinguino(1, "Azul", rs.getInt("Jugador_pos_1"), rs.getInt("dado_lento_1"), rs.getInt("dado_rapido_1"), rs.getInt("peces_1"), rs.getInt("bolas_nieve_1")));
                        pingus.add(new Pinguino(2, "Rojo", rs.getInt("Jugador_pos_2"), rs.getInt("dado_lento_2"), rs.getInt("dado_rapido_2"), rs.getInt("peces_2"), rs.getInt("bolas_nieve_2")));
                        pingus.add(new Pinguino(3, "Verde", rs.getInt("Jugador_pos_3"), rs.getInt("dado_lento_3"), rs.getInt("dado_rapido_3"), rs.getInt("peces_3"), rs.getInt("bolas_nieve_3")));
                        pingus.add(new Pinguino(4, "Amarillo", rs.getInt("Jugador_pos_4"), rs.getInt("dado_lento_4"), rs.getInt("dado_rapido_4"), rs.getInt("peces_4"), rs.getInt("bolas_nieve_4")));
                    }
                }
            }

            eventos.setText("Pingüinos restaurados exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            eventos.setText("Error al restaurar los pingüinos.");
        }
    }


    
    public void actualizarRecursos() {
        try {
            // Consulta actualizada para obtener todos los recursos de los 4 pingüinos
            String query = "SELECT " +
                          "DABO_LENTO_1, DABO_LENTO_2, DABO_LENTO_3, DABO_LENTO_4, " +
                          "DABO_RAPIDO_1, DABO_RAPIDO_2, DABO_RAPIDO_3, DABO_RAPIDO_4, " +
                          "PECES_1, PECES_2, PECES_3, PECES_4, " +  // Nota: No existe PECES_1 en tu tabla
                          "BOLAS_NIEVE_1, BOLAS_NIEVE_2, BOLAS_NIEVE_3, BOLAS_NIEVE_4 " +
                          "FROM DW2425_PIN_GRUP07_PARTICIPACIONES " +
                          "WHERE ID_PARTIDA = ?";
            
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, idPartida);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Actualizar los recursos para cada pingüino en la lista
                        for (int i = 0; i < Pinguino.ListaPinguinos.size() && i < 4; i++) {
                            Pinguino p = Pinguino.ListaPinguinos.get(i);
                            
                            // Actualizar dados lentos
                            p.setDadoLento(rs.getInt("DABO_LENTO_" + (i+1)));
                            
                            // Actualizar dados rápidos
                            p.setDadoRapido(rs.getInt("DABO_RAPIDO_" + (i+1)));
                            
                            // Actualizar bolas de nieve
                            p.setBolasNieve(rs.getInt("BOLAS_NIEVE_" + (i+1)));
                            
                            // Actualizar peces
                            p.setPescado(rs.getInt("PECES_" + (i+1)));
                            
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            eventos.setText("Error al actualizar recursos: " + e.getMessage());
        }
    }



    @FXML
    private void handleQuitGame() {
        System.out.println("Exit...");
        Platform.exit(); // cierra aplicación JavaFX
        // Alternativamente: System.exit(0);
    }

    
    //método para elegir de forma visual la ficha a mover
    private Circle getPinguinCircle(int index) {
        switch (index) {
            case 0: return P1;
            case 1: return P2;
            case 2: return P3;
            case 3: return P4;
            default: return P1; // Valor por defecto por si ocurre algo inesperado
        }
    }
    
    //metodo para hacer update de la posicion del pinguino y comprobar el tipo de casilla
    private void updatePenguinPosition() {
        Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
        
        int row = pingu.getPosicion() / 5; //5 X 10 grid
        int col = pingu.getPosicion() % 5;
        
        GridPane.setRowIndex(pinguCircle, row);
        GridPane.setColumnIndex(pinguCircle, col);
        
        int posicion = pingu.getPosicion();
        efectoCasilla(posicion);
    }
    
    //update de todos los pinguinos del tablero al cargar juego
    private void updateAllPenguinPosition() {
        for (int i = 0; i < pingus.size(); i++) {
            Pinguino pingu = pingus.get(i);
            Circle pinguCircle = getPinguinCircle(i);

            int row = pingu.getPosicion() / 5; // 5 columnas
            int col = pingu.getPosicion() % 5;

            GridPane.setRowIndex(pinguCircle, row);
            GridPane.setColumnIndex(pinguCircle, col);
        }
    }

    
    
    //update final en caso de caer en trampas
    private void finalUpdatePosition() {
    	Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
        
        int row = pingu.getPosicion() / 5; //5 X 10 grid
        int col = pingu.getPosicion() % 5;
        
        GridPane.setRowIndex(pinguCircle, row);
        GridPane.setColumnIndex(pinguCircle, col);
    }

    @FXML
    private void handleDado(ActionEvent event) {
        Pinguino pinguActual = pingus.get(turno);
        int resulDado = pinguActual.tirarDadoNormal();
        
        dadoResultText.setText("Ha salido: " + resulDado);
        
        //mover el pinguino
        if((pinguActual.getPosicion() + resulDado) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resulDado);
        }
        
        //Actualizar el tablero de forma visual
        updatePenguinPosition();
    }

    @FXML
    private void handleRapido() {
        System.out.println("Fast.");
        // TODO
        Pinguino pinguActual = pingus.get(turno);
        int resul;
        
        if(cantidadLento.get() == 0) {
        	resul = pinguActual.tirarDadoNormal();
        	dadoResultText.setText("Resultado dado Normal: " + resul);
        } else {
            //llamar a la función para tirar dado lento
            resul = pinguActual.tirarDadoRapido();
            dadoResultText.setText("Resultado dado Rápido: " + resul);
            pinguActual.setDadoRapido(pinguActual.getDadoRapido()-1);
        }
        //mover el pinguino
        if((pinguActual.getPosicion() + resul) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resul);
        }
        updatePenguinPosition();
    }

    @FXML
    private void handleLento() {
        System.out.println("Slow.");
        // TODO
        Pinguino pinguActual = pingus.get(turno);
        int resul;
        
        if(cantidadLento.get() == 0) {
        	resul = pinguActual.tirarDadoNormal();
        	dadoResultText.setText("Resultado dado Normal: " + resul);
        } else {
            //llamar a la función para tirar dado lento
            resul = pinguActual.tirarDadoLento();
            pinguActual.setDadoLento(pinguActual.getDadoLento() -1);
            dadoResultText.setText("Resultado dado Lento: " + resul);
        }
        //mover el pinguino
        if((pinguActual.getPosicion() + resul) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resul);
        }
        updatePenguinPosition();
    }	

    @FXML
    private void handlePeces() {
        System.out.println("Fish.");
        // TODO
    }

    @FXML
    private void handleNieve() {
        System.out.println("Snow.");
        // TODO
    }
    
    //////////////////////////// INSERTAR IMAGENES ///////////////////////////////////
    
    
    //imagen agujero
    private void mostrarImgAgujero() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Agujero) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/agujero.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //imagen trineo
    private void mostrarImagenesTrineo() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Trineo) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/trineo.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //imagen interrognate
    private void mostrarImagenesInterrogante() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Interrogante) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/interrogante.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //imagen oso
    private void mostrarImagenesOso() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Oso) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/oso.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //imagen moto
    private void mostrarImagenesMoto() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Oso) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/moto.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //imagen suelo quebradizo
    private void mostrarImagenesQuebradizo() {
    	for(int i = 0; i < tableroCasillas.length; i++) {
    		if(tableroCasillas[i] == TipoCasilla.Oso) {
    			int row = i / COLUMNS;
    			int col = i % COLUMNS;
    			
    			//añadir las imagenes
    			Image image = new Image(getClass().getResource("/Resources/quebradizo.png").toExternalForm());
    			ImageView imageView = new ImageView(image);
    			imageView.setFitWidth(40);
    			imageView.setFitHeight(40);
    			imageView.setPreserveRatio(true);
    			tablero.add(imageView, col, row);
    		}
    	}
    }
    
    //borrar las imagenes residuales
    private void eliminarImagenesEspeciales() {
        tablero.getChildren().removeIf(node -> {
            if (node instanceof ImageView) {
                ImageView imgView = (ImageView) node;
                String url = imgView.getImage().getUrl();
                return url != null && (
                    url.contains("agujero.png") ||
                    url.contains("trineo.png") ||
                    url.contains("interrogante.png") ||
                    url.contains("oso.png") ||
                    url.contains("moto.png") ||
                    url.contains("quebradizo.png")
                );
            }
            return false;
        });
    }
    
}
