package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

public class Generador {
	/* Ilustracion de la disposicion de la memoria en
	 * este ambiente de ejecucion para el lenguaje Tiny
	 *
	 * |t1	|<- mp (Maxima posicion de memoria de la TM
	 * |t1	|<- desplazamientoTmp (tope actual)
	 * |free|
	 * |free|
	 * |...	|
	 * |x	|
	 * |y	|<- gp
	 * 
	 * */
	
	
	
	/* desplazamientoTmp es una variable inicializada en 0
	 * y empleada como el desplazamiento de la siguiente localidad
	 * temporal disponible desde la parte superior o tope de la memoria
	 * (la que apunta el registro MP).
	 * 
	 * - Se decrementa (desplazamientoTmp--) despues de cada almacenamiento y
	 * 
	 * - Se incrementa (desplazamientoTmp++) despues de cada eliminacion/carga en 
	 *   otra variable de un valor de la pila.
	 * 
	 * Pudiendose ver como el apuntador hacia el tope de la pila temporal
	 * y las llamadas a la funcion emitirRM corresponden a una inserccion 
	 * y extraccion de esta pila
	 */
	private static int desplazamientoTmp = 0;
	private static TablaSimbolos tablaSimbolos = null;
	
	public static void setTablaSimbolos(TablaSimbolos tabla){
		tablaSimbolos = tabla;
	}
	
	public static void generarCodigoObjeto(NodoBase raiz){
		System.out.println();
		System.out.println();
		System.out.println("------ CODIGO OBJETO DEL LENGUAJE TINY GENERADO PARA LA TM ------");
		System.out.println();
		System.out.println();
		generarPreludioEstandar();
		generar(raiz);
		/*Genero el codigo de finalizacion de ejecucion del codigo*/   
		UtGen.emitirComentario("Fin de la ejecucion.");
		UtGen.emitirRO("HALT", 0, 0, 0, "");
		System.out.println();
		System.out.println();
		System.out.println("------ FIN DEL CODIGO OBJETO DEL LENGUAJE TINY GENERADO PARA LA TM ------");
	}
	
	//Funcion principal de generacion de codigo
	//prerequisito: Fijar la tabla de simbolos antes de generar el codigo objeto 
	private static void generar(NodoBase nodo){
	if(tablaSimbolos!=null){
		if (nodo instanceof  NodoIf){
			generarIf(nodo);
		}else if (nodo instanceof  NodoRepeat){
			generarRepeat(nodo);
		}else if (nodo instanceof  NodoFor){
			generarFor(nodo);
		}else if (nodo instanceof  NodoAsignacion){
			generarAsignacion(nodo);
		}else if (nodo instanceof  NodoLeer){
			generarLeer(nodo);
		}else if (nodo instanceof  NodoEscribir){
			generarEscribir(nodo);
		}else if (nodo instanceof NodoValor){
			generarValor(nodo);
		}else if (nodo instanceof NodoIdentificador){
			generarIdentificador(nodo);
		}else if (nodo instanceof NodoOperacion){
			generarOperacion(nodo);
		}
		else{
			if(!(nodo instanceof NodoDeclaracion))
			System.out.println("BUG: Tipo de nodo a generar desconocido");
		}
		/*Si el hijo de extrema izquierda tiene hermano a la derecha lo genero tambien*/
		if(nodo.TieneHermano())
			generar(nodo.getHermanoDerecha());
	}else
		System.out.println("���ERROR: por favor fije la tabla de simbolos a usar antes de generar codigo objeto!!!");
}

	private static void generarIf(NodoBase nodo){
    	NodoIf n = (NodoIf)nodo;
		int localidadSaltoElse,localidadSaltoEnd,localidadActual;
		if(UtGen.debug)	UtGen.emitirComentario("-> if");
		/*Genero el codigo para la parte de prueba del IF*/
		generar(n.getPrueba());
		localidadSaltoElse = UtGen.emitirSalto(1);
		UtGen.emitirComentario("If: el salto hacia el else debe estar aqui");
		/*Genero la parte THEN*/
		generar(n.getParteThen());
		localidadSaltoEnd = UtGen.emitirSalto(1);
		UtGen.emitirComentario("If: el salto hacia el final debe estar aqui");
		localidadActual = UtGen.emitirSalto(0);
		UtGen.cargarRespaldo(localidadSaltoElse);
		UtGen.emitirRM_Abs("JEQ", UtGen.AC, localidadActual, "if: jmp hacia else");
		UtGen.restaurarRespaldo();
		/*Genero la parte ELSE*/
		if(n.getParteElse()!=null){
			generar(n.getParteElse());
    	}
		localidadActual = UtGen.emitirSalto(0);
		UtGen.cargarRespaldo(localidadSaltoEnd);
		UtGen.emitirRM_Abs("LDA", UtGen.PC, localidadActual, "if: jmp hacia el final");
		UtGen.restaurarRespaldo();
		
		if(UtGen.debug)	UtGen.emitirComentario("<- if");
	}
	
