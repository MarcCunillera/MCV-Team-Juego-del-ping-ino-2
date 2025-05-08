package Vista;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import Controlador.*;
import Modelo.*;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

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
    
    Random rn = new Random();
    
    public enum TipoCasilla {
    	Normal,
    	Agujero,
    	Oso,
    	Trineo,
    	Interrogante,
    	Meta
    }
    
    private final int COLUMNS = 5; //Pruevas
    
    private static final int numCasillas = 50; //cadena constante
    private TipoCasilla[] tableroCasillas = new TipoCasilla[numCasillas]; //generar las casillas
    private IntegerProperty cantidadPeces = new SimpleIntegerProperty(0);
    private IntegerProperty cantidadNieve = new SimpleIntegerProperty(0);
    
    private int turno = 0;
    private ArrayList<Pinguino> pingus = new ArrayList<>();
    private Connection con;
    private int idPartida;
    
    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    @FXML
    private void initialize() {
        // This method is called automatically after the FXML is loaded
        // You can set initial values or add listeners here
        eventos.setText("¡El juego ha comenzado!");
        peces_t.textProperty().bind(Bindings.concat("Peces: ", cantidadPeces.asString()));
        nieve_t.textProperty().bind(Bindings.concat("Bolas de nieve: ", cantidadNieve.asString()));
        //añadir la lista de pinguinos
        //pingus = Pinguino.getListaPinguinos();
        pingus.add(new Pinguino(1, "Pinguino 1", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(2, "Pinguino 1", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(3, "Pinguino 1", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(4, "Pinguino 1", 0, 0, 0, 0, 0, 0));
        
        iniciarTablero();
    }
    
    //inicializar tablero
    private void iniciarTablero() {
    	Arrays.fill(tableroCasillas, TipoCasilla.Normal);
    	
    	//meter casillas especiales aleatorias
    	colocarCasillasEspeciales(TipoCasilla.Agujero, 4);
    	colocarCasillasEspeciales(TipoCasilla.Oso, 3);
    	colocarCasillasEspeciales(TipoCasilla.Interrogante, 7);
    	colocarCasillasEspeciales(TipoCasilla.Trineo, 4);
    	
    	//meter casilla inicio y fin (fijas)
    	tableroCasillas[0] = TipoCasilla.Normal;
    	tableroCasillas[49] = TipoCasilla.Meta;
    	
    	//imagenes
    	mostrarImagenesAgujero();
    	mostrarImagenesInterrogante();
    	mostrarImagenesOso();
    	mostrarImagenesTrineo();
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
    	
    	switch(casilla) {
    	//caso del oso
    	case Oso:
    		if (cantidadPeces.get() > 0) {
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
                        cantidadPeces.set(cantidadPeces.get() - 1);
                        eventos.setText("Has sobornado al oso con 1 pez.");
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
                if (tableroCasillas[i] == TipoCasilla.Agujero) { //si el tipo de casilla es agujero //revisar
                    encontradoA = true;
                    agujAnt = i;
                }
            }
            if (encontradoA) {
                eventos.setText(pingu.getID() + " cayó en un agujero 🕳 y retrocedió a la casilla " + agujAnt);
                pingu.setPosicion(agujAnt);
            } else {
                eventos.setText("El pinguino no se mueve de su posición");
            }
    		break;
    		//caso de interrogante
    	case Interrogante:
    		if(rn.nextBoolean()) {
    			if(cantidadNieve.get() >= 6) { //COMPROBAR QUE NO SUPERE EL MAXIMO DE BOLAS DE NIEVE
    				cantidadNieve.set(6);
    				eventos.setText("Ya tienes el maximo de Nieve possible " + cantidadNieve.get());
    			}else { //EN CASO DE QUE NO SUPERE EL LIMITE
    				int nieve = rn.nextInt(3) + 1;
        			cantidadNieve.set(cantidadNieve.get() + nieve);
        			eventos.setText("Has conseguido " + nieve + " Bolas de Nieve!!!");
    			}
    		}else {
    			if(cantidadPeces.get() >= 2 ) { //COMPROBAR QUE NO TENGA MAS DE 2 PECES
    				eventos.setText("Ya tienes el maximo de peces " + cantidadPeces.get());
    			}else { //EN CASO DE QUE TENGA MAS DE 2 PECES
    				cantidadPeces.set(cantidadPeces.get() + 1);
        			eventos.setText("Has conseguido 1 Pez!!!");
    			}
    		}
    		break;
    		//Caso trineo
    	case Trineo:
    		int trinPos = 0;
            boolean encontradoT = false;
            for (int i = pingu.getPosicion() + 1; i < tableroCasillas.length && !encontradoT; i++) {
                if (tableroCasillas[i] == TipoCasilla.Trineo) { //revisar
                    encontradoT = true;
                    trinPos = i;
                }
            }
            if (encontradoT) {
                eventos.setText(pingu.getID() + " usó un trineo 🛷 hasta la casilla " + trinPos);
                pingu.setPosicion(trinPos);
            } else {
                eventos.setText(pingu.getNombre() + " encontró un trineo 🛷 roto :(");
            }
            break;
            
            //en caso de caer a la casilla del final
    	case Meta:
    		Alert alert = new Alert(AlertType.INFORMATION);
    		alert.setTitle("El pinguino: " + pingu.getID() + " ha llegado a la meta!!");
    		alert.setHeaderText("El juego termina");
    		alert.showAndWait();
    		break;
    	case Normal:
    		eventos.setText("Casilla normal, todo tranquilo");
    		break;
    	}
    }
    
    //metodo para volver al inicio
    private void alInicio() {
    	Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
        
        GridPane.setRowIndex(pinguCircle, 0);
        GridPane.setColumnIndex(pinguCircle, 0);
        
        eventos.setText("Un oso te ha atrapado y vuelves al inicio :(");
        
        turno = (turno + 1) % pingus.size(); //Asegura que el turno vaya de uno en uno
    }

    // Button and menu actions

    @FXML
    private void handleNewGame() {
        System.out.println("Nueva Partida.");
        try {
            // Crea nueva partida
            idPartida = bbdd.crearNuevaPartida(con);
            eventos.setText("Nueva partida creada con ID: " + idPartida);

            // Crea una participación para cada jugador
            for (Pinguino pingu : pingus) {
                int idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                if (idJugador == -1) {
                    // Si no existe el jugador, lo crea
                    bbdd.crearJugador(con, pingu.getNombre(), "defaultPwd"); // Usa una mejor contraseña en producción
                    idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                }
                bbdd.crearParticipacion(con, idPartida, idJugador);
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventos.setText("Error al crear nueva partida.");
        }
    }

    @FXML
    private void handleSaveGame() {
        System.out.println("Guardando Partida.");
        
        // Verificar si hay una partida activa
        if (idPartida == -1) {
            mostrarAlerta("Error", "No hay partida activa para guardar", AlertType.ERROR);
            return;
        }

        Connection con = null;
        try {
            con = bbdd.conectarBaseDatos();
            if (con == null) {
                mostrarAlerta("Error", "No se pudo conectar con la base de datos", AlertType.ERROR);
                return;
            }

            // Deshabilitar auto-commit para manejar transacciones
            con.setAutoCommit(false);
            
            boolean error = false;
            
            for (Pinguino pingu : pingus) {
                if (pingu == null) {
                    System.err.println("¡Pinguino nulo detectado!");
                    continue;
                }

                try {
                    // Actualizar o crear participación para cada pingüino
                    if (!bbdd.actualizarParticipacion(con, idPartida, pingu.getNombre(), pingu.getPosicion())) {
                        error = true;
                        break;
                    }
                } catch (SQLException e) {
                    error = true;
                    System.err.println("Error al guardar datos del pingüino " + pingu.getNombre());
                    e.printStackTrace();
                    break;
                }
            }

            if (error) {
                con.rollback();
                mostrarAlerta("Error", "No se pudo guardar completamente la partida", AlertType.ERROR);
            } else {
                con.commit();
                mostrarAlerta("Éxito", "Partida guardada correctamente", AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error", "Error al guardar la partida: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar auto-commit
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadGame() {
        System.out.println("Cargar Partida.");
        
        // Mostrar diálogo para seleccionar partida
        Optional<Integer> resultado = mostrarDialogoCargarPartida();
        
        if (!resultado.isPresent()) {
            eventos.setText("Carga de partida cancelada.");
            return;
        }
        
        int numeroPartida = resultado.get();
        Connection con = null;
        
        try {
            con = bbdd.conectarBaseDatos();
            if (con == null) {
                mostrarAlerta("Error", "No se pudo conectar con la base de datos", AlertType.ERROR);
                return;
            }
            
            // Obtener ID de partida
            idPartida = bbdd.obtenerIdPartida(con, numeroPartida);
            if (idPartida == -1) {
                mostrarAlerta("Error", "No se encontró la partida con número: " + numeroPartida, AlertType.ERROR);
                return;
            }
            
            // Cargar datos de la partida
            Map<String, Integer> datosPartida = bbdd.cargarPartida(con, idPartida);
            if (datosPartida == null || datosPartida.isEmpty()) {
                mostrarAlerta("Error", "No se pudieron cargar los datos de la partida", AlertType.ERROR);
                return;
            }
            
            // Restaurar estado del juego
            boolean cargaExitosa = true;
            for (Pinguino pingu : pingus) {
                if (pingu == null) continue;
                
                Integer posicion = datosPartida.get(pingu.getNombre());
                if (posicion != null) {
                    pingu.setPosicion(posicion);
                    // Actualizar posición visual
                    actualizarPosicionVisual(pingu);
                } else {
                    cargaExitosa = false;
                    System.err.println("No se encontraron datos para: " + pingu.getNombre());
                }
            }
            
            if (cargaExitosa) {
                mostrarAlerta("Éxito", "Partida cargada correctamente", AlertType.INFORMATION);
                eventos.setText("Partida #" + numeroPartida + " cargada. Turno del jugador " + (turno + 1));
            } else {
                mostrarAlerta("Advertencia", "La partida se cargó parcialmente", AlertType.WARNING);
            }
            
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar la partida: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Optional<Integer> mostrarDialogoCargarPartida() {
        // Crear diálogo personalizado para mostrar lista de partidas disponibles
        List<Integer> partidasDisponibles = obtenerPartidasDisponibles();
        
        if (partidasDisponibles.isEmpty()) {
            mostrarAlerta("Información", "No hay partidas guardadas disponibles", AlertType.INFORMATION);
            return Optional.empty();
        }
        
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(partidasDisponibles.get(0), partidasDisponibles);
        dialog.setTitle("Cargar partida");
        dialog.setHeaderText("Seleccione una partida para cargar");
        dialog.setContentText("Partidas disponibles:");
        
        return dialog.showAndWait();
    }

    private List<Integer> obtenerPartidasDisponibles() {
        Connection con = null;
        List<Integer> partidas = new ArrayList<>();
        
        try {
            con = bbdd.conectarBaseDatos();
            if (con != null) {
                partidas = bbdd.obtenerPartidasGuardadas(con);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return partidas;
    }

    private void actualizarPosicionVisual(Pinguino pingu) {
        Platform.runLater(() -> {
            Circle pinguCircle = getPinguinCircle(pingus.indexOf(pingu));
            int row = pingu.getPosicion() / COLUMNS;
            int col = pingu.getPosicion() % COLUMNS;
            GridPane.setRowIndex(pinguCircle, row);
            GridPane.setColumnIndex(pinguCircle, col);
        });
    }
    
    private int obtenerNumeroPartidaDesdeInput() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cargar partida");
        dialog.setHeaderText("Carga de partida");
        dialog.setContentText("Introduce el número de partida:");

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
    private void handleQuitGame() {
        System.out.println("Saliendo del juego...");
        
        // Mostrar confirmación antes de salir
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar salida");
        alert.setHeaderText("¿Está seguro que desea salir del juego?");
        alert.setContentText("Se perderán los cambios no guardados.");
        
        ButtonType buttonTypeYes = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // Guardar estado actual antes de salir (opcional)
            try {
                handleSaveGame();
            } catch (Exception e) {
                System.err.println("Error al guardar al salir: " + e.getMessage());
            }
            
            // Cerrar la aplicación
            Platform.exit();
            System.exit(0);
        }
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
    
    //metodo para hacer update de la posicion del pinguino
    private void updatePenguinPosition() {
        Pinguino pingu = pingus.get(turno);
        Circle pinguCircle = getPinguinCircle(turno);
        
        int row = pingu.getPosicion() / 5; //5 X 10 grid
        int col = pingu.getPosicion() % 5;
        
        GridPane.setRowIndex(pinguCircle, row);
        GridPane.setColumnIndex(pinguCircle, col);
        
        int posicion = pingu.getPosicion();
        efectoCasilla(posicion);
        
        turno = (turno + 1) % pingus.size(); //Asegura que el turno vaya de uno en uno
    }

    @FXML
    private void handleDado(ActionEvent event) {
        Pinguino pinguActual = pingus.get(turno);
        int resulDado = pinguActual.tirarDadoNormal();
        
        dadoResultText.setText("Ha salido" + resulDado);
        
        //mover el pinguino
        if((pinguActual.getPosicion() + resulDado) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resulDado);
        }
        
        //Actualizar el tablero de forma visual
        updatePenguinPosition();
        int nuevaPosicion = pinguActual.getPosicion();
        
        if (nuevaPosicion >= 49) {
            eventos.setText("¡El pingüino " + pinguActual.getNombre() + " ha ganado!");
        }
        
    }

    @FXML
    private void handleRapido() {
        System.out.println("Fast.");
        // TODO
        Pinguino pinguActual = pingus.get(turno);
        
        //llamamos al metodo tirar dado rápido
        int resulRapido = pinguActual.tirarDadoRapido();
        
        //mostrar texto
        eventos.setText("Resultado dado Rapido" + resulRapido);
        
        //mover el pinguino
        if((pinguActual.getPosicion() + resulRapido) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resulRapido);
        }
        
        updatePenguinPosition();
    }

    @FXML
    private void handleLento() {
        System.out.println("Slow.");
        // TODO
        Pinguino pinguActual = pingus.get(turno);
        
        //llamar a la función para tirar dado lento
        int resulLento = pinguActual.tirarDadoLento();
        eventos.setText("Resultado dado Lento" + resulLento);
        
        //mover al pinguino
      //mover el pinguino
        if((pinguActual.getPosicion() + resulLento) > 49) { //si la posicion a cambiar es superior al limite del tablero
        	pinguActual.setPosicion(49);
        } else {
        	pinguActual.setPosicion(pinguActual.getPosicion() + resulLento);
        }
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
    
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    //////////////////////////// INSERTAR IMAGENES ///////////////////////////////////
    
    
    //imagen agujero
    private void mostrarImagenesAgujero() {
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
    
}
