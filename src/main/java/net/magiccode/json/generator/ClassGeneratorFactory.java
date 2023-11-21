/**
 * json-mapper
 * 
 * Published under Apache-2.0 license (https://github.com/CodeWeazle/json-mapper/blob/main/LICENSE)
 * 
 * Code: https://github.com/CodeWeazle/json-mapper
 * 
 * @author CodeWeazle (2023)
 * 
 * Filename: ClassGeneratorFactory.java
 */

package net.magiccode.json.generator;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

import com.squareup.javapoet.ClassName;

/**
 * 
 */
public class ClassGeneratorFactory {

	
	public static ClassGenerator getClassGenerator(GeneratorType generatorType, 
												   ProcessingEnvironment procEnv, 
												   Filer filer, 
												   Messager messager,
												   Map<ClassName, List<ElementInfo>> input) {
		
		switch (generatorType) {
		
			case JSON:
				return new JSONClassGenerator(procEnv, filer, messager, input);
		
			default:
				return new PlainClassGenerator(procEnv, filer, messager, input);
		
		}
	}
	
}