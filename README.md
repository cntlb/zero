[TOC]

zero bind library是一个仿ButterKnife的编译期注解框架的*练习*，旨在熟悉编译期注解和注解处理器的工作原理以及习惯的API。当前基本都使用Android Studio进行android开发，因此这个练习也基于AS开发环境(AS3.0, gradle-4.1-all, com.android.tools.build:gradle:3.0.0)。练习中大量参考了ButterKnife的源码，这些代码基本都源于ButterKnife，甚至目录结构和gradle的一些配置和编写风格，注释未及之处参考[JakeWharton/**butterknife**](https://github.com/JakeWharton/butterknife) 。笔者水平有限，错误在所难免，欢迎批评指正。

## 关于Processor

为了能更好的了解注解处理器在处理注解时进行了那些操作，代码调试的功能似乎是必不可少的，然而注解处理器是在javac之前执行，所以直接在处理器中打断点然后运行是调试不到注解处理器的。可以搜索相关的文章了解，比如这个[如何调试编译时注解处理器AnnotationProcessor](http://blog.csdn.net/tomatomas/article/details/53998585) ，鉴于调试的麻烦，刚开始了解Processor可以使用类似于打印日志的方式，这里需要注意的是`System.out.println()`无法在控制台打印日志，因此首先搭建一个具有日志输出功能的Processor。以下给出一个`LoggerProcessor`：

```java
package zero.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public abstract class LoggerProcessor extends AbstractProcessor {

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  protected void error(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.ERROR, element, message, args);
  }

  protected void note(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.NOTE, element, message, args);
  }

  private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    messager.printMessage(kind, message, element);
  }
}
```

`Processor#init`顾名思义对注解处理器进行一些配置，如这里获取`Message`对象。注解处理器框架涉及到大量的接口，这些接口用于帮助我们对注解进行处理，比如`Processor`、`Messager`、`Element`等等都是接口。

**Messager#printMessage(Diagnostic.Kind, CharSequence, Element)**

```java
    /**
     * Prints a message of the specified kind at the location of the
     * element.
     *
     * @param kind the kind of message
     * @param msg  the message, or an empty string if none
     * @param e    the element to use as a position hint
     */
    void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e);
```

这里传入的参数`Element`用于源码的定位，比如处理注解时警告或者错误信息。上面的`note()`方法使用后`note(element, "bind with layout id = %#x", id)`的效果如：

```
/home/jmu/AndroidStudioProjects/zero/sample/src/main/java/com/example/annotationtest/MainActivity.java:9: 注: bind with layout id = 0x7f09001b
public class MainActivity extends AppCompatActivity {
       ^
```

`error()`将使得注解处理器在调用处打印错误信息，并导致最终编译失败：

```
...MainActivity.java:9: 错误: bind with layout id = 0x7f09001b
public class MainActivity extends AppCompatActivity {
       ^
2 个错误

:sample:compileDebugJavaWithJavac FAILED

FAILURE: Build failed with an exception.
```

有了这两个日志方法，就可以在适当的时候在控制台打印想要了解的信息。



## 第一个注解@ContentView

**ContentView.java**

```java
package zero.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ContentView {
  int value();
}
```

这个注解使用在Activity类上，为Activity指定布局。类似于ButterKnife(ButterKnife不提供类似的注解)，`@ContentView`的作用使得我们将来要在

```java
@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Zero.bind(this);
  }
}
```

`Zero.bind(this)`之后调用注解处理器生成的java代码`Activity.setContentView(id)`，注意不是使用反射来调用`Activity.setContentView`。



## ContentViewProcessor

```java
package zero.annotation.processor;

@SupportedAnnotationTypes({"zero.annotation.ContentView"})
public class ContentViewProcessor extends LoggerProcessor {
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Set<? extends Element> elements = env.getElementsAnnotatedWith(ContentView.class);
    for (Element element : elements) {
      Element enclosingElement = element.getEnclosingElement();
      System.out.println(enclosingElement.getClass());
      int id = element.getAnnotation(ContentView.class).value();
      note(element, "bind with layout id = %#x", id);
    }
    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}

