package ast;

public class Resta extends OperacionBinaria {

    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "-";
    }
    
    @Override
    public String get_llvm_op_code() {
        return "sub";
    }
}