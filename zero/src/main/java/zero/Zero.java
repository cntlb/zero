package zero;

import android.app.Activity;

import zero.annotation.processor.ContentViewProcessor;

/**
 * @author Linbing Tang
 * @since 17-11-22.
 */
public class Zero {

  public static void bind(Activity activity){
    try {
      String fullName = activity.getClass().getCanonicalName()+ ContentViewProcessor.SUFFIX;
      Class<?> zeroBind = Class.forName(fullName);
      AbsZeroBind bind = (AbsZeroBind) zeroBind.getConstructor().newInstance();

      //setContentView
      bind.setContentView(activity);

      //findViewById
      bind.findViewById(activity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
