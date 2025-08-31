package org.danila.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoEdgeSpacesValidator::class])
annotation class NoEdgeSpaces(
    val message: String = "Field cannot start or end with spaces",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NoEdgeSpacesValidator : ConstraintValidator<NoEdgeSpaces, String> {
    override fun isValid(value: String?, ctx: ConstraintValidatorContext) =
        value == null || value == value.trim()
}