```

再议Processor(详见api)

> 1. Set<String> getSupportedAnnotationTypes();
>
>    指定该注解处理器可以处理那些注解，重写该方法返回一个`Set<String>`或者在处理器上使用注解`@SupportedAnnotationTypes`
>
> 2. SourceVersion getSupportedSourceVersion();
>
>    支持的java编译器版本，重写或者使用`@SupportedSourceVersion`注解
>
> 3. void init(ProcessingEnvironment processingEnv)；
>
>    Initializes the processor with the processing environment.
>
> 4. boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
>
>    处理注解的方法，待处理的注解通过参数`annotations`传递，返回值表示注解是否已被处理，`roundEnv`表示当前和之前的处理环境。

上面的代码简单的遍历了使用`@ContentView`的类，并将其中的布局文件id打印在控制台(验证`System.out.println`是否生效)。我们循序渐进旨在能在探索中了解`Processor` 。为了在AS上使用该处理器，需要进行一些配置，这些配置相比eclipse相对简单。

```
//1.结构
sample
├── build.gradle
├── proguard-rules.pro
└── src
    └── main
        ├── AndroidManifest.xml
        └── java/android/com/example/annotationtest
                                    └── MainActivity.java
zero-annotation
├── build.gradle
└── src/main/java/zero/annotation
                       └── ContentView.java

zero-annotation-processor/
├── build.gradle
└── src/main
        ├── java/zero/annotation/processor
        │                        ├── ContentViewProcessor.java
        │                        └── LoggerProcessor.java
        └── resources/META-INF/services
                               └── javax.annotation.processing.Processor
                               
//2.1 javax.annotation.processing.Processor内容
zero.annotation.processor.ContentViewProcessor

//2.2 sample/build.gradle依赖部分
dependencies {
	//其他依赖...
    annotationProcessor project(path: ':zero-annotation-processor')
    api project(path: ':zero-annotation')
}
```

对比eclipse下的配置，as中只需要上面的2.1,2.2即可使用自定义的注解处理器。

## Processor生成java代码

建立Android library :zero, 依赖

```
zero
├── build.gradle
├── proguard-rules.pro
└── src/main
        ├── AndroidManifest.xml
        └── java/zero
                ├── IContent.java
                └── Zero.java

//IContent.java
public interface IContent {
  void setContentView(Activity activity);
}

//build.gradle.dependencies
dependencies {
	...
    annotationProcessor project(path: ':zero-annotation-processor')
    compile project(path: ':zero-annotation-processor')
}
```

提供`IContent`接口，希望使用了`@ContentView`后的`Activity`可以在同目录下生成一个形如`Activity$$ZeroBind`的类，并且实现`IContent`接口，如`MainActivity$$ZeroBind` ：

```java
// Generated code from Zero library. Do not modify!
package com.example.annotationtest;

public class MainActivity$$ZeroBind implements zero.IContent {

  @Override
  public void setContentView(android.app.Activity activity) {
    activity.setContentView(2131296283);
  }
}
```

当使用`Zero.bind(this)`时，反射创建`MainActivity$$ZeroBind`对象，调用`IContent.setContentView`来为`MainActivity`设置布局。因此下面的小目标就是通过`Processor`生成`MainActivity$$ZeroBind.java`文件：

```java
@SupportedAnnotationTypes({"zero.annotation.ContentView"})
public class ContentViewProcessor extends LoggerProcessor {

  public static final String SUFFIX = "$$ZeroBind";

