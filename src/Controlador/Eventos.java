package Controlador;

import Modelo.*;

public class Eventos {
    private int idEvento;
    private int dadoRapido;
    private int dadoLento;
    private int pez;
    private int nieve;

    // Constructor
    public Eventos(int idEvento, int dadoRapido, int dadoLento, int pez, int nieve) {
        this.idEvento = idEvento;
        this.dadoRapido = dadoRapido;
        this.dadoLento = dadoLento;
        this.pez = pez;
        this.nieve = nieve;
    }

    // Getters y setters
    public int getIdEvento() { 
        return idEvento; 
    }
    public void setIdEvento(int idEvento) { 
        this.idEvento = idEvento; 
    }
    public int getDadoRapido() { 
        return dadoRapido; 
    }
    public void setDadoRapido(int dadoRapido) { 
        this.dadoRapido = dadoRapido; 
    }
    public int getDadoLento() { 
        return dadoLento; 
    }
    public void setDadoLento(int dadoLento) { 
        this.dadoLento = dadoLento; 
    }
    public int getPez() { 
        return pez; 
    }
    public void setPez(int pez) { 
        this.pez = pez; 
    }
    public int getNieve() { 
        return nieve; 
    }
    public void setNieve(int nieve) { 
        this.nieve = nieve; 
    }

    // Método para sobornar al oso
    public boolean sobornarOso(Pinguino pingu) {
        Inventario inventario = pingu.getInventario();
        
        // Verificar si tiene un pez
        for (ObjetosInventario obj : inventario.getInventario()) {
            if (obj.getIDobjeto() == 1 && obj.getCantidad() > 0) { // Pez
                inventario.eliminarObjeto(1);  // Eliminar un pez
                System.out.println("Has sobornado al oso con un pez 🦈 ¡Sigues jugando!");
                return true;
            }
        }
        
        // Si no tiene un pez, verificar el inventario
        System.out.println("No tienes peces para sobornar al oso :(");
        
        // Si tiene más de la mitad del inventario lleno
        int cantidadLleno = 0;
        for (ObjetosInventario obj : inventario.getInventario()) {
            if (obj.getCantidad() > 0) {
                cantidadLleno++;
            }
        }

        // Si tiene más de la mitad del inventario lleno, le quitamos la mitad de los objetos
        if (cantidadLleno > inventario.getInventario().size() / 2) {
            for (ObjetosInventario obj : inventario.getInventario()) {
                if (obj.getCantidad() > 0) {
                    int cantidadEliminar = obj.getCantidad() / 2;
                    obj.setCantidad(obj.getCantidad() - cantidadEliminar);
                    System.out.println("Se ha eliminado la mitad del inventario de " + obj.getNombreObjeto());
                }
            }
        } else {
            // Si no tiene más de la mitad del inventario ocupado, lo enviamos a la primera casilla
            pingu.setPosicion(0);
            System.out.println("No tienes suficientes objetos en tu inventario. Te envían a la primera casilla.");
        }
        
        return false;
    }
}
