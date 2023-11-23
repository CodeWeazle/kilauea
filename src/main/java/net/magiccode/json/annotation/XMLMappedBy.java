/**
 * 
 */
package net.magiccode.json.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Classes generated by JsonMapper are by default annotated with this annotation
 * providing the class their generation was based on in the <i>mappedClass</i> 
 * argument. This makes it easier to process this annotation at runtime, for instance
 * using instrumentation.
 */
public @interface XMLMappedBy {

	Class<?> mappedClass() default Object.class;
}