  private Filer filer;
  private Elements elementUtils;
  private Types typeUtils;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    filer = processingEnv.getFiler();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Set<? extends Element> elements = env.getElementsAnnotatedWith(ContentView.class);
    for (Element element : elements) {
//      Element enclosingElement = element.getEnclosingElement();
//      note(enclosingElement, "%s", enclosingElement.getClass().getSuperclass());
      int id = element.getAnnotation(ContentView.class).value();
//      note(element, "bind with layout id = %#x", id);
      TypeMirror typeMirror = element.asType();
//      note(element, "%s\n%s", typeMirror.toString(), typeMirror.getKind());

      try {
        String classFullName = typeMirror.toString() + SUFFIX;
        JavaFileObject sourceFile = filer.createSourceFile(classFullName, element);
        Writer writer = sourceFile.openWriter();
        TypeElement typeElement = elementUtils.getTypeElement(typeMirror.toString());
        PackageElement packageOf = elementUtils.getPackageOf(element);
        writer.append("// Generated code from Zero library. Do not modify!\n")
          .append("package ").append(packageOf.getQualifiedName()).append(";\n\n")
          .append("public class ").append(typeElement.getSimpleName()).append(SUFFIX).append(" implements zero.IContent {\n\n")
          .append("  @Override\n")
          .append("  public void setContentView(android.app.Activity activity) {\n")
          .append("    activity.setContentView(").append(String.valueOf(id)).append(");\n")
          .append("  }\n")
          .append("}")
          .flush();
        writer.close();
      } catch (IOException e) {
        error(element, "不能写入java文件！");
      }
    }
    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
```

为了说明上面的代码以及理解，需要一些准备知识。

## javax.lang.model包

| 包                                        | 描述                                       |
| ---------------------------------------- | ---------------------------------------- |
| [javax.lang.model](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/package-summary.html) | Classes and hierarchies of packages used to model the Java programming language. |
| [javax.lang.model.element](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/package-summary.html) | Interfaces used to model elements of the Java programming language. |
| [javax.lang.model.type](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/package-summary.html) | Interfaces used to model Java programming language types. |
| [javax.lang.model.util](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/package-summary.html) | Utilities to assist in the processing of [program elements](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/package-summary.html) and [types](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/package-summary.html). |

主要介绍：`Element`和`TypeMirror`

### Element

参看https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Element.html

**All Known Subinterfaces:**

> [ExecutableElement](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/ExecutableElement.html), [PackageElement](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/PackageElement.html), [Parameterizable](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Parameterizable.html), [QualifiedNameable](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/QualifiedNameable.html), [TypeElement](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/TypeElement.html), [TypeParameterElement](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/TypeParameterElement.html), [VariableElement](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/VariableElement.html)
>
> ```
> 继承关系
> Element
>     PackageElement (javax.lang.model.element)
>     ExecutableElement (javax.lang.model.element)
>     VariableElement (javax.lang.model.element)
>     TypeElement (javax.lang.model.element)
>     QualifiedNameable (javax.lang.model.element)
>         PackageElement (javax.lang.model.element)
>         TypeElement (javax.lang.model.element)
>     Parameterizable (javax.lang.model.element)
>         ExecutableElement (javax.lang.model.element)
>         TypeElement (javax.lang.model.element)
>     TypeParameterElement (javax.lang.model.element)
> ```
>
> 

**public interface Element**

> 代表程序中的元素，如包、类或方法。每个元素表示一个静态的、语言级的构造(不是运行时虚拟机构造的)。
>
> 元素的比较应该使用 [`equals(Object)`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Element.html#equals(java.lang.Object)) 方法. 不能保证任何特定元素总是由同一对象表示。
>
> 实现基于一个 `Element` 对象的类的操作, 使用 [visitor](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/ElementVisitor.html) 或者 [`getKind()`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Element.html#getKind()) 方法. 由于一个实现类可以选择多个 `Element` 的子接口，使用 `instanceof` 来决定在这种继承关系中的一个对象的实际类型未必是可靠的。
>
> ```java
> package com.example.demo;//[PackageElement, ElementKind.PACKAGE]
> public class Main {//[TypeElement,ElementKind.CLASS]
>   int a;//[VariableElement, ElementKind.FIELD]
>   
>   static {//[ExecutableElement, ElementKind.STATIC_INIT]
>     System.loadLibrary("c");
>   }
>   {//[ExecutableElement, ElementKind.INSTANCE_INIT]
>     a = 100;
>   }
>   public Main(){//[ExecutableElement,ElementKind.CONSTRUCTOR]
>     int b = 10;//[VariableElement, ElementKind.LOCAL_VARIABLE]
>   }
>   
>   public String toString(){//[ExecutableElement, ElementKind.METHOD]
>     return super.toString();
>   }
> }
>
> public @interface OnClick{//[TypeElement, ElementKind.ANNOTATION_TYPE]
>   
> }
>
> public interface Stack<T>{//[TypeElement,ElementKind.INTERFACE]
>   T top;//[VariableElement, ElementKind.FIELD, TypeKind.TYPEVAR]
>   TypeNotExists wtf;//[VariableElement, ElementKind.FIELD, TypeKind.ERROR]
> }
> ```

**Method Detail**

> 1. TypeMirror asType()	返回元素定义的类型
>
>    一个范型元素定义一族类型，而不是一个。 范型元素返回其原始类型. 这是元素在类型变量相应于其形式类型参数上的调用. 例如, 对于范型元素 `C<N extends Number>`, 返回参数化类型 `C<N>` .  [`Types`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/Types.html) 实用接口有更通用的方法来获取元素定义的所有类型的范围。
>
> 2. ElementKind getKind()  返回元素的类型
>
> 3. List<? extends AnnotationMirror> getAnnotationMirrors()  返回直接呈现在元素上的注解
>
>    使用getAllAnnotationMirrors可以获得继承来的注解
>
> 4. <A extends Annotation> A getAnnotation(Class<A> annotationType)
>
>    返回呈现在元素上的指定注解实例，不存在返回`null` 。注解可以直接直接呈现或者继承。
>
> 5. Set<Modifier> getModifiers()  返回元素的修饰符
>
> 6. Name getSimpleName()  返回元素的简单名字
>
>    范型类的名字不带任何形式类型参数，比如 `java.util.Set<E>` 的SimpleName是 `"Set"`. 未命名的包返回空名字， 构造器返回"`<init>`"，静态代码快返回 "`<clinit>`" ， 匿名内部类或者构造代码快返回空名字.
>
> 7. Element getEnclosingElement() 
>
>    返回元素所在的最里层元素, 简言之就是闭包.
>
>    - 如果该元素在逻辑上直接被另一个元素包裹，返回该包裹的元素
>    - 如果是一个顶级类, 返回包元素(PackageElement)
>    - 如果是包元素返回null
>    - 如果是类型参数或范型元素，返回类型参数(TypeParametrElement)
>
> 8. List<? extends Element> getEnclosedElements()
>
>    返回当前元素直接包裹的元素集合。类和接口视为包裹字段、方法、构造器和成员类型。 这包括了任何隐式的默认构造方法，枚举中的`values`和`valueOf`方法。包元素包裹在其中的顶级类和接口，但不认为包裹了子包。其他类型的元素当前默认不包裹任何元素，但可能 跟随API和编程语言而变更。
>
>    注意某些类型的元素可以通过 [`ElementFilter`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/ElementFilter.html)中的方法分离出来.

### TypeMirror

参考https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/TypeMirror.html

**All Known Subinterfaces：** 

> [ArrayType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/ArrayType.html), [DeclaredType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/DeclaredType.html), [ErrorType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/ErrorType.html), [ExecutableType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/ExecutableType.html), [NoType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/NoType.html), [NullType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/NullType.html), [PrimitiveType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/PrimitiveType.html), [ReferenceType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/ReferenceType.html), [TypeVariable](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/TypeVariable.html), [UnionType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/UnionType.html), [WildcardType](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/type/WildcardType.html)

**public interface TypeMirror**

> 表示java中的一个类型. 类型包含基本类型、声明类型 (类和接口)、数组、类型变量和null 类型. 也表示通配符类型参数(方法签名和返回值中的), 以及对应包和关键字 `void`的伪类型.
>
> 类型的比较应该使用 [`Types`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/Types.html). 不能保证任何特定类型总是由同一对象表示。
>
> 实现基于一个 `TypeMirror` 对象的类的操作, 使用 [visitor](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/ElementVisitor.html) 或者 [`getKind()`](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Element.html#getKind()) 方法.  由于一个实现类可以选择多个 `TypeMirror` 的子接口，使用 `instanceof` 来决定在这种继承关系中的一个对象的实际类型未必是可靠的。

### Utility

javax.lang.model.util下的接口(主要指[Elements](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/Elements.html)，[Types](https://docs.oracle.com/javase/7/docs/api/javax/lang/model/util/Types.html))拥有一些实用的方法。

> 1. PackageElement 	**Elements**.getPackageOf(Element type)
>
>    Returns the package of an element. The package of a package is itself.
>
> 2. TypeElement    **Elements**.getTypeElement(CharSequence name)
>
>    Returns a type element given its canonical name.
>
> 3. boolean **Types**.isAssignable(TypeMirror t1, TypeMirror t2)
>
>    Tests whether `t1` is assignable to `t2`.
>
> 4. boolean **Types**.isSameType(TypeMirror t1, TypeMirror t2)
>
>    Tests whether two `TypeMirror` objects represent the same type. Return `true` if and only if the two types are the same
>
> 5. boolean **Types**.isSubtype(TypeMirror t1, TypeMirror t2)
>
>    Return `true` if and only if the `t1` is a subtype of `t2`



## Process生成java代码续

现在我们详细注释下`ContentViewProcessor#process` ，代码有少许不同

```java
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Set<? extends Element> elements = env.getElementsAnnotatedWith(ContentView.class);
    for (Element element : elements) {
      //ContentView定义时指定作用范围是类，所以只能作用于类上,Element一定是类元素
      if(element.getKind() != ElementKind.CLASS){
        error(element, "ContentView注解必须作用在类上！");
        throw new RuntimeException();
      }
      
      TypeElement typeElement = (TypeElement) element;
      //获取包元素，主要为了方便获取Element的包名
      //element是类元素，因此还可以使用：
      //PackageElement packageOf = (PackageElement) element.getEnclosingElement()；
      PackageElement packageOf = elementUtils.getPackageOf(element);
      int id = element.getAnnotation(ContentView.class).value();

      try {
        //仿照ButterKnife，使用自己的后缀
        String classFullName = typeElement.getQualifiedName() + SUFFIX;
        //JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
        //name:完整类名
        //originatingElements：和创建的文件相关的类元素或包元素，可省略或为null
        JavaFileObject sourceFile = filer.createSourceFile(classFullName, element);
        Writer writer = sourceFile.openWriter();
        //关于ContentView注解的java 文件模板
        String tmp =
          "// Generated code from Zero library. Do not modify!\n" +
            "package %s;\n\n" +
            "public class %s implements zero.IContent {\n\n" +
            "  @Override\n" +
            "  public void setContentView(android.app.Activity activity) {\n" +
            "    activity.setContentView(%d);\n" +
            "  }\n" +
            "}";
        //填充包名，类名，布局文件id
        writer.write(String.format(tmp, packageOf.getQualifiedName(), typeElement.getSimpleName()+SUFFIX, id));
        writer.close();
      } catch (IOException e) {
        error(element, "不能写入java文件！");
      }
    }
    return true;//ContentView被我处理了
  }
```

## Zero.bind

基于注解处理器生成的java代码已完成，最后一道工序需要将代码调用起来即可。

```java
public class Zero {
  public static void bind(Activity activity){
    try {
      String fullName = activity.getClass().getCanonicalName()+ ContentViewProcessor.SUFFIX;
      Class<?> zeroBind = Class.forName(fullName);
      IContent content = (IContent) zeroBind.getConstructor().newInstance();
      content.setContentView(activity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
```

现在可以向ButterKnife一样使用`Zero.bind` 。这里根据我们定义的规则使用了少量的运行时反射手段用于动态调用适当的代码，另外发布时需要将相应的类不做混淆处理即可。