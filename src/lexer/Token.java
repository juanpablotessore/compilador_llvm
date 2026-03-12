package lexer;


import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import parser.ParserSym;

/**
 * Representa un token producido por el analizador léxico.
 * Extiende ComplexSymbol para ser compatible con java-cup.
 */
public class Token extends ComplexSymbol {

    public final String nombre;
    public final int linea;
    public final int columna;
    public final Object valor;

    public Token(int id, int linea, int columna) {
        this(id, linea, columna, null);
    }

    public Token(int id, int linea, int columna, Object valor) {
        super(ParserSym.terminalNames[id], id, valor);
        this.nombre  = ParserSym.terminalNames[id];
        this.linea   = linea;
        this.columna = columna;
        this.valor   = valor;
    }

    @Override
    public String toString() {
        String pos = " @ (L:" + linea + ", C:" + columna + ")";
        if (valor == null)
            return "[" + nombre + "]" + pos;
        else
            return "[" + nombre + "] -> (" + valor + ")" + pos;
    }
}
