/* 
 * Copyright (C) 2012-2014 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * Zollty Framework MVC Source Code - Since v1.0
 * Author(s): 
 * Zollty Tsou (zolltytsou@gmail.com, http://blog.zollty.com)
 */
package org.zollty.framework.mvc.handler.support;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zollty.framework.mvc.View;
import org.zollty.framework.mvc.handler.HandlerChain;
import org.zollty.framework.mvc.handler.WebHandler;
import org.zollty.framework.mvc.support.BasicParamMetaInfo;
import org.zollty.framework.mvc.support.ControllerMetaInfo;
import org.zollty.framework.mvc.support.ParamMetaInfo;
import org.zollty.framework.util.MvcConvertUtils;
import org.zollty.framework.util.MvcUtils;

/**
 * @author zollty 
 * @since 2013-9-16
 */
public class ControllerHandler implements WebHandler {

    private final ControllerMetaInfo controller;
    private Map<String, String> paramsMap;
	
    public ControllerHandler(ControllerMetaInfo controller) {
        this.controller = controller;
    }
    
    public ControllerHandler(ControllerMetaInfo controller, Map<String, String> paramsMap) {
        this.controller = controller;
        this.paramsMap = paramsMap;
    }
	
	@Override
	public View invoke(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) {
	    // modified by ZOLLTY 2014-11-19 - 'notAllowMethod' request would never reached this step.
//		if(!controller.allowMethod(request.getMethod())) {
//			return notAllowMethodResponse(request, response);
//		}
		
		Object[] p = getParams(request, response);
		return controller.invokeMethod(p, request, response);
	}
	
	/**
	 * controller方法参数注入
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Object[] getParams(HttpServletRequest request, HttpServletResponse response) {
		ControllerMetaInfo info = controller;
		ParamMetaInfo[] paramMetaInfos = info.getParamMetaInfos();
		BasicParamMetaInfo[] bParamMetas = info.getbParamMetas();
		
		byte[] paramType = info.getParamType();
		Object[] p = new Object[paramType.length];

		for (int i = 0; i < p.length; i++) {
			switch (paramType[i]) {
			case BasicParamMetaInfo.REQUEST:
				p[i] = request;
				break;
			case BasicParamMetaInfo.RESPONSE:
				p[i] = response;
				break;
			case BasicParamMetaInfo.HTTP_BEAN:
				// 请求参数封装到javabean
				Enumeration<String> enumeration = request.getParameterNames();
				ParamMetaInfo paramMetaInfo = paramMetaInfos[i];
				p[i] = paramMetaInfo.newParamInstance();

				// 把http参数赋值给参数对象
				while (enumeration.hasMoreElements()) {
					String httpParamName = enumeration.nextElement();
					String paramValue = request.getParameter(httpParamName);
					paramMetaInfo.setParam(p[i], httpParamName, paramValue);
				}
				if ( MvcUtils.StringUtil.isNotBlank(paramMetaInfo.getAttribute()) ) {
					request.setAttribute(paramMetaInfo.getAttribute(), p[i]);
				}
				break;
			case BasicParamMetaInfo.URI_PARAM:
			    BasicParamMetaInfo pb = bParamMetas[i];
			    String sval = this.getParamsMap().get(pb.getAttribute());
			    p[i] = MvcConvertUtils.convert(sval, pb.getParamClass());
			    if( pb.isSetAttr() ) {
			        request.setAttribute(pb.getAttribute(), p[i]); //默认行为：注入到request中
			    }
				break;
			case BasicParamMetaInfo.HTTP_SIMPLE_VALUE:
				BasicParamMetaInfo b = bParamMetas[i];
				p[i] = b.getValue(request);
				if( b.isSetAttr() ) {
				    request.setAttribute(b.getAttribute(), p[i]); //默认行为：注入到request中
				}
				break;
			}
		}
		return p;
	}
	
	public ControllerMetaInfo getController() {
        return controller;
    }
	
	public Map<String, String> getParamsMap() {
        return paramsMap;
    }
	
	@Override
	public String toString() {
	    return "[controller=" + controller + ", params=" + getParamsMap() + "]";
	}
	
//  private View notAllowMethodResponse(HttpServletRequest request, HttpServletResponse response) {
//      String allowMethod = controller.getAllowMethod();
//      response.setHeader("Allow", allowMethod);
//      return new ErrorHandler(null, "Only support " + allowMethod + " method", 
//          HttpServletResponse.SC_METHOD_NOT_ALLOWED).doErrorPage(request, response);
//  }

}
