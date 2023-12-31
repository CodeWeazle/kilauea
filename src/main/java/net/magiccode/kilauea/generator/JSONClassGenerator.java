/**
 * kilauea
 * 
 * Published under Apache-2.0 license (https://github.com/CodeWeazle/kilauea/blob/main/LICENSE)
 * 
 * Code: https://github.com/CodeWeazle/kilauea
 * 
 * @author CodeWeazle (2023)
 * 
 * Filename: JSONClassGenerator.java
 */
package net.magiccode.kilauea.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import net.magiccode.kilauea.annotation.JSONMappedBy;
import net.magiccode.kilauea.annotation.JSONRequired;
import net.magiccode.kilauea.annotation.JSONTransient;
import net.magiccode.kilauea.annotation.Mapped;
import net.magiccode.kilauea.util.StringUtil;

// 
/**
 * Generates JSON annotated mapping class for a given java class.
 * 
 * The generated class provides all the fields of the annotated class as well as methods to 
 * map between instances of these two classes seamlessly. (to/of methods)
 */
public class JSONClassGenerator extends AbstractClassGenerator {

	/**
	 * The purpose of this class is to generate JSON annotated Java code using the
	 * JavaPoet framework. See documentation for more details about
	 * <i>Mapped(type=GeneratorType.JSON)</i>
	 * 
	 * @param procEnv  - the processing environment
	 * @param filer    - the filer
	 * @param messager - used to output messages
	 * @param annotationInfo - {@code ElementInfo} instance containing information about the {@code @Mapped(type=GeneratorType.JSON)} annotation
	 */
	public JSONClassGenerator(final ProcessingEnvironment procEnv, 
							  final Filer filer, 
							  final Messager messager,
							  final ElementInfo annotationInfo,
							  final ClassName annotatedClass,
							  final Map<ClassName, List<ElementInfo>> input) {
		super(procEnv, filer, messager, annotationInfo, annotatedClass, input);
	}


	
	/**
	 * create JSON specific methods
	 */
	@Override
	public
	void createSpecificFieldsAndMethods(ClassName incomingObjectClass, String packageName, String className,
			ElementInfo annotationInfo, List<FieldSpec> fields, Map<String, MethodSpec> methods) {
		createToJSONString(methods);		
	}
	
	/**
	 * mapped by annotation is specific to type of mapper
	 *
	 * @param annotationInfo {@code ElementInfo} instance describing the annotation options
	 * @return annotation {@code AnnotationSpec} instance of created mapped-by annotation
	 */
	public AnnotationSpec createMappedByAnnotation(final ElementInfo annotationInfo) {
		return AnnotationSpec.builder(JSONMappedBy.class)
				.addMember("mappedClass", "$T.class", ClassName.get(annotationInfo.element())).build();
	}
	
	/**
	 * annotations specifically for the type JSON
	 *
	 * @param annotationInfo {@code ElementInfo} instance describing the annotation options
	 * @return List of annotations {@code AnnotationSpec} instances or null.
	 */
	
	public List<AnnotationSpec>  getAdditionalAnnotationsForClass(final ElementInfo annotationInfo) {
		List<AnnotationSpec> annotations = List.of(
				AnnotationSpec.builder(JsonInclude.class)
					.addMember("value", "$T.$L", Include.class, annotationInfo.jsonInclude().name()).build());
		return annotations;
	}
	

	/**
	 * generate toString method
	 * 
	 * @param methods - Map containing the methods to be created. Key is the name of the method, value a MethodSpec instance
	 * 		
	 */
	private void createToJSONString(final Map<String, MethodSpec> methods) {
		// create toJSONString method
		MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("toJSONString").addModifiers(Modifier.PUBLIC)
				.addJavadoc(CodeBlock.builder().add("provides a formatted JSON string with all fields\n")
						.add("and their current values.\n").build())
				.addStatement("$T mapper = new $T()", ObjectMapper.class, ObjectMapper.class)
				.addStatement("mapper.findAndRegisterModules()")
				.addStatement("String value = this.getClass().getName()").beginControlFlow("try")
				.addStatement("value += mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)")
				.endControlFlow().beginControlFlow("catch ($T e)", JsonProcessingException.class)
				.addStatement("e.printStackTrace()").endControlFlow().addStatement("return value")
				.returns(ClassName.get(String.class));
		methods.put("toJSONString",  toStringBuilder.build());
	}

