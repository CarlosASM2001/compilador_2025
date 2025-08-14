package ve.edu.unet.nodosAST;

public class NodoFor extends NodoBase {
    private NodoBase inicio;
    private NodoBase expresion;
    private NodoBase iterador;
    private NodoBase cuerpo;

    public NodoFor(NodoBase inicio, NodoBase expresion, NodoBase iterador, NodoBase cuerpo) {
        this.inicio = inicio;
        this.expresion = expresion;
        this.iterador = iterador;
        this.cuerpo = cuerpo;
    }

    public NodoBase getInicio() {
        return inicio;
    }

    public NodoBase getExpresion() {
        return expresion;
    }

    public NodoBase getIterador() {
        return iterador;
    }

    public NodoBase getCuerpo() {
        return cuerpo;
    }
}
