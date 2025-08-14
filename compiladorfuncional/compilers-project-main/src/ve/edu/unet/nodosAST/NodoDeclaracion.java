package ve.edu.unet.nodosAST;

public class NodoDeclaracion extends NodoBase {
    private tipoVar tipoVariable;
    private String identificador;
    private int longitud;

    public NodoDeclaracion(tipoVar tipoVariable, String identificador, int longitud) {
        super();
        this.identificador = identificador;
        this.tipoVariable = tipoVariable;
        this.longitud = longitud;
    }

    public String getIdentificador() {
        return identificador;
    }

    public tipoVar getTipoVariable() {
        return tipoVariable;
    }

    public int getLongitud() {
        return longitud;
    }
}
