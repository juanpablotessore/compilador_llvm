# Compilador - Analizador Léxico + Sintáctico + AST (UNNOBA 2026)

Analizador léxico y sintáctico de ejemplo construido con **JFlex** y **java-cup**, con generación de un Árbol de Sintaxis Abstracta (AST) y exportación a imagen mediante Graphviz.

## Estructura del proyecto

```
compilador_ast/
├── pom.xml
├── arbol.dot                       ← Archivo DOT generado (salida)
├── arbol.png                       ← Imagen del AST generada (salida)
└── src/
    ├── Generador.java              ← Regenera Lexer.java + Parser.java desde las fuentes
    ├── Main.java                   ← Punto de entrada principal: genera el AST y lo exporta
    ├── input_1.txt                 ← Archivo de prueba (contiene: SHOW(5+28*3))
    ├── ast/
    │   ├── Nodo.java               ← Clase base abstracta para todos los nodos del AST
    │   ├── Expresion.java          ← Clase abstracta para expresiones
    │   ├── OperacionBinaria.java   ← Clase abstracta para operaciones de dos operandos
    │   ├── Impresion.java          ← Nodo raíz: representa la sentencia SHOW
    │   ├── Constante.java          ← Nodo hoja: constante entera
    │   ├── Suma.java               ← Nodo para el operador +
    │   ├── Resta.java              ← Nodo para el operador -
    │   ├── Multiplicacion.java     ← Nodo para el operador *
    │   └── Division.java           ← Nodo para el operador /
    ├── lexer/
    │   ├── lexico.flex             ← Definición del léxico (fuente JFlex)
    │   ├── Lexer.java              ← Léxico generado por JFlex  ⟩ generados,
    │   ├── Token.java              ← Clase token                ⟩ se pueden
    │   └── Main_lexer.java         ← Punto de entrada: solo análisis léxico
    └── parser/
        ├── parser.cup              ← Definición de la gramática (fuente java-cup)
        ├── ParserSym.java          ← Constantes de terminales   ⟩ regenerar
        ├── Parser.java             ← Parser generado por CUP    ⟩ ejecutando
        └── Main_parser.java        ← Punto de entrada: análisis léxico + sintáctico
```

> **Nota:** `Lexer.java`, `Parser.java` y `ParserSym.java` son archivos
> **generados**. Si se modifican `lexico.flex` o `parser.cup`, deben regenerarse
> ejecutando `Generador.java`.

## Prerrequisitos

- Java 21 o superior
- Maven 3.6 o superior
- Graphviz (comando `dot`) para generar la imagen del AST

## Primer uso: regenerar el léxico y el parser

Si se modificaron `lexico.flex` o `parser.cup`:

```bash
mvn exec:java -Dexec.mainClass="Generador"
```

Esto produce `Lexer.java`, `Parser.java` y `ParserSym.java`.

## Compilar

```bash
mvn clean compile
```

## Ejecutar el programa principal (AST)

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Lee `./src/input_1.txt`, construye el AST, escribe `arbol.dot` y genera `arbol.png`
invocando Graphviz. Si Graphviz no está instalado, el archivo `.dot` igual se produce
y puede visualizarse con cualquier herramienta compatible.

## Ejecutar el analizador léxico

```bash
mvn exec:java -Dexec.mainClass="lexer.Main_lexer"
```

Al ejecutar, el programa pregunta:

```
=== Analizador Léxico ===
¿Desde dónde desea leer?
  1 - Desde consola
  2 - Desde archivo (./src/input_1.txt)
Ingrese su opción:
```

- **Opción 1 (consola):** escribí tokens línea a línea. Ingresá `FIN` para terminar.
- **Opción 2 (archivo):** lee y muestra todos los tokens de `input_1.txt`.

## Ejecutar el analizador sintáctico

```bash
mvn exec:java -Dexec.mainClass="parser.Main_parser"
```

Lee directamente desde `./src/input_1.txt` y aplica las reglas de `parser.cup`
sobre la secuencia de tokens, imprimiendo los errores si los hubiera.

## Árbol de Sintaxis Abstracta (AST)

El paquete `ast` implementa el AST mediante una jerarquía de clases:

- `Nodo` — clase base. Provee `getId()` (basado en `hashCode`), `getEtiqueta()` y el método `graficar(String idPadre)` que emite el fragmento DOT del nodo.
- `Expresion` — subclase abstracta de `Nodo` para cualquier expresión evaluable.
- `OperacionBinaria` — subclase abstracta de `Expresion` para operadores binarios. Almacena operandos `izquierda` y `derecha`, y su `graficar()` los recorre recursivamente.
- `Impresion` — nodo raíz que representa la sentencia `SHOW`. Su método `graficar()` (sin parámetros) inicia el recorrido y envuelve el resultado en `graph G { ... }`.

Los nodos hoja y de operación concretos son: `Constante`, `Suma`, `Resta`, `Multiplicacion` y `Division`.

Para la entrada `SHOW(5+28*3)` el árbol generado es:

```
Impresion
└── + (Suma)
    ├── Const 5
    └── * (Multiplicacion)
        ├── Const 28
        └── Const 3
```

## Diseño del léxico (`lexico.flex`)

El léxico usa `%implements java_cup.runtime.Scanner` y `%type Token`.
El método `next_token()` —requerido por la interfaz `Scanner`— simplemente
delega en `yylex()`, sin lógica adicional:

```java
public java_cup.runtime.Symbol next_token() throws java.io.IOException {
    return yylex();
}
```

## Clase Token

`Token` extiende `ComplexSymbol` (de java-cup) y resuelve su número de terminal
buscando el nombre en el array `ParserSym.terminalNames`. Almacena nombre, línea,
columna y valor léxico.

## Tokens reconocidos

| Token               | Descripción              |
|---------------------|--------------------------|
| `SHOW`              | Palabra reservada `SHOW` |
| `MAS`               | `+`                      |
| `MENOS`             | `-`                      |
| `ASTERISCO`         | `*`                      |
| `BARRA`             | `/`                      |
| `PARENTESIS_ABRE`   | `(`                      |
| `PARENTESIS_CIERRA` | `)`                      |
| `CONSTANTE_ENTERA`  | Constante entera (`42`)  |

## Gramática (`parser.cup`)

La gramática reconoce una sentencia `SHOW` seguida de una expresión aritmética.
La precedencia se resuelve estructuralmente mediante la separación en no-terminales:

```
impresion ::= SHOW expresion

expresion ::= expresion MAS termino
            | expresion MENOS termino
            | termino

termino   ::= termino ASTERISCO factor
            | termino BARRA factor
            | factor

factor    ::= CONSTANTE_ENTERA
            | PARENTESIS_ABRE expresion PARENTESIS_CIERRA
```

`MAS`/`MENOS` tienen menor precedencia que `ASTERISCO`/`BARRA`, y la asociatividad
es izquierda en ambos casos, garantizada por la recursión a izquierda de las reglas.
