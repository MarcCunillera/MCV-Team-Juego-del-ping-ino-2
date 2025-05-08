package Modelo;

public class ObjetosInventario {
	private int IDcasillaInv;
	private int IDobjeto;
	private String nombreObjeto;
	private int cantidad;
	
	//constructor
	public ObjetosInventario(int IDcasillaInv, int IDobjeto, String nombreObjeto, int cantObjeto) {
		this.IDcasillaInv = IDcasillaInv;
		this.IDobjeto = IDobjeto;
		this.nombreObjeto = nombreObjeto;
		this.cantidad = cantObjeto;
	}
	
	//gettters y setters
	public int getIDcasillaInv() {
		return IDcasillaInv;
	}

	public void setIDcasillaInv(int iDcasillaInv) {
		IDcasillaInv = iDcasillaInv;
	}

	public int getIDobjeto() {
		return IDobjeto;
	}

	public void setIDobjeto(int iDobjeto) {
		IDobjeto = iDobjeto;
	}

	public String getNombreObjeto() {
		return nombreObjeto;
	}

	public void setNombreObjeto(String nombreObjeto) {
		this.nombreObjeto = nombreObjeto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	
	//to string
	@Override
	public String toString() {
		return "ObjetosInventario [IDcasillaInv=" + IDcasillaInv + ", IDobjeto=" + IDobjeto + ", nombreObjeto="
				+ nombreObjeto + ", cantidad=" + cantidad + "]";
	}
	
	
}
