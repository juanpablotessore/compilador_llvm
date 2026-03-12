/****************************************************************************
 * En esta sección se puede incluir todo código que se copiará textualmente
 * al comienzo del archivo JAVA que contendrá la definición de la clase del 
 * analizador léxico.
 ****************************************************************************/
package lexer;
import parser.ParserSym;
import java.util.ArrayList;

/****************************************************************************
 * Las siguientes directivas afectan el comportamiento del analizador léxico:
 *
 *  - %class Nombre --> Nombre de la clase generada.
 *  - %type Nombre  --> Tipo retornado por yylex().
 *  - %line         --> Número de línea (this.yyline)
 *  - %column       --> Número de columna (this.yycolumn)
 *
 * Existen otras directivas adicionales descriptas en la documentación.
 ****************************************************************************/
%%

%public
%class Lexer
%implements java_cup.runtime.Scanner
%unicode
%type Token
%line
%column

%{
    /*************************************************************************
    * Variables de instancia y métodos auxiliares.
    *************************************************************************/

    public ArrayList<Token> tablaDeSimbolos = new ArrayList<>();

    private Token token(int ID) {
        return new Token(ID, this.yyline, this.yycolumn);
    }

    private Token token(int ID, Object valor) {
        return new Token(ID, this.yyline, this.yycolumn, valor);
    }

    /**
     * Requerido por java_cup.runtime.Scanner.
     * Simplemente delega en yylex() sin lógica extra.
     */
    public java_cup.runtime.Symbol next_token() throws java.io.IOException {
        return yylex();
    }
%}

FIN_DE_LINEA = \r | \n | \r\n

BLANCO       = {FIN_DE_LINEA} | [ \t\f]

ENTERO = [\d]+

%%

<YYINITIAL> {

    "SHOW"           { return token(ParserSym.SHOW); }
    "+"              { return token(ParserSym.MAS); }
    "-"              { return token(ParserSym.MENOS); }
    "*"              { return token(ParserSym.ASTERISCO); }
    "/"              { return token(ParserSym.BARRA); }
    "("              { return token(ParserSym.PARENTESIS_ABRE); }
    ")"              { return token(ParserSym.PARENTESIS_CIERRA); }
    {ENTERO}         { return token(ParserSym.CONSTANTE_ENTERA, Integer.parseInt(yytext())); }

    {BLANCO}         {}
}

[^]                  { throw new Error(String.format("Carácter no permitido: <%s> en línea %d, columna %d.", yytext(), yyline, yycolumn)); }


