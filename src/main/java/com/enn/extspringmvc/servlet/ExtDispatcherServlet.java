package com.enn.extspringmvc.servlet;

import com.enn.extspringmvc.annotation.ExtController;
import com.enn.extspringmvc.annotation.ExtRequestMapping;
import com.enn.extspringmvc.utils.ClassUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@WebServlet(urlPatterns = {"/"},loadOnStartup = 1,name = "dispatcher")
public class ExtDispatcherServlet extends HttpServlet {

    private ConcurrentHashMap<String,Object> springmvcBeans=new ConcurrentHashMap<String, Object>();
    private ConcurrentHashMap<String,Object> urlBeans=new ConcurrentHashMap<String, Object>();
    private ConcurrentHashMap<String,String> urlMethods=new ConcurrentHashMap<String, String>();
    @Override
    public void init() throws ServletException {
        super.init();
        List<Class<?>> classInfos = ClassUtil.getClasses("com.enn.extspringmvc.controller");
        try {
            findMVCBeans(classInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        handleMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestURI = req.getRequestURI();
        if(StringUtils.isEmpty(requestURI)){
             return;
        }
        Object obj = urlBeans.get(requestURI);
        if(obj==null){
            resp.getWriter().println("404 not found url");
            return;
        }
        String methodName = urlMethods.get(requestURI);
        try {
            String pageName  =(String)methodInvoke(obj, methodName);
            viewResolver(pageName,req,resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void viewResolver(String pageName,HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String prefix="/";
        String suffix=".jsp";
        req.getRequestDispatcher(prefix+pageName+suffix).forward(req,resp);
    }

    public  void  findMVCBeans(List<Class<?>> classes) throws Exception {
        for (Class<?>  classInfo:classes){
            ExtController declaredAnnotation = classInfo.getDeclaredAnnotation(ExtController.class);
            if(declaredAnnotation!=null){
                String beanId = ClassUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
                Object obj = ClassUtil.newInstance(classInfo);
                springmvcBeans.put(beanId,obj);
            }
        }
    }

    public void handleMapping(){
        for(Map.Entry<String,Object> mvcBean :springmvcBeans.entrySet()){
            Object obj = mvcBean.getValue();
            String beanId = mvcBean.getKey();
            Class<?> classInfo = obj.getClass();
            ExtRequestMapping extClassUrl = classInfo.getDeclaredAnnotation(ExtRequestMapping.class);
            String baseUrl="";
            if(extClassUrl!=null){
                baseUrl=extClassUrl.value();
            }
            Method[] methods = classInfo.getDeclaredMethods();
            for(Method method:methods){
                ExtRequestMapping extMethodUrl = method.getDeclaredAnnotation(ExtRequestMapping.class);
                if(extMethodUrl!=null){
                   String methodUrl=baseUrl+ extMethodUrl.value();
                     urlBeans.put(methodUrl,obj);
                     urlMethods.put(methodUrl,method.getName());
                }
            }
        }
    }

    private Object methodInvoke(Object obj,String methodName) throws Exception {
        Class<?> classInfo = obj.getClass();
        Method method = classInfo.getMethod(methodName);
        if(method!=null){
            Object result = method.invoke(obj);
            return result;
        }
        return null;
    }


}
