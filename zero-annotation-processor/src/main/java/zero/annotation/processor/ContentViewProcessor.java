package zero.annotation.processor;

import zero.annotation.ContentView;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

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
