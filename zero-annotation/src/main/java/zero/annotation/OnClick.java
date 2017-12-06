package zero.annotation;

/**
 * @author Linbing Tang
 * @since 17-12-6.
 */
public @interface OnClick {
  int[] value() default {-1};
}
