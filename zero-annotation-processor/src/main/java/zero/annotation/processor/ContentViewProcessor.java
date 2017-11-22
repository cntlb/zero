package zero.annotation.processor;

import zero.annotation.ContentView;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

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
