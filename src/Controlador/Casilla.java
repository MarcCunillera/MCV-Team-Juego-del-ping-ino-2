package Controlador;

import Vista.PantallaJuegoController.TipoCasilla;

public class Casilla {
    private int posicion;
    private TipoCasilla tipo;

    public Casilla(int posicion, TipoCasilla tipo) {
        this.posicion = posicion;
        this.tipo = tipo;
    }

	public int getPosicion() {
        return posicion;
    }

    public TipoCasilla getTipo() {
        return tipo;
    }
}