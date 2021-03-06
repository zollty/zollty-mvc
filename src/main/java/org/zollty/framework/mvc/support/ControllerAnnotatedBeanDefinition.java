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
 * Create by Zollty Tsou [http://blog.csdn.net/zollty (or GitHub)]
 */
package org.zollty.framework.mvc.support;

import java.lang.reflect.Method;
import java.util.List;

import org.zollty.framework.core.support.annotation.AnnotatedBeanDefinition;

/**
 * @author zollty 
 * @since 2013-9-21
 */
public class ControllerAnnotatedBeanDefinition extends AnnotatedBeanDefinition
		implements ControllerBeanDefinition {

	private List<Method> reqMethods;


	@Override
	public List<Method> getReqMethods() {
		return reqMethods;
	}
	
	@Override
	public void setReqMethods(List<Method> reqMethods) {
		this.reqMethods = reqMethods;
	}

}
