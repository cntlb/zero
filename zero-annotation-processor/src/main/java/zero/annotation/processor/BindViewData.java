package zero.annotation.processor;

/**
 * @author Linbing Tang
 * @since 17-12-5.
 */
public class BindViewData {
  private String viewType;
  private String name;
  private int id;

  public String getViewType() {
    return viewType;
  }

  public BindViewData setViewType(String viewType) {
    this.viewType = viewType;
    return this;
  }

  public String getName() {
    return name;
  }

  public BindViewData setName(String name) {
    this.name = name;
    return this;
  }

  public int getId() {
    return id;
  }

  public BindViewData setId(int id) {
    this.id = id;
    return this;
  }

  @Override
  public String toString() {
    return "BindViewData{" +
      "viewType='" + viewType + '\'' +
      ", name='" + name + '\'' +
      ", id=" + id +
      '}';
  }
}
