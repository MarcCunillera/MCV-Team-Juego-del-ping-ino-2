package Vista;

import java.util.ArrayList;
import Controlador.*;
import Modelo.*;
import java.sql.Connection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class PantallaMenuController {
	@FXML private MenuItem newGame;
	@FXML private MenuItem loadGame;
	@FXML private MenuItem quitGame;
	
	@FXML private Button loginButton;
	@FXML private Button registerButton;
	
	@FXML private Text eventos;
	
	@FXML
	private void initialize() {
		// This method is called automatically after the FXML is loaded
        // You can set initial values or add listeners here
        System.out.println("PAntalla iniciada");
	}
	
	@FXML
	private void handleNewGame(ActionEvent event) {
		
	}
	
}
