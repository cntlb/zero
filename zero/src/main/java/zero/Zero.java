package zero;

import android.app.Activity;

import zero.annotation.processor.ContentViewProcessor;

/**
 * @author Linbing Tang
 * @since 17-11-22.
 */
public class Zero {
  public static void bindContent(Activity activity){
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
