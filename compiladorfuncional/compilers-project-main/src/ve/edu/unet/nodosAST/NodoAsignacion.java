package ve.edu.unet.nodosAST;

public class NodoAsignacion extends NodoBase {
	private String identificador;
	private NodoBase expresion;
	private NodoBase desplazamiento;
	
	public NodoAsignacion(String identificador, NodoBase expresion) {
		super();
		this.identificador = identificador;
		this.expresion = expresion;
		this.desplazamiento = null;
	}

	public NodoAsignacion(String identificador, NodoBase expresion, NodoBase desplazamiento) {
		super();
		this.identificador = identificador;
		this.expresion = expresion;
		this.desplazamiento = desplazamiento;
	}

	public String getIdentificador() {
		return identificador;
	}

	public NodoBase getExpresion() {
		return expresion;
	}

	public NodoBase getDesplazamiento() {
		return desplazamiento;
	}
}
