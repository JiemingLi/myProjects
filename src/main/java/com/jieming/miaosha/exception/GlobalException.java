package com.jieming.miaosha.exception;

import com.jieming.miaosha.result.CodeMsg;

/*全局的异常包装*/
public class GlobalException extends RuntimeException {

    private CodeMsg cm;

    public GlobalException(CodeMsg cm){
        super();
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }

    public void setCm(CodeMsg cm) {
        this.cm = cm;
    }
}
