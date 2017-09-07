package ludwig.interpreter;

import ludwig.model.Visibilities;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visibility {
    Visibilities value();
}
