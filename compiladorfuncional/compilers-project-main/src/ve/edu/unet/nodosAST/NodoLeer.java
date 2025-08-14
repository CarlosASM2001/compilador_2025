package ve.edu.unet.nodosAST;

public class NodoLeer extends NodoBase {
	private String id;
	private NodoBase desplazamiento;

	public NodoLeer(String identificador) {
		super();
		this.id = identificador;
		this.desplazamiento = null;
	}

	public NodoLeer(String identificador, NodoBase desplazamiento) {
		super();
		this.id = identificador;
		this.desplazamiento = desplazamiento;
	}

	public String getIdentificador() {
		return id;
	}

	public NodoBase getDesplazamiento() {
		return desplazamiento;
	}

}