	private static void generarRepeat(NodoBase nodo){
    	NodoRepeat n = (NodoRepeat)nodo;
		int localidadSaltoInicio;
		if(UtGen.debug)	UtGen.emitirComentario("-> repeat");
			localidadSaltoInicio = UtGen.emitirSalto(0);
			UtGen.emitirComentario("repeat: el salto hacia el final (luego del cuerpo) del repeat debe estar aqui");
			/* Genero el cuerpo del repeat */
			generar(n.getCuerpo());
			/* Genero el codigo de la prueba del repeat */
			generar(n.getPrueba());
			UtGen.emitirRM_Abs("JEQ", UtGen.AC, localidadSaltoInicio, "repeat: jmp hacia el inicio del cuerpo");
		if(UtGen.debug)	UtGen.emitirComentario("<- repeat");
	}

	private static void generarFor(NodoBase nodo){
		NodoFor n = (NodoFor) nodo;
		int localidadSaltoInicio,localidadSaltoEnd, localidadActual;
		if(UtGen.debug)	UtGen.emitirComentario("-> for");
		generar(n.getInicio());
		localidadSaltoInicio = UtGen.emitirSalto(0);
		generar(n.getExpresion());
		localidadSaltoEnd = UtGen.emitirSalto(1);
		UtGen.emitirComentario("for: el salto hacia el final debe estar aqui");
		/* Genero el cuerpo del for */
		generar(n.getCuerpo());
		/* Genero la iteracion del for */
		generar(n.getIterador());
		UtGen.emitirRM_Abs("LDA", UtGen.PC, localidadSaltoInicio, "for: salto incondicional al inicio");
		localidadActual = UtGen.emitirSalto(0);
		UtGen.cargarRespaldo(localidadSaltoEnd);
		UtGen.emitirRM_Abs("JEQ", UtGen.AC, localidadActual, "for: jmp hacia el final");
		UtGen.restaurarRespaldo();
		if(UtGen.debug)	UtGen.emitirComentario("<- for");
	}

	private static void generarAsignacion(NodoBase nodo){
		NodoAsignacion n = (NodoAsignacion)nodo;
		int direccion;
		if(UtGen.debug)	UtGen.emitirComentario("-> asignacion");		
		/* Genero el codigo para la expresion a la derecha de la asignacion */
		/* Ahora almaceno el valor resultante */
		direccion = tablaSimbolos.getDireccion(n.getIdentificador());
		if(n.getDesplazamiento() == null) {
			generar(n.getExpresion());
			UtGen.emitirRM("ST", UtGen.AC, direccion, UtGen.GP, "asignacion: almaceno el valor para el id " + n.getIdentificador());
		}
		else{
			UtGen.emitirRM("LDC", UtGen.DESP, direccion, 0, "cargo direccion base en registro DESP");
			generar(n.getDesplazamiento());
			UtGen.emitirRO("ADD", UtGen.DESP, UtGen.AC, UtGen.DESP, "calculo direccion real");
			generar(n.getExpresion());
			UtGen.emitirRM("ST", UtGen.AC, 0, UtGen.DESP, "cargar valor de identificador con desplazamiento: "+n.getIdentificador());
		}
		if(UtGen.debug)	UtGen.emitirComentario("<- asignacion");
	}
	
	private static void generarLeer(NodoBase nodo){
		NodoLeer n = (NodoLeer)nodo;
		int direccion;
		if(UtGen.debug)	UtGen.emitirComentario("-> leer");
		direccion = tablaSimbolos.getDireccion(n.getIdentificador());
		if(n.getDesplazamiento() == null) {
			UtGen.emitirRO("IN", UtGen.AC, 0, 0, "leer: lee un valor entero ");
			UtGen.emitirRM("ST", UtGen.AC, direccion, UtGen.GP, "leer: almaceno el valor entero leido en el id " + n.getIdentificador());
		}
		else{
			UtGen.emitirRM("LDC", UtGen.DESP, direccion, 0, "cargo direccion base en registro DESP");
			generar(n.getDesplazamiento());
			UtGen.emitirRO("ADD", UtGen.DESP, UtGen.AC, UtGen.DESP, "calculo direccion real");
			UtGen.emitirRO("IN", UtGen.AC, 0, 0, "leer: lee un valor entero ");
			UtGen.emitirRM("ST", UtGen.AC, 0, UtGen.DESP, "cargar valor de identificador con desplazamiento: "+n.getIdentificador());
		}
		if(UtGen.debug)	UtGen.emitirComentario("<- leer");
	}
	
	private static void generarEscribir(NodoBase nodo){
		NodoEscribir n = (NodoEscribir)nodo;
		if(UtGen.debug)	UtGen.emitirComentario("-> escribir");
		/* Genero el codigo de la expresion que va a ser escrita en pantalla */
		generar(n.getExpresion());
		/* Ahora genero la salida */
		UtGen.emitirRO("OUT", UtGen.AC, 0, 0, "escribir: genero la salida de la expresion");
		if(UtGen.debug)	UtGen.emitirComentario("<- escribir");
	}
	
