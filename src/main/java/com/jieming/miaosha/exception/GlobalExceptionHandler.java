package com.jieming.miaosha.exception;

import com.jieming.miaosha.result.CodeMsg;
import com.jieming.miaosha.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/*全局异常处理器*/
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    /*拦截到被@RequestMapping上的Controller的异常就执行下面的方法*/
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){

        if(e instanceof  BindException){
            BindException ex = (BindException)e;
            List<ObjectError> allErrors = ex.getAllErrors();
            ObjectError error = allErrors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));

        }else if(e instanceof GlobalException){
            GlobalException ex = (GlobalException)e;
            return Result.error(ex.getCm());
        }else{
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
