package zero.annotation.processor;

import java.util.ArrayList;

/**
 * @author Linbing Tang
 * @since 17-12-5.
 */
public class BindData {
  //java 类的包名
  private String packageName;

  //将要生成的java类名， MainActivity$$ZeroBind
  private String className;

  //组件名， Activity等
  private String componentName;


  //content view 数据
  private ContentData contentData;

  //绑定的控件集合
  private ArrayList<BindViewData> bindViewDatas = new ArrayList<>();

  public String getPackageName() {
    return packageName;
  }

  public BindData setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public String getClassName() {
    return className;
  }

  public BindData setClassName(String className) {
    this.className = className;
    return this;
  }

  public String getComponentName() {
    return componentName;
  }

  public BindData setComponentName(String componentName) {
    this.componentName = componentName;
    return this;
  }

  public ContentData getContentData() {
    return contentData;
  }

  public BindData setContentData(ContentData contentData) {
    this.contentData = contentData;
    return this;
  }

  public ArrayList<BindViewData> getBindViewDatas() {
    return bindViewDatas;
  }

  public BindData addBindViewData(BindViewData bindViewData){
    bindViewDatas.add(bindViewData);
    return this;
  }

  public String getQualifiedName(){
    return packageName+"."+className;
  }

  public String genJavaCode(){
    StringBuilder sb = new StringBuilder();
    sb.append("// Generated code from Zero library. Do not modify!\n")
      .append("package ").append(packageName).append(";\n\n")
      .append("import android.app.*;\n")
      .append("import zero.*;\n")
      .append("import ").append(packageName).append(".*;\n\n")
      .append("public class ").append(className).append(" extends AbsZeroBind {\n\n");

    //setContentView
    if(contentData != null){
      sb.append("  @Override\n")
        .append("  public void setContentView(Activity activity) {\n")
        .append("    activity.setContentView(").append(String.valueOf(contentData.getId())).append(");\n")
        .append("  }\n\n");
    }

    //findViewById
    if(!bindViewDatas.isEmpty()){
      sb.append("  @Override\n")
        .append("  public void findViewById(Activity a) {\n")
        .append("    //cast to ").append(componentName).append("\n")
        .append("    ").append(componentName).append(" activity = (").append(componentName).append(")a;\n");
      for(BindViewData viewData : bindViewDatas){
        sb.append("    ").append("activity.").append(viewData.getName())
          .append(" = (").append(viewData.getViewType())
          .append(")activity.findViewById(").append(String.valueOf(viewData.getId())).append(");\n");
      }
      sb.append("  }\n\n");
    }

    sb.append("}\n");
    return sb.toString();
  }

  @Override
  public String toString() {
    return "BindData{" +
      "packageName='" + packageName + '\'' +
      ", className='" + className + '\'' +
      ", componentName='" + componentName + '\'' +
      ", contentData=" + contentData +
      ", bindViewDatas=" + bindViewDatas +
      '}';
  }
}
