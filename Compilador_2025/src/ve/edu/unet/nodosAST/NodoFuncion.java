package ve.edu.unet.nodosAST;

public class NodoFuncion extends NodoBase {
	private String nombre;
	private String parametro;
	private NodoBase cuerpo;
	private NodoBase retorno;

	public NodoFuncion(String nombre, String parametro, NodoBase cuerpo, NodoBase retorno) {
		this.nombre = nombre;
		this.parametro = parametro;
		this.cuerpo = cuerpo;
		this.retorno = retorno;
	}

	public String getNombre() {
		return nombre;
	}

	public String getParametro() {
		return parametro;
	}

	public NodoBase getCuerpo() {
		return cuerpo;
	}

	public NodoBase getRetorno() {
		return retorno;
	}
}