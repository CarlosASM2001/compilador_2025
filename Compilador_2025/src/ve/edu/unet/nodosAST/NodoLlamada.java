package ve.edu.unet.nodosAST;

public class NodoLlamada extends NodoBase {
	private String nombre;
	private NodoBase argumento;

	public NodoLlamada(String nombre, NodoBase argumento) {
		this.nombre = nombre;
		this.argumento = argumento;
	}

	public String getNombre() {
		return nombre;
	}

	public NodoBase getArgumento() {
		return argumento;
	}
}