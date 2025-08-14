### Análisis Léxico (JFlex) — Diseño, Tokens y Consideraciones

Este documento describe el análisis léxico del lenguaje Tiny extendido en este proyecto (`Compilador_2025`), su modelo de tokens, la relación con CUP y las decisiones de diseño aplicadas. Se basa en la especificación ubicada en `src/especificacion/lexico.flex` y en el escáner generado `ve.edu.unet.Lexico`.

- Referencias clave:
  - Archivo de especificación JFlex: `src/especificacion/lexico.flex`
  - Clase generada: `src/ve/edu/unet/Lexico.java`
  - Símbolos del parser: `src/ve/edu/unet/sym.java`

### Objetivos del léxico
- **Particionar** la entrada en una secuencia de tokens significativos para el parser CUP.
- **Normalizar** variantes de operadores lógicos (`and`/`&&`, `or`/`||`, `not`/`!`) a un conjunto uniforme de terminales.
- **Anotar valores léxicos** cuando aplica (por ejemplo, `ID` y `NUM`).
- **Ignorar** espacios en blanco y comentarios, preservando coordenadas `line/column` para diagnóstico.

### Conjunto de tokens terminales
Del archivo `.flex` se reconocen, entre otros, los siguientes terminales (ver `sym.java`):
- Palabras clave de control: `IF`, `THEN`, `ELSE`, `BEGIN`, `END`, `REPEAT`, `UNTIL`, `FOR`, `TO`, `STEP`.
- I/O y declaraciones: `READ`, `WRITE`, `GLOBAL`, `VAR`, `ARRAY`.
- Funciones: `FUNCTION`, `RETURN`, `CALL`.
- Operadores relacionales: `LT`, `LE`, `GT`, `GE`, `EQ`, `NE`.
- Operadores aritméticos: `PLUS`, `MINUS`, `TIMES`, `OVER`, `MOD`, `POW`.
- Lógicos: `AND`, `OR`, `NOT` (también mapeados desde `&&`, `||`, `!`).
- Puntuación: `ASSIGN` (`:=`), `LPAREN`, `RPAREN`, `LBRACKET`, `RBRACKET`, `SEMI`, `COMMA`, `COLON`.
- Literales y nombres: `NUM` (valor léxico String con dígitos), `ID` (valor léxico String).

Observación: `ARRAY`, `LBRACKET` y `RBRACKET` permiten la sintaxis de vectores, p. ej. `var a: array[10];` y el acceso `a[i]`.

### Reglas léxicas relevantes
- Identificadores: `identificador = [a-zA-Z]([a-zA-Z]|[0-9]|_)*` devuelven `ID` con lexema.
- Números: `numero = [0-9]+` devuelven `NUM` con lexema decimal.
- Espacios en blanco y saltos de línea se ignoran, actualizando contadores `lineanum`, `%line`, `%column`.
- Comentarios:
  - De línea: `// ...` ignorados.
  - De bloque-ligero: `{ ... }` (no anidados) ignorados.

### Interoperabilidad con CUP
- El escáner usa `%cup` y construye símbolos con un `SymbolFactory` para transportar nombre del token y, si corresponde, su valor.
- La creación de símbolos se hace mediante `sf.newSymbol("TOKEN", sym.TOKEN, value?)`.
- `Lexico` incluye constructor `Lexico(Reader r, SymbolFactory sf)` usado por el `parser` generado.

### Normalización y ambigüedades
- Operadores lógicos escritos en forma textual o simbólica se **normalizan** a los mismos terminales (`AND`, `OR`, `NOT`).
- `NE` se reconoce desde `<>` y `!=` por compatibilidad.
- `POW` se reconoce como `**` y se implementa en el backend con un bucle iterativo.

### Errores léxicos
- Cualquier carácter no reconocido desencadena: `Caracter Ilegal encontrado en analisis lexico: <char>`.
- La estrategia recomendada es abortar o continuar consumiendo para permitir al parser reportar más errores; el escáner actual imprime por `stderr`.

### Vectores y funciones (enfoque léxico)
- Vectores: el léxico expone `ARRAY`, `LBRACKET`, `RBRACKET`, `COMMA`, `COLON`, `NUM`, `ID`. No valida tamaños; sólo tokeniza.
- Funciones: reconoce `FUNCTION`, `CALL`, `RETURN` y delimitadores `LPAREN`, `RPAREN`, `SEMI`. El vínculo semántico (nombres, parámetros) se valida más adelante.

### Buenas prácticas aplicadas
- Uso de `%line` y `%column` para diagnósticos.
- Ignorar comentarios y espacios para simplificar la gramática.
- Mantener el conjunto de tokens mínimo y consistente con `sintactico.cup`.

### Extensiones posibles
- Soporte de literales negativos como un solo token (hoy se parsea como `MINUS NUM`).
- Cadenas de texto y caracteres.
- Comentarios anidados o estilo `/* ... */`.
- Reconocimiento de múltiples parámetros en funciones (hoy la gramática admite solo uno).