package com.enn.extspringmvc.controller;

import com.enn.extspringmvc.annotation.ExtController;
import com.enn.extspringmvc.annotation.ExtRequestMapping;

@ExtController
@ExtRequestMapping("/ext")
public class ExtIndexController {

    @ExtRequestMapping("/test")
    public String test(){
          System.out.println("hhhhhhhh");
          return "index";
    }
}
