package ve.edu.unet.nodosAST;

import ve.edu.unet.RegistroSimbolo;
import ve.edu.unet.TablaSimbolos;

public class Util {

	static int sangria = 0;
	public static int total_errores = 0;

	//Imprimo en modo texto con sangrias el AST
	public static void imprimirAST(NodoBase raiz) {
		sangria += 2;
		while (raiz != null) {
			printSpaces();
			if (raiz instanceof NodoDeclaracion)
				System.out.println("Declaracion");
			else if (raiz instanceof NodoIf)
				System.out.println("If");
			else if (raiz instanceof NodoRepeat)
				System.out.println("Repeat");
			else if (raiz instanceof NodoFor)
				System.out.println("For");

			else if (raiz instanceof NodoAsignacion)
				System.out.println("Asignacion a: " + ((NodoAsignacion) raiz).getIdentificador());

			else if (raiz instanceof NodoLeer)
				System.out.println("Lectura: " + ((NodoLeer) raiz).getIdentificador());

			else if (raiz instanceof NodoEscribir)
				System.out.println("Escribir");

			else if (raiz instanceof NodoOperacion
					|| raiz instanceof NodoValor
					|| raiz instanceof NodoIdentificador)
				imprimirNodo(raiz);
			else System.out.println("Tipo de nodo desconocido");
			;

			/* Hago el recorrido recursivo */
			if (raiz instanceof NodoDeclaracion) {
				printSpaces();
				System.out.println("**Tipo variable**");
				System.out.println(((NodoDeclaracion) raiz).getTipoVariable());
				System.out.println("**Nombre variable**");
				System.out.println(((NodoDeclaracion) raiz).getIdentificador());
				int longitud = ((NodoDeclaracion) raiz).getLongitud();
				if (longitud > 1) {
					System.out.println("**Longitud**");
					System.out.println(((NodoDeclaracion) raiz).getLongitud());
				}
			} else if (raiz instanceof NodoIf) {
				printSpaces();
				System.out.println("**Prueba IF**");
				imprimirAST(((NodoIf) raiz).getPrueba());
				printSpaces();
				System.out.println("**Then IF**");
				imprimirAST(((NodoIf) raiz).getParteThen());
				if (((NodoIf) raiz).getParteElse() != null) {
					printSpaces();
					System.out.println("**Else IF**");
					imprimirAST(((NodoIf) raiz).getParteElse());
				}
			} else if (raiz instanceof NodoFor) {
				printSpaces();
				System.out.println("**Inicio FOR**");
				imprimirAST(((NodoFor) raiz).getInicio());
				printSpaces();
				System.out.println("**Expresion FOR**");
				imprimirAST(((NodoFor) raiz).getExpresion());
				printSpaces();
				System.out.println("**Iterador FOR**");
				imprimirAST(((NodoFor) raiz).getIterador());
				printSpaces();
				System.out.println("**Cuerpo FOR**");
				imprimirAST(((NodoFor) raiz).getCuerpo());
			} else if (raiz instanceof NodoRepeat) {
				printSpaces();
				System.out.println("**Cuerpo REPEAT**");
				imprimirAST(((NodoRepeat) raiz).getCuerpo());
				printSpaces();
				System.out.println("**Prueba REPEAT**");
				imprimirAST(((NodoRepeat) raiz).getPrueba());
			} else if (raiz instanceof NodoAsignacion)
				imprimirAST(((NodoAsignacion) raiz).getExpresion());
			else if (raiz instanceof NodoEscribir)
				imprimirAST(((NodoEscribir) raiz).getExpresion());
			else if (raiz instanceof NodoOperacion) {
				printSpaces();
				System.out.println("**Expr Izquierda Operacion**");
				imprimirAST(((NodoOperacion) raiz).getOpIzquierdo());
				printSpaces();
				System.out.println("**Expr Derecha Operacion**");
				imprimirAST(((NodoOperacion) raiz).getOpDerecho());
			}
			raiz = raiz.getHermanoDerecha();
		}
		sangria -= 2;
	}

	/* Imprime espacios con sangria */
	static void printSpaces() {
		int i;
		for (i = 0; i < sangria; i++)
			System.out.print(" ");
	}

	/* Imprime informacion de los nodos */
	static void imprimirNodo(NodoBase raiz) {
		if (raiz instanceof NodoRepeat
				|| raiz instanceof NodoLeer
				|| raiz instanceof NodoEscribir) {
			System.out.println("palabra reservada: " + raiz.getClass().getName());
		}

		if (raiz instanceof NodoAsignacion)
			System.out.println(":=");

		if (raiz instanceof NodoOperacion) {
			tipoOp sel = ((NodoOperacion) raiz).getOperacion();
			if (sel == tipoOp.menor)
				System.out.println("<");
			if (sel == tipoOp.igual)
				System.out.println("=");
			if (sel == tipoOp.mas)
				System.out.println("+");
			if (sel == tipoOp.menos)
				System.out.println("-");
			if (sel == tipoOp.por)
				System.out.println("*");
			if (sel == tipoOp.entre)
				System.out.println("/");
		}

		if (raiz instanceof NodoValor) {
			System.out.println("NUM, val= " + ((NodoValor) raiz).getValor());
		}

		if (raiz instanceof NodoIdentificador) {
			System.out.println("ID, nombre= " + ((NodoIdentificador) raiz).getNombre());
		}

	}

