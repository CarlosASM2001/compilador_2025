### Generación de Código (TM) — Arquitectura, Convenciones y Mapeo de Nodos

Este documento detalla el backend que emite código para la Tiny Machine (TM) en `Compilador_2025`. Se describen los registros, la organización de memoria, la pseudo-pila temporal, el manejo de funciones (llamadas y retorno), vectores y el mapeo nodo-a-instrucciones.

- Referencias clave:
  - Emisor y utilidades: `src/ve/edu/unet/UtGen.java`
  - Generador principal: `src/ve/edu/unet/Generador.java`
  - Tabla de símbolos: `src/ve/edu/unet/TablaSimbolos.java`
  - Nodos AST: `src/ve/edu/unet/nodosAST/*.java`

### Modelo de ejecución en TM
Registros y convenciones (`UtGen`):
- `PC` (7): program counter.
- `MP` (6): tope de memoria; se usa como pila de temporales (push/pop via `ST`/`LD` con desplazamientos negativos y `desplazamientoTmp`).
- `GP` (5): base de datos globales; todas las variables (y parámetros) residen en direcciones relativas a `GP`.
- `AC` (0) y `AC1` (1): acumuladores.
- `DESP` (2): registro auxiliar para calcular direcciones efectivas de vectores.

Preludio estándar (`generarPreludioEstandar`): inicializa `MP` y limpia la localidad 0. El archivo de salida se configura con `UtGen.setOutputFile`.

### Organización de memoria
- La `TablaSimbolos` asigna direcciones lineales a identificadores conforme se descubren en el AST (modelo plano sin ámbitos).
- Vectores: `NodoDeclaracion.longitud > 1` reserva un bloque contiguo de direcciones. El nombre se refiere a la dirección base.

### Pila de temporales
- `desplazamientoTmp` es un desplazamiento descendente desde `MP`. Patrón típico en operaciones binarias:
  1) Generar operando izquierdo (resultado en `AC`).
  2) `ST AC, desplazamientoTmp--(MP)` empuja en pila temporal.
  3) Generar operando derecho (en `AC`).
  4) `LD AC1, ++desplazamientoTmp(MP)` recupera el izquierdo en `AC1`.
  5) Aplicar operación en `AC` con `AC1`.

### Mapeo nodo-a-TM
- `NodoValor`: `LDC AC, const`.
- `NodoIdentificador`:
  - Escalar: `LD AC, dir(GP)`.
  - Indexado: `LDC DESP, base; [gen índice]; ADD DESP, AC, DESP; LD AC, 0(DESP)`.
- `NodoAsignacion`:
  - Escalar: [gen exp] → `ST AC, dir(GP)`.
  - Indexada: `LDC DESP, base; [gen desp]; ADD DESP, AC, DESP; [gen exp]; ST AC, 0(DESP)`.
- `NodoEscribir`: [gen exp]; `OUT AC`.
- `NodoLeer`: `IN AC`; luego almacenar a escalar o indexado como en asignación.
- `NodoOperacion`:
  - Aritméticas: `ADD/SUB/MUL/DIV`.
  - `MOD`: resta repetida (apoya con saltos) para resultado no negativo.
  - Relacionales: patrón `SUB` + salto condicional para producir booleano 0/1.
  - Lógicas: `AND` como `MUL`; `OR` como `ADD` seguido de normalización a 0/1; `NOT` con comparación cero.
  - `POW`: bucle iterativo: guarda base/exp/result en temporales, decrementa exponente, multiplica acumulativamente.
- `NodoIf`: evalúa prueba; reserva salto condicional a `else`; genera `then`; salta al final; backpatch de ambos saltos.
- `NodoRepeat`: etiqueta de inicio, genera cuerpo, evalúa prueba, `JEQ` a inicio.
- `NodoFor`: genera inicio; prueba → salto al final; cuerpo; iteración; salto al inicio; backpatch al final.

### Funciones: declaración, llamada y retorno
Modelo didáctico, sin stack de activación ni cambio de ámbito. La generación realiza “inlining estructurado” del cuerpo con backpatch de retornos:
- Registro de funciones: al visitar `NodoFuncion`, se almacena en un mapa `nombre → NodoFuncion`.
- Llamada (`NodoLlamada`):
  - Si hay argumento y la función define parámetro, se evalúa el argumento en `AC` y se almacena en la dirección del parámetro (`ST AC, dirParam(GP)`).
  - Se empuja una lista para recopilar “slots” de saltos de retorno.
  - Se genera el cuerpo de la función inline.
  - Se emite un salto a la salida y se “backpatchean” todos los `return` a esa salida.
- Retorno (`NodoReturn`):
  - Evalúa la expresión en `AC`.
  - Emite un `emitirSalto(1)` y guarda ese slot para luego conectarlo con un `LDA PC, salida`.

Implicaciones:
- No hay variables locales verdaderas; el parámetro y los temporales viven en el mismo espacio global.
- Reentrancia y recursión no están soportadas con este esquema.

### Vectores: direccionamiento efectivo
El acceso `a[i]` se implementa como:
1) Obtener `base = dir(a)` desde la TS.
2) `LDC DESP, base`.
3) Generar `i` → `AC`.
4) `ADD DESP, AC, DESP` para producir la dirección efectiva.
5) Cargar/almacenar con desplazamiento 0 sobre `DESP`.

### Entradas y salidas
- `IN` lee un entero a `AC`.
- `OUT` imprime `AC`.

### Preludio y cierre
- `HALT` al final del programa principal.
- Comentarios opcionales con `UtGen.debug = true` para inspeccionar el flujo generado.

### Limitaciones y mejoras posibles
- Ausencia de pila de activación real: sin frames, sin retorno de direcciones, sin llamadas anidadas seguras.
- Ámbitos: una sola `TablaSimbolos` global; mejorar con pila de tablas y offsets por función/bloque.
- Pasaje de múltiples parámetros y valores de retorno compuestos.
- Tipado y verificación semántica (hoy todo es entero).
- Optimización: eliminación de temporales, cortocircuito lógico real, exponenciación por cuadrados, etc.

### Ejemplo de flujo (resumen)
Para `resultado := call duplicar(x);`:
- Carga de argumento: `LD AC, dir(x)(GP)`; `ST AC, dir(num)(GP)`.
- Generación inline del cuerpo de `duplicar` (asignaciones, suma a `x`, `return temp`).
- El `return` deja el valor en `AC` y salta a la salida de la llamada; `resultado` almacena `AC` en su dirección global.