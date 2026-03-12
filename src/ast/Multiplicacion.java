package ast;

public class Multiplicacion extends OperacionBinaria {

    public Multiplicacion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "*";
    }
    
    @Override
    public String get_llvm_op_code() {
        return "mul";
    }
}