	/**
	 * create field
	 * 
	 * @param field         - VariableElement representation of field to be created
	 * @param annotationInfo - {@code ElementInfo} instance containing information about the {@code @Mapped(type=GeneratorType.JSON)} annotation
	 * @param fieldClass    - TypeName for class field shall be created in.
	 * @param fieldIsMapped - indicates whether or not the given field is annotated  with {@code Mapped(type=GeneratorType.JSON)}.
	 * @return field specification for the create field.
	 */
	@Override
	public FieldSpec createFieldSpec(final VariableElement field, 
									 final ElementInfo annotationInfo, 
									 final TypeName fieldClass,				
									 boolean fieldIsMapped) {

		List<AnnotationSpec> annotations = new ArrayList<>();
		
		FieldSpec fieldspec = null;
		String fieldName = field.getSimpleName().toString();
		if (field.getAnnotation(JSONTransient.class) == null && field.getAnnotation(JsonIgnore.class) == null) {
			// check for java.time.LocalDate or java.time.LocalDateTime
			
			if (fieldClass.equals(ClassName.get(LocalDateTime.class))) {
				annotations.add(AnnotationSpec.builder(JsonFormat.class).addMember("pattern", StringUtil.quote(annotationInfo.dateTimePattern())).build());				
			}
			if (fieldClass.equals(ClassName.get(LocalDate.class))) {
				annotations.add(AnnotationSpec.builder(JsonFormat.class).addMember("pattern", StringUtil.quote(annotationInfo.datePattern())).build());
			}
			
			AnnotationSpec.Builder jsonPropertyAnnotationBuilder = AnnotationSpec.builder(JsonProperty.class).addMember(
					"value", StringUtil.quote(StringUtil.camelToSnake(field.getSimpleName().toString()), '"'));
			if (field.getAnnotation(JSONRequired.class) != null) {
				jsonPropertyAnnotationBuilder.addMember("required", "true");
			}
			annotations.add(jsonPropertyAnnotationBuilder.build());
			
			TypeMirror type = field.asType();

			TypeName fieldType = fieldClass;
			if (fieldIsMapped) {
				TypeMirror fieldTypeMirror = field.asType();
				Element fieldElement = typeUtils.asElement(fieldTypeMirror);
				if (fieldElement instanceof TypeElement) {
					ClassName fieldClassName = ClassName.get((TypeElement) fieldElement);
					fieldType = getMappedTypeForClassName(fieldClassName);
				}
			}
			fieldType = checkFieldTypeForCollections(annotationInfo, type, fieldType);

			FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE); 
			annotations.stream().forEach(annotation -> {
				fieldSpecBuilder.addAnnotation(annotation);
			});
			fieldspec = fieldSpecBuilder.build();
			
		} else {
			fieldspec = FieldSpec.builder(fieldClass, fieldName, Modifier.PRIVATE)
								 .addAnnotation(JsonIgnore.class)
								 .build();
		}
		return fieldspec;
	}
	
	
	/**
	 * create (additional) field
	 * 
	 * @param annotationInfo - information about the arguments of the <i>@Mapped</i> annotation
	 * @param fields         - list of fields to be created
	 * 
	 */
	@Override
	public void createAdditionalFields(ElementInfo annotationInfo, final List<FieldSpec> fields) {
		annotationInfo.additionalFields().entrySet().stream().forEach(field -> {
			FieldSpec.Builder fieldspecBuilder = generateFieldAnnotation(field);
			fields.add(fieldspecBuilder.build());
		});
	}

	/**
	 * create (additional) fields with getters and setters
	 * 
	 * @param annotationInfo - information about the arguments of the <i>@Mapped</i> annotation
	 * @param fields         - list of fields to be created
	 * @param methods        - maps with MethodSpec definitions for methods to be
	 * 
	 */
	@Override
	public void createAdditionalFieldsGettersAndSetters(final ElementInfo annotationInfo, 
														 final List<FieldSpec> fields, 
														 final Map<String, MethodSpec> methods) {
		annotationInfo.additionalFields().entrySet().stream().forEach(field -> {
			if (fields.stream().anyMatch(fieldSpec -> fieldSpec.name.equals(field.getKey()))) {
				System.out.println("Additional field "+field.getKey()+" cannot be generated because it already exists. Check your annotated class and remove or rename this argument.");
			} else {
				FieldSpec.Builder fieldspecBuilder = generateFieldAnnotation(field);
				fields.add(fieldspecBuilder.build());
				
				createAdditionalGetterMethodSpec(annotationInfo, field.getKey(), field.getValue(), methods);
				createAdditionalSetterMethodSpec(annotationInfo, field.getKey(), field.getValue(), methods);
			}
		});
	}



	/**
	 * @param field
	 * @return
	 */
	private FieldSpec.Builder generateFieldAnnotation(Entry<String, TypeMirror> field) {
		TypeMirror fieldMirror = field.getValue();
		AnnotationSpec.Builder jsonPropertyAnnotationBuilder = AnnotationSpec.builder(JsonProperty.class).addMember(
				"value", StringUtil.quote(StringUtil.camelToSnake(field.getKey()), '"'));
		
		FieldSpec.Builder fieldspecBuilder = FieldSpec.builder(TypeName.get(fieldMirror), 
																field.getKey(), 
																Modifier.PRIVATE);
		fieldspecBuilder.addAnnotation(jsonPropertyAnnotationBuilder.build());
		return fieldspecBuilder;
	}
	
	
	@Override
	public Types getTypeUtils() {
		return procEnv.getTypeUtils();
	}

	@Override
	public Elements getElementUtils() {
		return procEnv.getElementUtils();
	}
	
	public boolean fieldIsMapped(final Element field) {
		return fieldIsAnnotedWith(field, Mapped.class, GeneratorType.JSON);
	}

	public boolean typeIsMapped(final TypeElement typeElement) {
		return typeIsAnnotatedWith(typeElement, Mapped.class, GeneratorType.JSON);
	}

}
