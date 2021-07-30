package io.agora.meeting.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Example {
    /**
     * @return example index
     */
    int index();
    /**
     * @return group name
     */
    String group() default "";

    /**
     * @return example name
     */
    int nameStrId();

    /**
     * @return tips ID
     * */
    int tipStrId();
}