	public static void analisisSemantico(TablaSimbolos ts, NodoBase raiz) {
		while (raiz != null) {
			if(raiz instanceof NodoAsignacion) {
				NodoAsignacion n = (NodoAsignacion) raiz;
				tipoVar tipoID = getTipo(ts, new NodoIdentificador(n.getIdentificador(), n.getDesplazamiento()));
				tipoVar tipoEX = getTipo(ts, n.getExpresion());
				if(tipoID != tipoEX) {
					total_errores++;
					System.out.println("*** Error en el tipo de dato para la asignacion");
				}
			}
			if(raiz instanceof NodoLeer) {
				NodoLeer n = (NodoLeer) raiz;
				tipoVar tipoID = getTipo(ts, new NodoIdentificador(n.getIdentificador(), n.getDesplazamiento()));
				if(tipoID != tipoVar.entero) {
					total_errores++;
					System.out.println("*** Error en el tipo de dato para la lectura");
				}
			}
			if(raiz instanceof NodoIf) {
				NodoIf n = (NodoIf) raiz;
				tipoVar tipoPrueba = getTipo(ts, n.getPrueba());
				if(tipoPrueba != tipoVar.booleano) {
					total_errores++;
					System.out.println("*** Error en el tipo de dato para la condicion del if");
				}
				analisisSemantico(ts, n.getParteThen());
				if(n.getParteElse() != null) {
					analisisSemantico(ts, n.getParteElse());
				}
			}
			if(raiz instanceof NodoRepeat) {
				NodoRepeat n = (NodoRepeat) raiz;
				tipoVar tipoPrueba = getTipo(ts, n.getPrueba());
				if(tipoPrueba != tipoVar.booleano) {
					total_errores++;
					System.out.println("*** Error en el tipo de dato para la condicion del repeat");
				}
				analisisSemantico(ts, n.getCuerpo());
			}
			if(raiz instanceof NodoFor) {
				NodoFor n = (NodoFor) raiz;
				tipoVar tipoEX = getTipo(ts, n.getExpresion());
				if (tipoEX != tipoVar.booleano) {
					total_errores++;
					System.out.println("*** Error en el tipo de dato para la condicion del for");
				}
				analisisSemantico(ts, n.getInicio());
				analisisSemantico(ts, n.getIterador());
				analisisSemantico(ts, n.getCuerpo());
			}
			raiz = raiz.getHermanoDerecha();
		}
	}

	private static tipoVar getTipo(TablaSimbolos ts, NodoBase n) {
		if (n instanceof NodoOperacion) {
			NodoOperacion op = (NodoOperacion) n;
			tipoVar tipoI = getTipo(ts, op.getOpIzquierdo());
			tipoVar tipoD = getTipo(ts, op.getOpDerecho());

			if (tipoI == null || tipoD == null) {
				System.out.println("*** Los tipos de dato en los operadores no coinciden");
				return null;
			}

			tipoVar res = null;
			switch (op.getOperacion()) {
				case mas:
				case menos:
				case por:
				case entre:
				case mod:
					if (tipoI != tipoVar.entero || tipoD != tipoVar.entero) {
						System.out.println("*** Operaciones matematicas requieren valores enteros");
						res = null;
					} else {
						res = tipoVar.entero;
					}
					break;
				case mayor:
				case menor:
				case mayorigual:
				case menorigual:
				case igual:
				case diferente:
					if (tipoI != tipoVar.entero || tipoD != tipoVar.entero) {
						System.out.println("*** Operaciones de comparacion requieren valores enteros");
						res = null;
					} else {
						res = tipoVar.booleano;
					}
					break;
				case or:
				case and:
					if (tipoI != tipoVar.booleano || tipoD != tipoVar.booleano) {
						System.out.println("*** Operaciones logicas requieren valores booleanos");
						res = null;
					} else {
						res = tipoVar.booleano;
					}
					break;
				case not:
					if (tipoI != tipoVar.booleano) {
						System.out.println("*** Operaciones logicas requieren valores booleanos");
						res = null;
					} else {
						res = tipoVar.booleano;
					}
					break;

			}
			return res;
		}
		else if (n instanceof NodoValor) {
			return tipoVar.entero;
		}
		else if (n instanceof NodoIdentificador) {
			NodoIdentificador nodoIdentificador = (NodoIdentificador)n;
			RegistroSimbolo rs = ts.BuscarSimbolo(nodoIdentificador.getNombre());
			tipoVar res = null;
			if (rs != null) {
				res = rs.getTipoVariable();
				if(nodoIdentificador.getIndice() != null){
					tipoVar tipoIndice = getTipo(ts, nodoIdentificador.getIndice());
					if(tipoIndice != tipoVar.entero){
						res = null;
						System.out.println("*** El indice de un vector debe ser entero");
					}
				}
			}
			else{
				System.out.println("*** Variable no declarada");
			}
			return res;
		}
		return null;
	}


}

