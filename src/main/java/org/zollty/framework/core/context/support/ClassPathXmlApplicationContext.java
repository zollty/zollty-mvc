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
package org.zollty.framework.core.context.support;

import java.util.List;

import org.zollty.framework.core.Const;
import org.zollty.framework.core.config.ConfigReader;
import org.zollty.framework.core.support.BeanDefinition;
import org.zollty.framework.core.support.xml.XmlBeanReader;
import org.zollty.log.LogFactory;
import org.zollty.log.Logger;

/**
 * @author zollty 
 * @since 2013-10-11
 */
public class ClassPathXmlApplicationContext extends AbstractApplicationContext{

	private Logger log;
	
	public ClassPathXmlApplicationContext(){
	    this(Const.DEFAULT_CONFIG_LOCATION, null);
	}
	
	public ClassPathXmlApplicationContext(ClassLoader beanClassLoader){
	    this(Const.DEFAULT_CONFIG_LOCATION, beanClassLoader);
	}
	
	public ClassPathXmlApplicationContext(String configLocation){
		this(configLocation, null);
	}

	public ClassPathXmlApplicationContext(String configLocation, ClassLoader beanClassLoader){
	    super(configLocation, beanClassLoader);
//		ConfigReader.getInstance().load(configLocation);
//		setBeanClassLoader(beanClassLoader);
//		refresh();
//		BeanFactoryHelper.setBeanFactory(this);
	    refresh();
	}
	
    private long beginTimeMs;

    @Override
    protected void doBeforeRefresh() {
        beginTimeMs = System.currentTimeMillis();
        log = LogFactory.getLogger(getClass());
        if (LogFactory.isDebugEnabled()) {
            log.debug("load {} ...", getClass().getSimpleName());
        }
        ConfigReader.getInstance().load(getConfigLocation(), getBeanClassLoader());
    }

    @Override
    protected void doAfterRefresh() {
        if (LogFactory.isDebugEnabled()) {
            log.debug("{} completed in {} ms.", getClass().getSimpleName(), (System.currentTimeMillis() - beginTimeMs));
        }
    }
	
	@Override
	protected List<BeanDefinition> loadBeanDefinitions() {
		List<BeanDefinition> list2 = new XmlBeanReader( getBeanClassLoader() ).loadBeanDefinitions();
		if (list2 != null) {
			log.debug("-- xml bean --");
			return list2;
		}
		return null;
	}

}
