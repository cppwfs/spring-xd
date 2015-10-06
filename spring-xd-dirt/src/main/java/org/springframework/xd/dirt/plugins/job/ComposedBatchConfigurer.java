/*
 *
 *  * Copyright 2015 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package org.springframework.xd.dirt.plugins.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.xd.dirt.module.support.ModuleDefinitionService;
import org.springframework.xd.module.ModuleType;

/**
 * @author Glenn Renfro
 */
@Component
public class ComposedBatchConfigurer {
	
	public static final String MODULE_SUFFIX = "_COMPOSED";
	
	private static String convertDefinitionToXML(String definition){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<beans xmlns=\"http://www.springframework.org/schema/beans\" \n"
				+ "xmlns:batch=\"http://www.springframework.org/schema/batch\" xmlns:aop=\"http://www.springframework.org/schema/aop\"\n"
				+ "xmlns:tx=\"http://www.springframework.org/schema/tx\" xmlns:p=\"http://www.springframework.org/schema/p\"\n"
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "xsi:schemaLocation=\"\n"
				+ "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\n"
				+ "http://www.springframework.org/schema/batch http://www.springframework.org/schema/batch/spring-batch.xsd\n"
				+ "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd\n"
				+ "http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd\">\n\n"
				+ "<!-- Set up our reader and its properties -->\n"
				+ "<bean id=\"itemReader\" class=\"org.springframework.batch.item.file.FlatFileItemReader\">\n"
				+ "<property name=\"resource\" value=\"file:/tmp/sample.txt\" />\n"
				+ "<property name=\"lineMapper\" ref=\"lineMapper\" />\n"
				+ "</bean>\n"

				+ "<bean id=\"lineMapper\" class=\"org.springframework.batch.item.file.mapping.DefaultLineMapper\">\n"
				+ "<property name=\"lineTokenizer\" ref=\"lineTokenizer\" />\n"
				+ "<property name=\"fieldSetMapper\" ref=\"fieldSetMapper\" />\n"
				+ "</bean>\n\n"

				+ "<bean id=\"lineTokenizer\" class=\"org.springframework.batch.item.file.transform.DelimitedLineTokenizer\">\n"
				+ "<constructor-arg value=\",\" />\n"
				+ "</bean>\n\n"

				+ "<bean id=\"fieldSetMapper\" class=\"org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper\" />\n"

				+ "<bean id=\"lineAggregator\" class=\"org.springframework.batch.item.file.transform.DelimitedLineAggregator\">\n"
				+ "<property name=\"delimiter\" value=\" \"/>\n"
				+ "</bean>\n\n"

				+ "<!-- Set up our writer, and it's properties -->\n"
				+ "<bean id=\"itemWriter\" class=\"org.springframework.batch.item.file.FlatFileItemWriter\">\n"
				+ "<property name=\"resource\" value=\"file:/tmp/sample1out.txt\" />\n"
				+ "<property name=\"lineAggregator\" ref=\"lineAggregator\" />\n"
				+ "</bean>\n"
				+ "<batch:job id=\"job\">\n"
				+ "<batch:step id=\"step1\">\n"
				+ "<batch:tasklet>\n"
				+ "<batch:chunk reader=\"itemReader\" writer=\"itemWriter\"\n"
				+ "commit-interval=\"1\" />\n"
				+ "</batch:tasklet>\n"
				+ "</batch:step>\n"
				+ "</batch:job>\n"
				+ "</beans>\n";
	}

	public static void createComposedJobModule(String jobName, String definition, ModuleDefinitionService moduleDefinitionService) {
		String xmlDefinition = convertDefinitionToXML(definition);
		String moduleName = getComposedJobModuleName(jobName);
		moduleDefinitionService.upload(moduleName, ModuleType.job, xmlDefinition.getBytes(),false);
	}

	public static String getComposedJobModuleName(String jobName){
		return jobName + MODULE_SUFFIX;
	}
	public static boolean isComposedJobDefinition(String definition){
		String patternString = "(\\|(?=([^\\\"']*[\\\"'][^\\\"']*[\\\"'])*[^\\\"']*$)) " +
				"| (\\&(?=([^\\\"']*[\\\"'][^\\\"']*[\\\"'])*[^\\\"']*$)) ";
		Pattern pattern = Pattern.compile(patternString);

		Matcher matcher = pattern.matcher(definition);
		return matcher.find() || definition.contains("jdbc");
		
	}
}
