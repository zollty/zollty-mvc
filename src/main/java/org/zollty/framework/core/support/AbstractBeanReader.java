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
package org.zollty.framework.core.support;

import java.util.List;

import org.zollty.log.LogFactory;
import org.zollty.log.Logger;
import org.zollty.framework.core.support.exception.BeanDefinitionParsingException;

/**
 * @author zollty 
 * @since 2013-9-21
 */
abstract public class AbstractBeanReader implements BeanReader {
	private Logger log = LogFactory.getLogger(AbstractBeanReader.class);
	protected List<BeanDefinition> beanDefinitions;

	@Override
	public List<BeanDefinition> loadBeanDefinitions() {
		return beanDefinitions;
	}

	/**
	 * 处理异常
	 * 
	 * @param msg
	 *            异常信息
	 */
	protected void error(String msg) {
		log.error(msg);
		throw new BeanDefinitionParsingException(msg);
	}
	
}
