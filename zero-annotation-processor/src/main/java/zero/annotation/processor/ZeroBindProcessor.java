package zero.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import zero.annotation.BindView;
import zero.annotation.ContentView;
import zero.annotation.OnClick;

@SupportedAnnotationTypes({
  "zero.annotation.ContentView",
  "zero.annotation.BindView",
  "zero.annotation.OnClick",
})
public class ZeroBindProcessor extends LoggerProcessor {

  public static final String SUFFIX = "$$ZeroBind";

  private Filer filer;
  private Elements elementUtils;
  private Types typeUtils;
  private TypeMirror activityType;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    filer = processingEnv.getFiler();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    activityType = elementUtils.getTypeElement("android.app.Activity").asType();
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
      if(element.getKind() != ElementKind.CLASS){
        errorThrow(element, "ContentView必须使用在类上");
      }

      TypeElement typeElement = (TypeElement) element;
      if(!isSubtypeOfActivity(typeElement)){
        errorThrow(element, "ContentView必须使用在Activity上");
      }

      BindData bindData = getOrCreateBindData(map, typeElement);
      int id = element.getAnnotation(ContentView.class).value();
      bindData.setContentData(new ContentData().setId(id));
      map.put(typeElement, bindData);
    }

    for(Element element : env.getElementsAnnotatedWith(BindView.class)){
      if(element.getKind() != ElementKind.FIELD){
        errorThrow(element, "BindView必须使用在字段上面");
      }

      VariableElement variableElement = (VariableElement) element;
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      BindData bindData = getOrCreateBindData(map, typeElement);
      int id = element.getAnnotation(BindView.class).value();
      BindViewData bindViewData = new BindViewData()
        .setId(id)
        .setViewType(variableElement.asType().toString())
        .setName(variableElement.getSimpleName().toString());
      bindData.addBindViewData(bindViewData);
      map.put(typeElement, bindData);
    }

    for(Element element : env.getElementsAnnotatedWith(OnClick.class)){
      if(element.getKind() != ElementKind.METHOD){
        errorThrow(element, "OnClick必须使用在方法上");
      }

      ExecutableElement method = (ExecutableElement) element;
      List<? extends VariableElement> parameters = method.getParameters();
      if(parameters != null && !parameters.isEmpty()){
        errorThrow(element, "OnClick必须使用在无参方法上");
      }

      TypeElement typeElement = (TypeElement) method.getEnclosingElement();
      BindData bindData = getOrCreateBindData(map, typeElement);

      int[] ids = element.getAnnotation(OnClick.class).value();
      OnClickData onClickData = new OnClickData()
        .setIds(ids)
        .setMethod(method.getSimpleName().toString());
      bindData.addOnClickData(onClickData);
      map.put(typeElement, bindData);
    }

    return map;
  }

  private boolean isSubtypeOfActivity(TypeElement element) {
    return typeUtils.isSubtype(element.asType(), activityType);
  }

  private BindData getOrCreateBindData(Map<TypeElement, BindData> map, TypeElement element){
    BindData bindData = map.get(element);
    if(bindData == null){
      bindData = newBindData(element);
    }
    return bindData;
  }

  private BindData newBindData(TypeElement element){
    PackageElement packageEle = elementUtils.getPackageOf(element);
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
