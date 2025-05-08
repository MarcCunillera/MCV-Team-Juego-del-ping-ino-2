package Vista;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Menu extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargar la pantalla principal del juego
        Parent root = FXMLLoader.load(getClass().getResource("/Resources/pantallaPrincipal.fxml"));

        Scene scene = new Scene(root);
        
        // Opcional: si tienes un estilo CSS
        // scene.getStylesheets().add(getClass().getResource("/Resources/styles.css").toExternalForm());

        stage.setTitle("Juego del Pingüino 🐧");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