	private static void generarValor(NodoBase nodo){
    	NodoValor n = (NodoValor)nodo;
    	if(UtGen.debug)	UtGen.emitirComentario("-> constante");
    	UtGen.emitirRM("LDC", UtGen.AC, n.getValor(), 0, "cargar constante: "+n.getValor());
    	if(UtGen.debug)	UtGen.emitirComentario("<- constante");
	}
	
	private static void generarIdentificador(NodoBase nodo){
		NodoIdentificador n = (NodoIdentificador)nodo;
		int direccion;
		if(UtGen.debug)	UtGen.emitirComentario("-> identificador");
		direccion = tablaSimbolos.getDireccion(n.getNombre());
		if(n.getIndice() == null) {
			UtGen.emitirRM("LD", UtGen.AC, direccion, UtGen.GP, "cargar valor de identificador: "+n.getNombre());
		}
		else{
			UtGen.emitirRM("LDC", UtGen.DESP, direccion, 0, "cargo direccion base en registro DESP");
			generar(n.getIndice());
			UtGen.emitirRO("ADD", UtGen.DESP, UtGen.AC, UtGen.DESP, "calculo direccion real");
			UtGen.emitirRM("LD", UtGen.AC, 0, UtGen.DESP, "cargar valor de identificador con desplazamiento: "+n.getNombre());
		}
		if(UtGen.debug)	UtGen.emitirComentario("-> identificador");
	}

	private static void generarOperacion(NodoBase nodo){
		NodoOperacion n = (NodoOperacion) nodo;
		if(UtGen.debug)	UtGen.emitirComentario("-> Operacion: " + n.getOperacion());
		/* Genero la expresion izquierda de la operacion */
		generar(n.getOpIzquierdo());
		/* Almaceno en la pseudo pila de valor temporales el valor de la operacion izquierda */
		UtGen.emitirRM("ST", UtGen.AC, desplazamientoTmp--, UtGen.MP, "op: push en la pila tmp el resultado expresion izquierda");
		/* Genero la expresion derecha de la operacion */
		generar(n.getOpDerecho());
		/* Ahora cargo/saco de la pila el valor izquierdo */
		UtGen.emitirRM("LD", UtGen.AC1, ++desplazamientoTmp, UtGen.MP, "op: pop o cargo de la pila el valor izquierdo en AC1");
		switch(n.getOperacion()){
			case	mas:
			case    or:
							UtGen.emitirRO("ADD", UtGen.AC, UtGen.AC1, UtGen.AC, "op: +");
							break;
			case	menos:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: -");
							break;
			case	por:
			case    and:
							UtGen.emitirRO("MUL", UtGen.AC, UtGen.AC1, UtGen.AC, "op: *");
							break;
			case	entre:	UtGen.emitirRO("DIV", UtGen.AC, UtGen.AC1, UtGen.AC, "op: /");
							break;
			case	mod:	UtGen.emitirRO("SUB", UtGen.AC1, UtGen.AC1, UtGen.AC, "op: mod");
							UtGen.emitirRM("JLT", UtGen.AC1, 1, UtGen.PC, "");
							UtGen.emitirRM("LDA", UtGen.PC, -3, UtGen.PC, "");
							UtGen.emitirRO("ADD", UtGen.AC, UtGen.AC1, UtGen.AC, "");
							break;
			case	menor:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: <");
							UtGen.emitirRM("JLT", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC<0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case	igual:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: ==");
							UtGen.emitirRM("JEQ", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case	mayor:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: >");
							UtGen.emitirRM("JGT", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case	menorigual:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: <=");
							UtGen.emitirRM("JLE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case	mayorigual:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: >=");
							UtGen.emitirRM("JGE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case	diferente:	UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: <>");
							UtGen.emitirRM("JNE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
							UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
							UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incodicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
							break;
			case  not:		UtGen.emitirRM("JNE", UtGen.AC1, 1, UtGen.PC, "op not, si es diferente de cero, salto");
							UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de falso, asigno verdadero (AC=1)");
							break;
			default:
							UtGen.emitirComentario("BUG: tipo de operacion desconocida");
		}
		if(UtGen.debug)	UtGen.emitirComentario("<- Operacion: " + n.getOperacion());
	}
	
	//TODO: enviar preludio a archivo de salida, obtener antes su nombre
	private static void generarPreludioEstandar(){
		UtGen.emitirComentario("Compilacion TINY para el codigo objeto TM");
		UtGen.emitirComentario("Archivo: "+ "NOMBRE_ARREGLAR");
		/*Genero inicializaciones del preludio estandar*/
		/*Todos los registros en tiny comienzan en cero*/
		UtGen.emitirComentario("Preludio estandar:");
		UtGen.emitirRM("LD", UtGen.MP, 0, UtGen.AC, "cargar la maxima direccion desde la localidad 0");
		UtGen.emitirRM("ST", UtGen.AC, 0, UtGen.AC, "limpio el registro de la localidad 0");
	}

}
