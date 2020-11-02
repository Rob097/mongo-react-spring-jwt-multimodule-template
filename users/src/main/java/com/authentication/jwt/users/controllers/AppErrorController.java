package com.authentication.jwt.users.controllers;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {
	
	private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        //for brevity, only handling 404
        if (status != null) {
        	
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value() || statusCode == HttpStatus.UNAUTHORIZED.value()) {            	
                return "forward:/#404";
            }else {
            	System.out.println("STATUS: " + statusCode);
            }
        }
        return "error";
    }


    @Override
    public String getErrorPath() {
        return PATH;
    }
    
}
