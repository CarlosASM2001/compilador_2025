package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

import java.util.*;




public class TablaSimbolos {
	private HashMap<String, RegistroSimbolo> tabla;
	private int direccion;  //Contador de las localidades de memoria asignadas a la tabla
	
	public TablaSimbolos() {
		super();
		tabla = new HashMap<String, RegistroSimbolo>();
		direccion=0;
	}

	public void cargarTabla(NodoBase raiz){
		while (raiz != null) {
	    if (raiz instanceof NodoIdentificador){
	    	InsertarSimbolo(((NodoIdentificador)raiz).getNombre(),-1);
	    	if(((NodoIdentificador)raiz).getIndice() != null){
	    		cargarTabla(((NodoIdentificador)raiz).getIndice());
	    	}
	    }

	    /* Hago el recorrido recursivo */
	    if (raiz instanceof  NodoIf){
	    	cargarTabla(((NodoIf)raiz).getPrueba());
	    	cargarTabla(((NodoIf)raiz).getParteThen());
	    	if(((NodoIf)raiz).getParteElse()!=null){
	    		cargarTabla(((NodoIf)raiz).getParteElse());
	    	}
	    }
	    else if (raiz instanceof  NodoRepeat){
	    	cargarTabla(((NodoRepeat)raiz).getCuerpo());
	    	cargarTabla(((NodoRepeat)raiz).getPrueba());
	    }
	    else if (raiz instanceof  NodoFor){
	    	cargarTabla(((NodoFor)raiz).getInicio());
	    	cargarTabla(((NodoFor)raiz).getExpresion());
	    	cargarTabla(((NodoFor)raiz).getIterador());
	    	cargarTabla(((NodoFor)raiz).getCuerpo());
	    }
	    else if (raiz instanceof  NodoAsignacion){
	    	InsertarSimbolo(((NodoAsignacion)raiz).getIdentificador(), -1);
	    	cargarTabla(((NodoAsignacion)raiz).getExpresion());
	    }
	    else if (raiz instanceof  NodoLeer){
	    	InsertarSimbolo(((NodoLeer)raiz).getIdentificador(), -1);
	    	if(((NodoLeer)raiz).getDesplazamiento()!=null){
	    		cargarTabla(((NodoLeer)raiz).getDesplazamiento());
	    	}
	    }
	    else if (raiz instanceof  NodoEscribir)
	    	cargarTabla(((NodoEscribir)raiz).getExpresion());
	    else if (raiz instanceof NodoOperacion){
	    	cargarTabla(((NodoOperacion)raiz).getOpIzquierdo());
	    	cargarTabla(((NodoOperacion)raiz).getOpDerecho());
	    }
	    else if (raiz instanceof NodoDeclaracion){
	    	InsertarSimbolo(((NodoDeclaracion)raiz).getIdentificador(), -1);
	    }
	    else if (raiz instanceof NodoFuncion){
	    	NodoFuncion f = (NodoFuncion) raiz;
	    	InsertarSimbolo(f.getNombre(), -1);
	    	if (f.getParametro() != null) InsertarSimbolo(f.getParametro(), -1);
	    	cargarTabla(f.getCuerpo());
	    	if (f.getRetorno() != null) cargarTabla(f.getRetorno());
	    }
	    raiz = raiz.getHermanoDerecha();
	  }
	}
	
	//true es nuevo no existe se insertara, false ya existe NO se vuelve a insertar 
	public boolean InsertarSimbolo(String identificador, int numLinea){
		RegistroSimbolo simbolo;
		if(tabla.containsKey(identificador)){
			return false;
		}else{
			simbolo= new RegistroSimbolo(identificador,numLinea,direccion++);
			tabla.put(identificador,simbolo);
			return true;			
		}
	}
	
	public RegistroSimbolo BuscarSimbolo(String identificador){
		RegistroSimbolo simbolo=(RegistroSimbolo)tabla.get(identificador);
		return simbolo;
	}
	
	public void ImprimirClaves(){
		System.out.println("*** Tabla de Simbolos ***");
		for( Iterator <String>it = tabla.keySet().iterator(); it.hasNext();) { 
            String s = (String)it.next();
	    System.out.println("Consegui Key: "+s+" con direccion: " + BuscarSimbolo(s).getDireccionMemoria());
		}
	}

	public int getDireccion(String Clave){
		return BuscarSimbolo(Clave).getDireccionMemoria();
	}
	
	/*
	 * TODO:
	 * 1. Crear lista con las lineas de codigo donde la variable es usada.
	 * 2. Manejo real de alcances (pila de tablas o mapas anidados) para variables locales de función.
	 * */
}
