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
    private IntegerProperty cantidadPeces = new SimpleIntegerProperty();
    private IntegerProperty cantidadNieve = new SimpleIntegerProperty();
    
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
        //añadir la lista de pinguinos
        //pingus = Pinguino.getListaPinguinos();
        pingus.add(new Pinguino(1, "Toñin", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(2, "Juan", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(3, "Ruperto", 0, 0, 0, 0, 0, 0));
        pingus.add(new Pinguino(4, "Alfred", 0, 0, 0, 0, 0, 0));
        
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
    
    //método para avanzar turno
    private void siguienteTurno() {
    	turno = (turno + 1) % pingus.size();
    }
    
    //método para actualizar el inventario
    private void actualizarInventario() {
    	Pinguino pingu = pingus.get(turno);
    	cantidadPeces.set(pingu.getPescado());
    	cantidadNieve.set(pingu.getBolasNieve());
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
            }

        } catch (Exception e) {
            e.printStackTrace();
            eventos.setText("Error al crear nueva partida.");
        }
        alInicioNew();
    }


    
    @FXML
    public void handleSaveGame() {
    	 Alert alerta = new Alert(AlertType.CONFIRMATION);
    	// Comprobar si la conexión está establecida
        if (con == null) {
            eventos.setText("Conexión a la base de datos no establecida.");
            return; // Salir si no hay conexión
        }

        // Ejecutamos la tarea de guardar en un hilo separado para no bloquear la UI
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                boolean exito = false;  // Variable para controlar el éxito del proceso

                try {
                    // Crear la partida en la base de datos
                    int idPartida = bbdd.crearNuevaPartida(con);
                    eventos.setText("Nueva partida creada con ID: " + idPartida);

                    // Guardamos el estado de las casillas (tablero de juego)
                    Integer[] casillas = obtenerEstadoCasillas();
                    bbdd.insertarPartida(con, idPartida, "EN CURSO", casillas); // Guardamos el estado de las casillas

                    // Guardamos las participaciones de cada jugador (pingüino)
                    for (Pinguino pingu : pingus) {
                        int idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                        if (idJugador == -1) {
                            // Si el jugador no existe, lo creamos
                            bbdd.crearJugador(con, pingu.getNombre(), "defaultPwd"); // Usar mejor contraseña en producción
                            idJugador = bbdd.obtenerIdJugador(con, pingu.getNombre());
                        }

                        // Crear la participación del jugador en la partida
                        bbdd.crearParticipacion(con, idPartida, idJugador, pingu.getPosicion(),
                                                 pingu.getDadoLento(), pingu.getDadoRapido(),
                                                 pingu.getPescado(), pingu.getBolasNieve());
                        //eventos.setText("Participación del jugador " + pingu.getNombre() + " guardada.");
                        
                        alerta.setTitle("Gaurdado");
                        alerta.setHeaderText("");
                        alerta.setContentText("Participación del jugador " + pingu.getNombre() + " guardada.");
                        alerta.showAndWait();
                    }

                    // Si todo se guarda correctamente, marcamos el éxito
                    exito = true;
                    //eventos.setText("Juego guardado exitosamente.");
                    alerta.setTitle("Gaurdado");
                    alerta.setHeaderText("");
                    alerta.setContentText("Juego guardado exitosamente.");
                    alerta.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    //eventos.setText("Error al guardar el juego.");
                    alerta.setTitle("ERROR e");
                    alerta.setHeaderText("");
                    alerta.setContentText("Error al guardar el juego");
                    alerta.showAndWait();
                }

                // Llamamos a done después de que la tarea termine para manejar el estado final
                if (!exito) {
                    publish("Ocurrió un problema durante el proceso de guardado.");
                }
                
                return null;
            }

            // Este método se ejecuta cuando la tarea termina (independientemente de si tuvo éxito o no)
            @Override
            protected void done() {
                try {
                    // Aquí podemos verificar si se completó correctamente
                    get();  // Lanzará una excepción si la tarea falló
                } catch (Exception e) {
                    // Si hubo un error, mostramos el mensaje de error en la UI
                    eventos.setText("Error final al guardar el juego.");
                }
            }
        };

        // Iniciamos la tarea en el hilo de fondo
        worker.execute();
    }


    // Método para obtener el estado de las casillas (tablero)
    private Integer[] obtenerEstadoCasillas() {
        // Este método debe devolver el estado actual de las casillas del tablero
        Integer[] casillas = new Integer[50];
        // Ejemplo de asignación: Casilla 1 = 'INICIO', Casilla 50 = 'META', etc.
        // Esto debe ser ajustado según cómo manejas las casillas en el juego
        for (int i = 0; i < 50; i++) {
            casillas[i] = i + 1; // Aquí deberías poner la lógica de asignación real
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
                } else {
                    eventos.setText("No se encontró la partida con ese número.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                eventos.setText("Error al cargar la partida.");
            }
        }
    }


    public void restaurarTablero() {
        try {
            // Recuperar las casillas de la base de datos usando el idPartida
            String queryCasillas = "SELECT casilla_id, tipo, posicion FROM casillas WHERE id_partida = ?";
            try (PreparedStatement stmt = con.prepareStatement(queryCasillas)) {
                stmt.setInt(1, idPartida); // Usamos idPartida para filtrar las casillas

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int casillaId = rs.getInt("casilla_id");
                        String tipoCasilla = rs.getString("tipo");
                        int posicion = rs.getInt("posicion");

                        // Determinar el tipo de casilla
                        TipoCasilla tipo = TipoCasilla.valueOf(tipoCasilla);

                        // Actualizar la casilla en el tablero
                        tableroCasillas[posicion] = tipo;
                    }
                }
            }
            
            // Si tienes otros recursos como nieve y peces, actualízalos aquí si es necesario.
            // actualizarRecursos(); // Descomentar si se necesita actualizar los recursos

            eventos.setText("Tablero restaurado exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            eventos.setText("Error al restaurar el tablero.");
        }
    }


    public void restaurarPinguinos() {
        try {
            // Recuperar los pingüinos de la base de datos usando el idPartida
            String queryPinguinos = "SELECT id, nombre, posicion, dado_normal, dado_lento, dado_rapido, bolas_nieve, pescado FROM pinguinos WHERE id_partida = ?";
            try (PreparedStatement stmt = con.prepareStatement(queryPinguinos)) {
                stmt.setInt(1, idPartida); // Usamos idPartida para filtrar los pingüinos

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nombre = rs.getString("nombre");
                        int posicion = rs.getInt("posicion");
                        int dadoNormal = rs.getInt("dado_normal");
                        int dadoLento = rs.getInt("dado_lento");
                        int dadoRapido = rs.getInt("dado_rapido");
                        int bolasNieve = rs.getInt("bolas_nieve");
                        int pescado = rs.getInt("pescado");

                        // Crear el pingüino y añadirlo a la lista
                        new Pinguino(id, nombre, posicion, dadoNormal, dadoLento, dadoRapido, bolasNieve, pescado);
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
        // Aquí podrías actualizar los recursos del juego, como los peces y la nieve
        // Si tienes una tabla en la base de datos que guarda la cantidad de estos recursos, 
        // la consulta podría ser algo como esto:

        String query = "SELECT cantidad_peces, cantidad_nieve FROM recursos WHERE id_partida = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, idPartida);  // Usamos el idPartida para filtrar los recursos específicos
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int cantidadPecesRecuperados = rs.getInt("cantidad_peces");
                    int cantidadNieveRecuperada = rs.getInt("cantidad_nieve");
                    
                    // Actualizamos las propiedades
                    cantidadPeces.set(cantidadPecesRecuperados);
                    cantidadNieve.set(cantidadNieveRecuperada);
                    
                    // Si hay algún componente en la UI que depende de estos valores, 
                    // los puedes actualizar aquí también.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        actualizarInventario();
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
        
    }

    @FXML
    private void handleRapido() {
        System.out.println("Fast.");
        // TODO
        Pinguino pinguActual = pingus.get(turno);
        actualizarInventario();
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
        actualizarInventario();
        
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
