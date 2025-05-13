package Modelo;

import java.util.Random;
import java.util.ArrayList;

public class Pinguino extends Usuarios{
    private String nombre;
    private int posicion;
    private Inventario inventario;
    private int dadoSeleccionado;
    private int dadoNormal;
    private int dadoLento;
    private int dadoRapido;
    private int bolasNieve;
    private int pescado;
    
    public int getDadoSeleccionado() {
		return dadoSeleccionado;
	}

	public void setDadoSeleccionado(int dadoSeleccionado) {
		this.dadoSeleccionado = dadoSeleccionado;
	}

	public int getDadoNormal() {
		return dadoNormal;
	}

	public void setDadoNormal(int dadoNormal) {
		this.dadoNormal = dadoNormal;
	}

	public int getDadoLento() {
		return dadoLento;
	}

	public void setDadoLento(int dadoLento) {
		this.dadoLento = dadoLento;
	}

	public int getDadoRapido() {
		return dadoRapido;
	}

	public void setDadoRapido(int dadoRapido) {
		this.dadoRapido = dadoRapido;
	}

	public int getBolasNieve() {
		return bolasNieve;
	}

	public void setBolasNieve(int bolasNieve) {
		this.bolasNieve = bolasNieve;
	}

	public int getPescado() {
		return pescado;
	}

	public void setPescado(int pescado) {
		this.pescado = pescado;
	}

	public static ArrayList<Pinguino> ListaPinguinos = new ArrayList<>();

    public Pinguino(int id, String nombre, int posicion, int dadoLento, int dadoRapido, int bolasNieve, int pescado) {
        super(id);
        this.nombre = nombre;
        this.posicion = posicion;
        this.dadoLento = dadoLento;
        this.dadoRapido = dadoRapido;
        this.bolasNieve = bolasNieve;
        this.pescado = pescado;
        
        ListaPinguinos.add(this);
    }
    
    //metodo para tirar dado normal
    public int tirarDadoNormal() {
        Random random = new Random();
        int resultado = random.nextInt(6) +1;
        return resultado;
    }
    
    //método para tirar dado lento
    public int tirarDadoRapido() {
    	Random random = new Random();
        int resultado = 0;
        resultado = random.nextInt(6) +1;
        resultado = resultado + 4;
        
        return resultado;
    }
    
    //método para tirar dado rápido
    public int tirarDadoLento() {
    	Random random = new Random();
        int resultado = 0;
        resultado = random.nextInt(3) + 1;

        return resultado;
    }
    
    //getters y setters
    public String getNombre() { 
    	return nombre; 
    }
    public void setNombre(String nombre) { 
    	this.nombre = nombre; 
    }
    public int getPosicion() { 
    	return posicion; 
    }
    public void setPosicion(int posicion) { 
    	this.posicion = posicion; 
    }
    public Inventario getInventario() { 
    	return inventario; 
    }
    public void setInventario(Inventario inventario) { 
    	this.inventario = inventario; 
    }

	public static ArrayList<Pinguino> getListaPinguinos() {
		return ListaPinguinos;
	}

	public void setListaPinguinos(ArrayList<Pinguino> listaPinguinos) {
		ListaPinguinos = listaPinguinos;
	}

	@Override
	public String toString() {
		return "Pinguino [nombre=" + nombre + ", posicion=" + posicion + ", inventario=" + inventario
				+ ", ListaPinguinos=" + ListaPinguinos + "]";
	}
}
