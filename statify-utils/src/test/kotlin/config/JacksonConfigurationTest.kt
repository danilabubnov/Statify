package config

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [JacksonConfiguration::class])
class JacksonConfigurationTest @Autowired constructor(
    private val objectMapper: ObjectMapper
) {

    data class TestClass(val name: String? = null, val list: List<String> = emptyList())

    @Test
    fun `should configure SerializationFeature WRITE_DATES_AS_TIMESTAMPS to false`() {
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse()
    }

    @Test
    fun `should configure DeserializationFeature FAIL_ON_UNKNOWN_PROPERTIES to false`() {
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse()
    }

    @Test
    fun `should have NON_NULL serialization inclusion`() {
        assertThat(objectMapper.serializationConfig.serializationInclusion).isEqualTo(JsonInclude.Include.NON_NULL)
    }

    @Test
    fun `should disable SerializationFeature FAIL_ON_EMPTY_BEANS`() {
        assertThat(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse()
    }

    @Test
    fun `should convert null collection fields to empty collections`() {
        val json = """{"name":"test"}"""
        val result = objectMapper.readValue(json, TestClass::class.java)
        assertThat(result.list).isEqualTo(emptyList())
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json = """{"name":"test", "unknown":"field"}"""
        val result = objectMapper.readValue(json, TestClass::class.java)
        assertThat(result.name).isEqualTo("test")
    }
}