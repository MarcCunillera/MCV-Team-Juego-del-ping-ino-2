package Vista;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import Controlador.bbdd;

public class PantallaPrincipalController {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private Connection con;

    public void initialize() {
        // Se conecta a la base de datos al cargar la interfaz
        con = bbdd.conectarBaseDatos();
    }

    @FXML
    private void handleLogin() {
        String usuario = userField.getText().trim();
        String contrasena = passField.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            System.out.println("Debes rellenar usuario y contraseña.");
            return;
        }

        if (validarCredenciales(usuario, contrasena)) {
            System.out.println("Login exitoso. Cargando juego...");
            cargarPantallaJuego();
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    private void handleRegister() {
        String usuario = userField.getText().trim();
        String contrasena = passField.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            System.out.println("Debes rellenar usuario y contraseña para registrarte.");
            return;
        }

        if (contrasena.length() > 8) {
            System.out.println("Contraseña demasiado larga (máximo 8 caracteres).");
            return;
        }

        if (usuarioExiste(usuario)) {
            System.out.println("Ese usuario ya está registrado.");
            return;
        }

        bbdd.crearJugador(con, usuario, contrasena);
        System.out.println("Registro exitoso. Usuario: " + usuario);
        cargarPantallaJuego();
    }

    private boolean usuarioExiste(String nombre) {
        try {
            String sql = "SELECT * FROM Jugadores WHERE Nickname = '" + nombre + "'";
            ResultSet rs = bbdd.select(con, sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // prevenir registros fallidos por errores
        }
    }

    private boolean validarCredenciales(String nombre, String contrasena) {
        try {
            String sql = "SELECT * FROM Jugadores WHERE Nickname = '" + nombre + "' AND Contrasena = '" + contrasena + "'";
            ResultSet rs = bbdd.select(con, sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void cargarPantallaJuego() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/pantallaJuego.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

