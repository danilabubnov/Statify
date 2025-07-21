package logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun logger(): ReadOnlyProperty<Any, KLogger> =
    object : ReadOnlyProperty<Any, KLogger> {
        override fun getValue(thisRef: Any, property: KProperty<*>): KLogger {
            return KotlinLogging.logger(thisRef::class.java.name)
        }
    }