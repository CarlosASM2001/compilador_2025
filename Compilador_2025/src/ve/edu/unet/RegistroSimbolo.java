package ve.edu.unet;

import ve.edu.unet.nodosAST.tipoVar;

public class RegistroSimbolo {
	private String identificador;
	private int numLinea;
	private int direccionMemoria;
	private tipoVar tipoVariable;
	
	public RegistroSimbolo(String identificador, int numLinea, int direccionMemoria) {
		super();
		this.identificador = identificador;
		this.numLinea = numLinea;
		this.direccionMemoria = direccionMemoria;
		this.tipoVariable = tipoVar.entero; // por defecto entero
	}

	public String getIdentificador() {
		return identificador;
	}

	public int getNumLinea() {
		return numLinea;
	}

	public int getDireccionMemoria() {
		return direccionMemoria;
	}

	public tipoVar getTipoVariable() {
		return tipoVariable;
	}
}