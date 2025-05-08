package Modelo;

public class Usuarios {

	private int id;
	
	//constructor
	public Usuarios (int ID) {
		this.id = ID;
	}
	
	//Geters y seters
    public int getID() {
        return id;
    }

    public void setID(int ID) {
        this.id = ID;
    }
    
    //ToString per mostra el ID del Usuari
    public String toString() {
        return "ID: " + id ; 
    }

}
