package org.danila.config

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring

@DgsComponent
class ScalarsWiring {
    @DgsRuntimeWiring
    fun addLongScalar(builder: RuntimeWiring.Builder): RuntimeWiring.Builder =
        builder.scalar(ExtendedScalars.GraphQLLong)
}