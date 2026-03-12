package ast;

import llvm.CodeGeneratorHelper;

public abstract class OperacionBinaria extends Expresion {
    protected final Expresion izquierda;

    protected final Expresion derecha;
    

    public OperacionBinaria(Expresion izquierda, Expresion derecha) {
        this.izquierda = izquierda;
        this.derecha = derecha;
    }
    
    public Expresion getIzquierda() {
        return izquierda;
    }

    public Expresion getDerecha() {
        return derecha;
    }
    
    @Override
    protected String getEtiqueta() {
        return String.format("%s", this.getNombreOperacion());
    }

    protected abstract String getNombreOperacion();

    @Override
    protected String graficar(String idPadre) {
        final String miId = this.getId();
        return super.graficar(idPadre) +
                izquierda.graficar(miId) +
                derecha.graficar(miId);
    }
    
    public abstract String get_llvm_op_code();
    
    @Override
    public String generarCodigo(){
        StringBuilder resultado = new StringBuilder();        
        resultado.append(this.getIzquierda().generarCodigo());
        resultado.append(this.getDerecha().generarCodigo());
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("%1$s = %2$s i32 %3$s, %4$s\n", this.getIr_ref(), 
                this.get_llvm_op_code(), this.getIzquierda().getIr_ref(), 
                this.getDerecha().getIr_ref()));
        return resultado.toString();
    }
}
