package com.jordancalderwood.pizzatoppings

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.net.URI

@SpringBootTest
internal class PizzaToppingsApplicationTests {

	var testRestTemplate = TestRestTemplate()
	private fun applicationUrl() = "http://localhost:8080"
	@Test
	fun `test post submission endpoint`() {
		val toppingTestValue = "CRAZYTESTVALUE"
		testRestTemplate.exchange(
			URI(applicationUrl() + "/toppings"),
			HttpMethod.POST,
			HttpEntity(ToppingsSubmission("TESTEMAIL123@email.com", arrayOf(toppingTestValue))),
			Any::class.java
		)
		Thread.sleep(4000)

		val result = testRestTemplate.exchange(
			URI(applicationUrl() + "/toppingVotes"),
			HttpMethod.GET,
			HttpEntity(""),
			Array<ToppingVote>::class.java
		).body
		assert(result!!.any{ it.topping == toppingTestValue })
	}
}
