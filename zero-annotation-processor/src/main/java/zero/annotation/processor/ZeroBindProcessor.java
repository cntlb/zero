package zero.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import zero.Constant;
import zero.annotation.BindView;
import zero.annotation.ContentView;

@SupportedAnnotationTypes({
  "zero.annotation.ContentView",
  "zero.annotation.BindView",
})
public class ZeroBindProcessor extends LoggerProcessor {

  public static final String SUFFIX = Constant.SUFFIX;

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
    Map<TypeElement, BindData> targetClassMap = findAndParseTargets(env);

    for(Map.Entry<TypeElement, BindData> entry : targetClassMap.entrySet()){
      TypeElement element = entry.getKey();
      BindData bindData = entry.getValue();

      try {
        JavaFileObject sourceFile = filer.createSourceFile(bindData.getQualifiedName(), element);
        Writer writer = sourceFile.openWriter();
        writer.write(bindData.genJavaCode());
        writer.close();
      } catch (IOException e) {
        error(element, "不能写入java文件！%s", e.getMessage());
      }
    }

    return true;
  }

  private Map<TypeElement,BindData> findAndParseTargets(RoundEnvironment env) {
    Map<TypeElement, BindData> map = new LinkedHashMap<>();

    for(Element element : env.getElementsAnnotatedWith(ContentView.class)){
      if(!(element instanceof TypeElement)){
        error(element, "ContentView必须使用在类上面");
        throw new RuntimeException();
      }

      BindData bindData = map.get(element);
      if(bindData == null){
        bindData = newBindData((TypeElement) element);
      }
      int id = element.getAnnotation(ContentView.class).value();
      bindData.setContentData(new ContentData().setId(id));
      map.put((TypeElement) element, bindData);
    }

    for(Element element : env.getElementsAnnotatedWith(BindView.class)){
      if(!(element instanceof VariableElement)){
        error(element, "BindView必须使用在字段上面");
        throw new RuntimeException();
      }

      VariableElement variableElement = (VariableElement) element;
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      BindData bindData = map.get(typeElement);
      if(bindData == null){
        bindData = newBindData(typeElement);
      }
      int id = element.getAnnotation(BindView.class).value();
      BindViewData bindViewData = new BindViewData()
        .setId(id)
        .setViewType(variableElement.asType().toString())
        .setName(variableElement.getSimpleName().toString());
      bindData.addBindViewData(bindViewData);
      map.put(typeElement, bindData);
    }


    return map;
  }

  private BindData newBindData(TypeElement element){
    PackageElement packageEle = (PackageElement) element.getEnclosingElement();
    String className = element.getSimpleName().toString();
    return new BindData()
      .setPackageName(packageEle.getQualifiedName().toString())
      .setClassName(className +SUFFIX)
      .setComponentName(className);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }


}
