package zero.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind a layout to the Activity for the specified ID.
 * <pre>
 *   {@literal @}ContentView(R.layout.activity_main)
 *   public class MainActivity extends AppCompatActivity {
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ContentView {
  int value();
}
