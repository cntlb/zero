package zero.annotation.processor;

import zero.annotation.BindView;
import zero.annotation.ContentView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({
  "zero.annotation.ContentView",
  "zero.annotation.BindView",
})
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
        Name packageName = packageOf.getQualifiedName();
        Name className = typeElement.getSimpleName();

        //java class head
        writer.append("// Generated code from Zero library. Do not modify!\n")
          .append("package ").append(packageName).append(";\n\n")
          .append("import android.app.*;\n")
          .append("import zero.*;\n")
          .append("import ").append(packageName).append(".*;\n\n")
          .append("public class ").append(className).append(SUFFIX).append(" extends AbsZeroBind {\n\n");

        //setContentView
        writer.append("  @Override\n")
          .append("  public void setContentView(Activity activity) {\n")
          .append("    activity.setContentView(").append(String.valueOf(id)).append(");\n")
          .append("  }\n\n");

        //findViewById
        Set<? extends Element> bindViews = env.getElementsAnnotatedWith(BindView.class);
        if(!bindViews.isEmpty()) {
          writer
            .append("  @Override\n")
            .append("  public void findViewById(Activity a) {\n")
            .append("    //cast to ").append(className).append("\n")
            .append("    ").append(className).append(" activity = (").append(className).append(")a;\n");

          for (Element e : bindViews) {
            int viewId = e.getAnnotation(BindView.class).value();
            String fieldType = e.asType().toString();
            writer
              .append("    ").append("activity.").append(e.getSimpleName())
              .append(" = (").append(fieldType)
              .append(")activity.findViewById(").append(String.valueOf(viewId)).append(");\n");
          }

          writer.append("  }\n\n");
        }

        //java end
        writer
          .append("}")
          .flush();
        writer.close();
      } catch (IOException e) {
        error(element, "不能写入java文件！%s", e.getMessage());
      }
    }
    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
