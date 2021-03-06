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
package org.zollty.framework.core.support.xml.parse;

import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.CLASS_ATTRIBUTE;
import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.ID_ATTRIBUTE;
import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.NAME_ATTRIBUTE;
import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.PROPERTY_ELEMENT;
import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.REF_ATTRIBUTE;
import static org.zollty.framework.core.support.xml.parse.XmlNodeConstants.VALUE_ATTRIBUTE;

import java.lang.reflect.Method;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zollty.framework.core.support.BeanDefinition;
import org.zollty.framework.core.support.exception.BeanDefinitionParsingException;
import org.zollty.framework.core.support.xml.ManagedRef;
import org.zollty.framework.core.support.xml.ManagedValue;
import org.zollty.framework.core.support.xml.XmlBeanDefinition;
import org.zollty.framework.core.support.xml.XmlGenericBeanDefinition;
import org.zollty.framework.util.MvcReflectUtils;
import org.zollty.framework.util.MvcRuntimeException;
import org.zollty.framework.util.MvcUtils;
import org.zollty.framework.util.dom.Dom;
import org.zollty.log.LogFactory;
import org.zollty.log.Logger;

public class BeanNodeParser {

	private static Logger log = LogFactory.getLogger(BeanNodeParser.class);
	
	
	public static XmlBeanDefinition parse(Element ele, Dom dom, ClassLoader beanClassLoader) {
		XmlBeanDefinition xmlBeanDefinition = new XmlGenericBeanDefinition();
		
		// 获取所有property
		List<Element> properties = dom.elements(ele, PROPERTY_ELEMENT);

		// 迭代property列表
		if (properties != null) {
			for (Element property : properties) {
				String name = property.getAttribute(NAME_ATTRIBUTE);

				boolean hasValueAttribute = property.hasAttribute(VALUE_ATTRIBUTE);
				boolean hasRefAttribute = property.hasAttribute(REF_ATTRIBUTE);

				// 只能有一个子元素: ref, value, list, etc.
				NodeList nl = property.getChildNodes();
				Element subElement = null;
				for (int i = 0; i < nl.getLength(); ++i) {
					Node node = nl.item(i);
					if (node instanceof Element) {
						if (subElement != null) {
							error(name + " must not contain more than one sub-element");
						} else {
							subElement = (Element) node;
						}
					}
				}

				if (hasValueAttribute && hasRefAttribute
						|| ((hasValueAttribute || hasRefAttribute) && subElement != null)) {
					error(name + " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element");
				}

				if (hasValueAttribute) {
					// 普通赋值
					String value = property.getAttribute(VALUE_ATTRIBUTE);
					if ( MvcUtils.StringUtil.isBlank(value) ) {
						error(name + " contains empty 'value' attribute");
					}
					xmlBeanDefinition.getProperties().put(name, new ManagedValue(value));
				} else if (hasRefAttribute) {
					// 依赖其他bean
					String ref = property.getAttribute(REF_ATTRIBUTE);
					if ( MvcUtils.StringUtil.isBlank(ref) ) {
						error(name + " contains empty 'ref' attribute");
					}
					xmlBeanDefinition.getProperties().put(name, new ManagedRef(ref));
				} else if (subElement != null) {
					// 处理子元素
					Object subEle = XmlNodeStateMachine.getXmlBeanDefinition(subElement, dom, beanClassLoader);
					xmlBeanDefinition.getProperties().put(name, subEle);
				} else {
					error(name + " must specify a ref or value");
					return null;
				}
			}
		}
		
		// 获取基本属性
		String id = ele.getAttribute(ID_ATTRIBUTE);
		xmlBeanDefinition.setId(id);
		String className = ele.getAttribute(CLASS_ATTRIBUTE);
		
		if( className.indexOf("#")==-1 ){
			xmlBeanDefinition.setBeanType(BeanDefinition.CLASS_BEAN_TYPE);
			xmlBeanDefinition.setClassName(className);
			// 实例化对象
			Class<?> clazz = null;
			Object obj = null;
			try {
				clazz = beanClassLoader.loadClass(className);
				obj = clazz.newInstance();
			} catch (Exception e) {
				throw new MvcRuntimeException(e,"beanClassLoader.loadClass error!");
			}
			xmlBeanDefinition.setObject(obj);

			// 取得接口名称
			String[] names = MvcReflectUtils.getInterfaceNames(clazz);
			xmlBeanDefinition.setInterfaceNames(names);
			log.info("class ["+className+"] names size ["+names.length+"]");
			
		}
		
		else{
			xmlBeanDefinition.setBeanType(BeanDefinition.METHOD_BEAN_TYPE);
			
			String[] tempArray = className.split("#");
			if( tempArray.length!=2 ){
				error(id + " class attribute define error: " + className);
			}
			className = tempArray[0];
			String methodName = tempArray[1];
			// 
			xmlBeanDefinition.setMethodName(methodName);
			
			try {
				
				Class<?> hostClazz = beanClassLoader.loadClass(className);
				Object hostobj = hostClazz.newInstance();
				// 
				xmlBeanDefinition.setObject(hostobj);
				
				Method method = null;
				Method[] methods = hostClazz.getDeclaredMethods();
				for(Method m: methods){
					if( m.getName().equals(methodName) ){
						method = m;
					}
				}
				Class<?> clazz = method.getReturnType();
				//
				xmlBeanDefinition.setClassName(clazz.getName());
				// 取得接口名称
				String[] names = MvcReflectUtils.getInterfaceNames(clazz); //new String[]{};
				xmlBeanDefinition.setInterfaceNames(names);
				log.debug("class ["+className+"] names size ["+names.length+"]");
				
			}catch (Exception e) {
				log.error(e);
			}
		}
		return xmlBeanDefinition;
	}
	
	
	private static void error(String msg) {
		log.error(msg);
		throw new BeanDefinitionParsingException(msg);
	}
}
