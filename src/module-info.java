module MCVTeamJuegoDelPinguino_2 {
	requires java.sql;
	
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires java.desktop;
	
	opens Vista to javafx.fxml;
	
	exports Vista;
	exports Controlador;
	exports Modelo;
}