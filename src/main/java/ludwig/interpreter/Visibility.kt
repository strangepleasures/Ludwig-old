package ludwig.interpreter

import ludwig.model.Visibilities

import java.lang.annotation.*

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Visibility(val value: Visibilities)
