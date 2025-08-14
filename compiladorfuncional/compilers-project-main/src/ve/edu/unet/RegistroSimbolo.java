package ve.edu.unet;

import ve.edu.unet.nodosAST.tipoVar;

public class RegistroSimbolo {
	private String identificador;
	private int longitud;
	private int DireccionMemoria;
	private tipoVar tipoVariable;
	
	public RegistroSimbolo(String identificador, int longitud,
			int direccionMemoria, tipoVar tipoVariable) {
		super();
		this.identificador = identificador;
		this.longitud = longitud;
		DireccionMemoria = direccionMemoria;
		this.tipoVariable = tipoVariable;
	}

	public String getIdentificador() {
		return identificador;
	}

	public int getLongitud() {
		return longitud;
	}

	public int getDireccionMemoria() {
		return DireccionMemoria;
	}

	public void setDireccionMemoria(int direccionMemoria) {
		DireccionMemoria = direccionMemoria;
	}

	public tipoVar getTipoVariable() {
		return tipoVariable;
	}
}
