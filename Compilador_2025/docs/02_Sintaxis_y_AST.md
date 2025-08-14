### Análisis Sintáctico (CUP) y AST — Gramática, Nodos y Precedencias

Este documento explica la gramática CUP, la construcción del AST y las estructuras de datos de nodos en `Compilador_2025`, con foco en funciones, vectores y la secuenciación de sentencias.

- Referencias clave:
  - Gramática: `src/especificacion/sintactico.cup`
  - Parser generado: `src/ve/edu/unet/parser.java`
  - Nodos AST: `src/ve/edu/unet/nodosAST/*.java`

### Visión general de la gramática
El no terminal inicial `program` admite un preámbulo opcional de declaraciones/funciones y un bloque principal entre `BEGIN ... END`:
- `program ::= preamble BEGIN stmt_seq END | BEGIN stmt_seq END`
- `preamble` concatena elementos `pre_item` como:
  - Declaración global: `GLOBAL ID;` ⇒ `NodoDeclaracion(entero, id, 1)`
  - Declaración de función: `FUNCTION ID(LPAREN ID RPAREN) BEGIN stmt_seq END` ⇒ `NodoFuncion(nombre, param, cuerpo, null)`

### Sentencias (`stmt`) y secuencias (`stmt_seq`)
- `stmt_seq` encadena nodos hermanos mediante `HermanoDerecha` (ver `NodoBase`).
- Sentencias soportadas (`stmt`): `if`, `repeat`, `for`, `assign`, `read`, `write`, `decl`, `return`, `func_decl`.

### Declaraciones y vectores
- Variable escalar: `VAR ID` ⇒ `NodoDeclaracion(tipoVar.entero, id, 1)`.
- Vector: `VAR ID : ARRAY [ NUM ]` ⇒ `NodoDeclaracion(..., longitud = NUM)`.
- Acceso indexado en expresiones y E/S:
  - Factor indexado: `ID [ exp ]` ⇒ `NodoIdentificador(nombre, indice)`.
  - Asignación indexada: `ID [ exp ] := exp` ⇒ `NodoAsignacion(ident, exp, desplazamiento)`.
  - Lectura indexada: `READ ID [ exp ]` ⇒ `NodoLeer(ident, desplazamiento)`.

### Funciones y llamadas
- Declaración: `FUNCTION fname(param) BEGIN stmt_seq END` ⇒ un `NodoFuncion` con un único parámetro (modelo didáctico).
- Llamada como factor:
  - `CALL fname()` ⇒ `NodoLlamada(fname, null)`.
  - `CALL fname(exp)` ⇒ `NodoLlamada(fname, exp)`.
- Retorno: `RETURN exp` ⇒ `NodoReturn(exp)`.

Nota: El alcance de variables es global en la implementación actual. El parámetro de función se modela como un nombre en la misma `TablaSimbolos` (ver sección TS).

### Expresiones y precedencias
La gramática declara precedencias en CUP para evitar ambigüedades:
- `POW` (asociatividad derecha), luego `OR`, `AND`, relacionales (`EQ`, `NE`, `LT`, `LE`, `GT`, `GE`), suma/resta, multiplicación/división/módulo.
- Producciones clave:
  - `exp ::= logic_exp AND logic_exp | logic_exp OR logic_exp | NOT logic_exp | logic_exp`
  - `logic_exp ::= simple_exp (LT|EQ|GT|LE|GE|NE) simple_exp | simple_exp`
  - `simple_exp ::= simple_exp PLUS term | simple_exp MINUS term | term`
  - `term ::= term TIMES factor | term OVER factor | term MOD factor | term POW factor | factor`
  - `factor ::= ( exp ) | NUM | ID | ID [ exp ] | CALL ... | error`

### Modelo de AST
Todos los nodos heredan de `NodoBase`, que aporta encadenamiento por hermanos (`HermanoDerecha`). Nodos principales:
- `NodoIf`: prueba, then, else opcional.
- `NodoRepeat`: cuerpo, prueba.
- `NodoFor`: inicio (una asignación), condición, iterador, cuerpo.
- `NodoAsignacion`: `identificador`, `expresion`, `desplazamiento` opcional (para vectores).
- `NodoLeer`/`NodoEscribir`: operaciones de E/S, con `desplazamiento` opcional en lectura.
- `NodoOperacion`: operador `tipoOp` y operandos izquierdo/derecho.
- `NodoValor`: entero literal.
- `NodoIdentificador`: `nombre` e `indice` opcional.
- `NodoFuncion`: `nombre`, `parametro`, `cuerpo`, `retorno` (no usado en esta gramática; se usa `RETURN exp`).
- `NodoLlamada`: `nombre`, `argumento` opcional.
- `NodoReturn`: `expresion`.

### Integración con `TablaSimbolos`
- Durante el recorrido `TablaSimbolos.cargarTabla(root)` se insertan símbolos y se asignan direcciones lineales en memoria global (`GP`).
- Para vectores, se reserva espacio contiguo: `longitud` posiciones (se ajusta `direccion` internamente).
- Para funciones, se insertan el nombre de la función y su parámetro como identificadores (modelo plano, sin ámbitos).

### Errores sintácticos
- La gramática contiene alternativas `error` en `stmt` y `factor` para sincronización simple y mensajes como "Ocurrió error en cup # 000x".
- Recomendación: extender con reglas de recuperación dirigidas si se desea una experiencia de diagnóstico más robusta.

### Limitaciones intencionales (modelo didáctico)
- Un único parámetro por función, sin tipos explícitos, y sin ámbitos anidados.
- No hay listas de argumentos ni aridad variable.
- Vectores de una dimensión con índices enteros.

### Ejemplo ilustrativo
Código fuente (`ejemplo_fuente/ejemplo_funcion.tiny`):
```tiny
global x;

function duplicar(num)
begin
    var temp;
    temp := num * 2;
    x := x + 1;
    return temp
end

begin
    var resultado;
    x := 5;
    resultado := call duplicar(x);
    write resultado;
    write x
end
```
Notas:
- Se declara `x` global.
- `duplicar` incrementa `x` (global) y retorna un valor basado en su parámetro.
- La llamada `call duplicar(x)` se integra como `factor` en una asignación.