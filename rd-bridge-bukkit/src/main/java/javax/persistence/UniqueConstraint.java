package javax.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueConstraint {
    String name() default "";
    String[] columnNames();
}
