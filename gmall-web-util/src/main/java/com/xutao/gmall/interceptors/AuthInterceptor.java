package com.xutao.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.xutao.gmall.annotations.LoginRequired;
import com.xutao.gmall.util.CookieUtil;
import com.xutao.utils.HttpclientUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器拦截未登录
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        //拦截代码
        //判断被拦截的请求的访问的方法的注解(是否时需要拦截的)
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        //StringBuffer url = request.getRequestURL();
        //System.out.println(url);

        //是否拦截
        if(methodAnnotation==null){
            return true;
        }
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if(StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if(StringUtils.isNotBlank(newToken)){
            token = newToken;
        }
        //是否必须登录
        /*获得该请求是否必须登录成功
         *返回为true时，必须登录成功
         * 返回为false时，即使登录不成功也可以访问
         */
        boolean logginSuccess = methodAnnotation.logginSuccess();

        //调用认证中心进行认证
        String success = "fail";
        Map<String,String> successMap = new HashMap<>();
        if(StringUtils.isNotBlank(token)){
            String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }
            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);
            successMap = JSON.parseObject(successJson,Map.class);
            success = successMap.get("status");
        }
        if(logginSuccess){
            //必须登录成功才能使用
            if(!success.equals("success")){
                //如果认证中心未认证通过，则
                //重定向到passport重新登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);
                return false;
            }
            //需要将token携带用户信息写入
            request.setAttribute("memberId",successMap.get("memberId"));
            request.setAttribute("nickname",successMap.get("nickname"));
            session.setAttribute("memberId",successMap.get("memberId"));
            session.setAttribute("nickname",successMap.get("nickname"));
            //验证通过，需要覆盖cookie中的token
            if(StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }
        }else{
            //没有登录成功也能使用吗，但需要验证
            if(success.equals("success")){
                //需要将token携带的用户信息写入
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickname",successMap.get("nickname"));
                session.setAttribute("memberId",successMap.get("memberId"));
                session.setAttribute("nickname",successMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }
        return true;
    }
}
