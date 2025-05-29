package org.danila.configuration.resilience4j

import org.springframework.stereotype.Component
import retrofit2.HttpException
import java.util.function.Predicate

@Component("spotifyServerErrorPredicate")
class Resilience4jRetryExceptionPredicate : Predicate<Throwable> {

    override fun test(t: Throwable): Boolean = t is HttpException && t.code() in 500..599

}