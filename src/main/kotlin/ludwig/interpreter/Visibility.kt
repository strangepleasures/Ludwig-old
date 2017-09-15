package ludwig.interpreter

import ludwig.model.Visibilities

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Visibility(val value: Visibilities)
