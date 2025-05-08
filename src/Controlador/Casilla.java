package Controlador;

import Modelo.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Casilla {
    private int idCasilla;
    private int idTipoCasilla;
    private String tipoCasilla;
    
    //control de casillas
    private static int casInicio = 4;
    private static int contOso = 0;
    private static int contAgujero = 0;
    private static int contTrineo = 0;
    private static int contCasillaInterrogante = 0;

    public Casilla(int idCasilla, int idTipoCasilla, String tipoCasilla) {
        this.idCasilla = idCasilla;
        this.idTipoCasilla = idTipoCasilla;
        this.tipoCasilla = tipoCasilla;
    }

    public int getIdCasilla() { 
    	return idCasilla; 
    }
    public void setIdCasilla(int idCasilla) { 
    	this.idCasilla = idCasilla; 
    }
    public int getIDTipoCasilla() { 
    	return idTipoCasilla; 
    }
    public void setIdTipoCasilla(int idTipoCasilla) { 
    	this.idTipoCasilla = idTipoCasilla; 
    }
    public String getTipoCasilla() { 
    	return tipoCasilla; 
    }
    public void setTipoCasilla(String tipoCasilla) { 
    	this.tipoCasilla = tipoCasilla; 
    }

    public static void asignarCasillas(int idCasilla, ArrayList<Casilla> tablero, GridPane tableroGrafico) {
        Random rn = new Random();
        int tipo = rn.nextInt(6);
        String nombreTipo;
        int[] types = {0,1,2,3,4,5};
        
        //control máximos
        if (contOso >= 2 && tipo == 1) {
        	types[1] = 0;
        }
        if(contAgujero >= 4 && tipo == 2) {
        	types[2] = 0;
        }
        if(contTrineo >= 3 && tipo == 3) {
        	types[3] = 0;
        }
        if(contCasillaInterrogante >= 10 && tipo == 4) {
        	types[4] = 0;
        }
        tipo = types[tipo];
        //control primeras 4 casillas (normales)
        if (casInicio > 0) {
        	tipo = 0;
        	casInicio--;
        }
        
        //elegir el tipo
        switch (tipo) {
            case 0: nombreTipo = "Normal"; break;
            case 1: nombreTipo = "Oso";contOso++; break;
            case 2: nombreTipo = "Agujero en el hielo";contAgujero++; break;
            case 3: nombreTipo = "Trineo";contTrineo++; break;
            case 4: nombreTipo = "Casilla Interrogante";contCasillaInterrogante++; break;
            default: nombreTipo = "Normal"; break;
        }
        tablero.add(new Casilla(idCasilla, tipo, nombreTipo));
        
        //Entorno grafico
        Label label = new Label(nombreTipo);
        int fila = idCasilla / 10;
        int columna = idCasilla % 10;
        
        try {
			tableroGrafico.add(label, columna, fila);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //método para gestionar la casilla interrogante
    public void casillaInterrogante(Pinguino pingu) {
        Random rn = new Random();
        int evento = rn.nextInt(3) + 1;
        int idObj = 0;
        int cantidad = 0;
        String nomObj = "";
        int i = -1;
        

        switch (evento) {
            case 1: idObj = 1; nomObj = "Pez"; cantidad = 1; System.out.println("Ha tocado un pez! 🦈"); break;
            case 2: idObj = 2; nomObj = "Bolas de Nieve"; cantidad = rn.nextInt(3) + 1; System.out.println("Ha/n tocado " + cantidad + " bola/s de nieve! ❄"); break;
            case 3:
                if (rn.nextBoolean()) {
                    idObj = 3;
                    nomObj = "Dado Rápido";
                    System.out.println("Ha tocado un dado Rápido! 🎲");
                } else {
                    idObj = 4;
                    nomObj = "Dado Lento";
                    System.out.println("Ha tocado un dado Lento! 🎲");
                }
                cantidad = 1;
                break;
        }
        
        //validación para añadir el objeto al inventario
        ObjetosInventario obj = new ObjetosInventario(i, idObj, nomObj, cantidad);
        Inventario.modificarInventario(obj);
    }
}
