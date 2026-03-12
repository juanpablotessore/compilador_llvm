import ast.Impresion;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;
import lexer.Lexer;
import parser.Parser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) throws Exception {
        final FileReader entrada = new FileReader("./src/input_1.txt");
        final Lexer lexico = new Lexer(entrada);
        SymbolFactory sf = new ComplexSymbolFactory();
        final Parser sintactico= new Parser(lexico, sf);
        final Impresion impresion = (Impresion) sintactico.parse().value;
     
        try {
            PrintWriter grafico = new PrintWriter(new FileWriter("arbol.dot"));
            grafico.println(impresion.graficar());
            grafico.close();
            Process processGrafico = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", "arbol.dot", "-o", "arbol.png"});
            BufferedReader readerGrafico = new BufferedReader(new InputStreamReader(processGrafico.getInputStream()));
            String lineGrafico;
            while ((lineGrafico = readerGrafico.readLine()) != null) {
                System.out.println(lineGrafico);
            }
            System.out.println("AST generado exitosamente.");
     
            //generar codigo IR para el LLVM
            PrintWriter ll = new PrintWriter(new FileWriter("programa.ll"));
            ll.println(impresion.generarCodigo());
            ll.close();
            System.out.println("Código generado");
        
            Process processObj = Runtime.getRuntime().exec(new String[]{"clang", "-c", "-o", "programa.o", "programa.ll"});
            BufferedReader readerObj = new BufferedReader(new InputStreamReader(processObj.getInputStream()));
            String lineObj;
            while ((lineObj = readerObj.readLine()) != null) {
                System.out.println(lineObj);
            }
            System.out.println("Archivo objeto generado");

            Process processExe = Runtime.getRuntime().exec(new String[]{"clang", "-o", "programa.exe", "programa.o"});
            BufferedReader readerExe = new BufferedReader(new InputStreamReader(processExe.getInputStream()));
            String lineExe;
            while ((lineExe = readerExe.readLine()) != null) {
                System.out.println(lineExe);
            }
            System.out.println("Ejecutable generado");

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
