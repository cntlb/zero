package zero.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind a method to an {@link OnClickListener OnClickListener} on the view for each ID specified.
 * <pre><code>
 * {@literal @}OnClick(R.id.example) void onClick() {
 *   Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link OnClickListener#onClick(android.view.View) onClick} may be used on the
 * method.
 *
 * @author Linbing Tang
 * @since 17-12-6.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnClick {
  int[] value() default {-1};
}
