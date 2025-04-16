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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest(classes = [UtilsJacksonConfiguration::class])
class UtilsJacksonConfigurationTest @Autowired constructor(
    @Qualifier("kafkaObjectMapper") private val objectMapper: ObjectMapper
) {

    data class TestClass(
        val name: String? = null,
        val list: List<String> = emptyList(),
        val timestamp: Instant? = null
    )

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

    @Test
    fun `should serialize Instant to ISO-8601 format`() {
        val json = objectMapper.writeValueAsString(TestClass(name = "test", list = listOf("value"), timestamp = Instant.parse("2025-03-27T12:34:56Z")))
        org.assertj.core.api.Assertions.assertThat(json).contains("2025-03-27T12:34:56Z")
    }

    @Test
    fun `should deserialize ISO-8601 formatted date to Instant`() {
        val result = objectMapper.readValue("""{"name":"test","list":["value"],"timestamp":"2025-03-27T12:34:56Z"}""", TestClass::class.java)
        assertThat(result.timestamp).isEqualTo(Instant.parse("2025-03-27T12:34:56Z"))
    }

}