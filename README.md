# Compilador - Analizador Léxico + Sintáctico + AST + Generación de Código LLVM IR (UNNOBA 2026)

Analizador léxico y sintáctico construido con **JFlex** y **java-cup**, con generación de un Árbol de Sintaxis Abstracta (AST), exportación a imagen mediante Graphviz, y generación de código intermedio en formato **LLVM IR** compilable con Clang.

## Estructura del proyecto

```
compilador_llvm/
├── pom.xml
├── arbol.dot                       ← Archivo DOT generado (salida)
├── arbol.png                       ← Imagen del AST generada (salida)
├── programa.ll                     ← Código LLVM IR generado (salida)
├── programa.o                      ← Archivo objeto generado por Clang (salida)
├── programa.exe                    ← Ejecutable generado por Clang (salida)
└── src/
    ├── Generador.java              ← Regenera Lexer.java + Parser.java desde las fuentes
    ├── Main.java                   ← Punto de entrada principal: genera AST, exporta DOT/PNG y compila LLVM IR
    ├── input_1.txt                 ← Archivo de prueba (contiene: SHOW (15+3)*20/3-21)
    ├── ast/
    │   ├── Nodo.java               ← Clase base abstracta para todos los nodos del AST
    │   ├── Expresion.java          ← Clase abstracta para expresiones (incluye ir_ref para LLVM)
    │   ├── OperacionBinaria.java   ← Clase abstracta para operaciones de dos operandos
    │   ├── Impresion.java          ← Nodo raíz: representa la sentencia SHOW; genera el encabezado LLVM
    │   ├── Constante.java          ← Nodo hoja: constante entera
    │   ├── Suma.java               ← Nodo para el operador +  (opcode: add)
    │   ├── Resta.java              ← Nodo para el operador -  (opcode: sub)
    │   ├── Multiplicacion.java     ← Nodo para el operador *  (opcode: mul)
    │   └── Division.java           ← Nodo para el operador /  (opcode: sdiv)
    ├── lexer/
    │   ├── lexico.flex             ← Definición del léxico (fuente JFlex)
    │   ├── Lexer.java              ← Léxico generado por JFlex  ⟩ generados,
    │   ├── Token.java              ← Clase token                ⟩ se pueden
    │   └── Main_lexer.java         ← Punto de entrada: solo análisis léxico
    ├── llvm/
    │   └── CodeGeneratorHelper.java ← Utilidad para generar nombres únicos de registros LLVM (%ptro.N, @gb.N, tag.N)
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
- Clang (comando `clang`) para compilar el código LLVM IR a ejecutable nativo

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

## Ejecutar el programa principal (AST + LLVM IR)

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Lee `./src/input_1.txt`, construye el AST, escribe `arbol.dot` y genera `arbol.png`
invocando Graphviz. Luego genera `programa.ll` (código LLVM IR), lo compila con Clang
a `programa.o` y finalmente enlaza el ejecutable `programa.exe`.

Si Graphviz o Clang no están instalados, los archivos intermedios (`.dot`, `.ll`) se
producen igualmente y pueden procesarse con las herramientas correspondientes.

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

- `Nodo` — clase base. Provee `getId()` (basado en `hashCode`), `getEtiqueta()`, el método `graficar(String idPadre)` que emite el fragmento DOT del nodo, y el método abstracto `generarCodigo()`.
- `Expresion` — subclase abstracta de `Nodo` para cualquier expresión evaluable. Almacena `ir_ref`, el nombre del registro LLVM asignado al resultado de la expresión.
- `OperacionBinaria` — subclase abstracta de `Expresion` para operadores binarios. Almacena operandos `izquierda` y `derecha`, y su `graficar()` los recorre recursivamente. Su `generarCodigo()` emite el código de ambos operandos y luego la instrucción LLVM correspondiente.
- `Impresion` — nodo raíz que representa la sentencia `SHOW`. Su método `graficar()` (sin parámetros) inicia el recorrido y envuelve el resultado en `graph G { ... }`. Su `generarCodigo()` produce el módulo LLVM IR completo, incluyendo encabezados, la función `@main` y la llamada a `@printf`.

Los nodos hoja y de operación concretos son: `Constante`, `Suma`, `Resta`, `Multiplicacion` y `Division`.

Para la entrada `SHOW (15+3)*20/3-21` el árbol generado es:

```
Impresion
└── - (Resta)
    ├── / (Division)
    │   ├── * (Multiplicacion)
    │   │   ├── + (Suma)
    │   │   │   ├── Const 15
    │   │   │   └── Const 3
    │   │   └── Const 20
    │   └── Const 3
    └── Const 21
```

## Generación de código LLVM IR

Cada nodo del AST implementa `generarCodigo()`, que emite instrucciones LLVM IR en orden postorden. El paquete `llvm` provee la clase utilitaria `CodeGeneratorHelper`, que mantiene un contador global para asignar nombres únicos a los registros virtuales.

| Método                        | Resultado          | Ejemplo                     |
|-------------------------------|--------------------|-----------------------------|
| `getNewPointer()`             | `%ptro.N`          | `%ptro.1`, `%ptro.2`, …     |
| `getNewGlobalPointer()`       | `@gb.N`            | `@gb.1`, …                  |
| `getNewTag()`                 | `tag.N`            | `tag.1`, …                  |

El módulo LLVM IR generado para `SHOW (15+3)*20/3-21` es equivalente a la secuencia:

```llvm
%ptro.1 = add  i32 0, 15
%ptro.2 = add  i32 0, 3
%ptro.3 = add  i32 %ptro.1, %ptro.2   ; 15+3 = 18
%ptro.4 = add  i32 0, 20
%ptro.5 = mul  i32 %ptro.3, %ptro.4   ; 18*20 = 360
%ptro.6 = add  i32 0, 3
%ptro.7 = sdiv i32 %ptro.5, %ptro.6   ; 360/3 = 120
%ptro.8 = add  i32 0, 21
%ptro.9 = sub  i32 %ptro.7, %ptro.8   ; 120-21 = 99
; llamada a printf con el resultado final
```

El archivo `programa.ll` completo se escribe en el directorio raíz del proyecto y luego se compila con:

```bash
clang -c -o programa.o programa.ll
clang -o programa.exe programa.o
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
