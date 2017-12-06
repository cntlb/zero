package zero.annotation.processor;

import java.util.Arrays;

/**
 * @author Linbing Tang
 * @since 17-12-6.
 */
public class OnClickData {
  private String method;
  private int[] ids;

  public String getMethod() {
    return method;
  }

  public OnClickData setMethod(String method) {
    this.method = method;
    return this;
  }

  public int[] getIds() {
    return ids;
  }

  public OnClickData setIds(int[] ids) {
    this.ids = ids;
    return this;
  }

  @Override
  public String toString() {
    return "OnClickData{" +
      "method='" + method + '\'' +
      ", ids=" + Arrays.toString(ids) +
      '}';
  }
}
