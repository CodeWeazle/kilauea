# json-mapper

*json-mapper* implements an annotation processor to generate Java classes with Jackson annotations with some convenient creation/mapping functions.

## Usage

### Maven

Add the annotation processor class to your pom.xml and rebuild your project. (The version shown is related to the *release* branch.)

```
	<build>
		<plugins>
			...
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				...
				<configuration>
					...
					<annotationProcessorPaths>
						...
						<path>
							<groupId>net.magiccode.json</groupId>
							<artifactId>json-mapper</artifactId>
							<version>0.0.2</version>
						</path>
						...
					</annotationProcessorPaths>
				</configuration>
			</plugin>

		</plugins>

	</build>
```

## Annotations

### @JSONMapped

@JSONMapped can be used on class level and initiates the generation of a class that contains the same fields as the annotated class
but annotates these with Jackson annotations for JSON generation.

Example:
```
@JSONMapped(fluentAccessors = false)
```

Some arguments can be provided to have some influence on the code generation.

| argument | values | Description |
| --- | --- | -- |
|useLombok |true, **false**|Setting useLombok to true generates much less code, because getters and setters can be replace by lombok annotations, just as the constructor(s), toString etc.|
|fluentAccessors |true, **false**|Creates getters and setters that do not start with *get*, *is* or *set* rather than the actual name of the field. If useLombok is *true*, this setting is passed on to @Accessors(fluent=true|false).|
|chainedSetters |**true**, false|Generates setters which return *this*. |
|prefix | JSON |Adds a prefix to the name of the generated class. Defaults to "JSON"|
|packageName| |Defines the name of the packacke for the generated class. If no *packageName* is given, this defaults to the package of the annotated class.|
|subpackageName| |Defines the name for a subpackage added to the default if *packageName* is not specified.|
|jsonInclude |**ALWAYS**, NON_NULL, NON_ABSENT, NON_EMPTY, NON_DEFAULT, CUSTOM, USE_DEFAULTS|Generated classes are annotated with *@JsonInclude*. This defaults to ALWAYS, but can be specified otherwise by using the *jsonInclude* argument.|
|superClass| |Fully qualified name of the superclass that the generated class will extend.|
|interfaces| |Comma separated list of fully qualified name of the superinterface that the generated class will implement.|
|inheritFields|**true**, false|Defines whether or not field from superclasses of the annotated class should be generated. Default is **true**|

### @JSONTransient

This annotation works on field level and is used to mark fields that will be annotated with *@JsonIgnore* in the generated code.

```
@JSONTransient
private String ignoredValue;
```
in the annotated class becomes
```
@JsonIgnore
private String ignoredValue;
```
in the generated code.


### @JSONRequired

Every field in the generated class will be annotated with *@JsonProperty* unless marked with *@JSONTransient*. Setting *@JSONRequired* on a field 
additionally adds the *required = true* argument.

```
@JSONRequired
private Double requiredValue;
```
becomes
```
@JsonProperty(
       value = "double_value",
       required = true
)
private Double requiredValue;
```
in the generated class.


## Generated code

The code generated by the annotation processor depends on the options specified on the *@JSONMapped* annotation. If *useLombok* is set to true, a class will be generated
using Lombok annotations and no getters, setters or constructors will be created. 
Fields, of course, will be generated with (at least) @JsonProperty or @JsonIgnore annotations in any case.

### Constructors

---- Constructor explanation an 'of' methods ----


### toJSONString()

Additionally to the usual *toString()* method and additional *toJSONString()* method is generated that prints the name and contents of the class as formatted JSON.

```
net.magiccode.lazy.json.JSONExampleCode04{
  "another_field" : 90,
  "value_list" : [ "abc", "def", "ghi", "xyz" ],
  "third_field" : -23.4567,
  "boolean_field" : true,
  "example02" : null
}
```


## Libraries

At the time being, the dependencies used by *json-mapper* are

```
	<properties>
		<java.version>17</java.version>
		<jackson.version>2.15.0</jackson.version>
		<log4j.version>2.21.1</log4j.version>
		<lombok.version>1.18.30</lombok.version>
		<javapoet.version>1.13.0</javapoet.version>
		<auto-service.version>1.1.1</auto-service.version>
	</properties>

	...

	<dependency>
		<groupId>com.fasterxml.jackson.core</groupId>
		<artifactId>jackson-core</artifactId>
		<version>${jackson.version}</version>
	</dependency>

	<dependency>
		<groupId>com.fasterxml.jackson.core</groupId>
		<artifactId>jackson-databind</artifactId>
		<version>${jackson.version}</version>
	</dependency>

	<dependency>
		<groupId>com.fasterxml.jackson.core</groupId>
		<artifactId>jackson-annotations</artifactId>
		<version>${jackson.version}</version>
	</dependency>

	<dependency>
		<groupId>org.projectlombok</groupId>
		<artifactId>lombok</artifactId>
		<version>${lombok.version}</version>
	</dependency>

	<dependency>
		<groupId>com.squareup</groupId>
		<artifactId>javapoet</artifactId>
		<version>${javapoet.version}</version>
	</dependency>

	<dependency>
		<groupId>com.google.auto.service</groupId>
		<artifactId>auto-service</artifactId>
		<version>${auto-service.version}</version>
	</dependency>

	<dependency>
		<groupId>org.apache.logging.log4j</groupId>
		<artifactId>log4j-core</artifactId>
		<version>${log4j.version}</version>
	</dependency>
```

## Todos

- implement *Builder* for generated classes (non-useLombok)
- handle class inheritance properly
  - pick up fields from superclasses
  - handle plain vs. hierarchy generation 
- generate **mapping** for *useLombok* case
- generate *to* method to be able to create and populate annotated class
- generate methods for annotated class to handle mapping
