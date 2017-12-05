package zero.annotation.processor;

/**
 * @author Linbing Tang
 * @since 17-12-5.
 */
public class ContentData {
  private int id;

  public int getId() {
    return id;
  }

  public ContentData setId(int id) {
    this.id = id;
    return this;
  }

  @Override
  public String toString() {
    return "ContentData{" +
      "id=" + id +
      '}';
  }
}
