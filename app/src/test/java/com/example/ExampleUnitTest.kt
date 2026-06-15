package com.example

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun probe_csrf_endpoints() {
    val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    val urls = listOf(
        "https://www.jiuspeak.com.br/api/auth/csrf",
        "https://www.jiuspeak.com.br/api/csrf",
        "https://www.jiuspeak.com.br/api/csrf-token",
        "https://www.jiuspeak.com.br/api/auth/csrf-token",
        "https://www.jiuspeak.com.br/api/auth/session",
        "https://www.jiuspeak.com.br/api/auth/login",
        "https://www.jiuspeak.com.br/api/auth/me"
    )

    for (url in urls) {
        println("===============================================")
        println("PROBING URL: $url")
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                println("STATUS: ${response.code}")
                println("HEADERS:")
                response.headers.forEach { pair ->
                    println("  ${pair.first}: ${pair.second}")
                }
                val body = response.body?.string() ?: ""
                println("BODY: $body")
            }
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
        }
    }
    println("===============================================")
  }

  class LocalTestCookieJar : okhttp3.CookieJar {
    private val cookieStore = java.util.concurrent.ConcurrentHashMap<String, List<okhttp3.Cookie>>()
    override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
        val host = url.host
        val current = cookieStore[host]?.toMutableList() ?: mutableListOf()
        cookies.forEach { newCookie ->
            current.removeAll { it.name == newCookie.name }
            current.add(newCookie)
        }
        cookieStore[host] = current
    }
    override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
        val host = url.host
        return cookieStore[host] ?: emptyList()
    }
  }

  @Test
  fun test_real_csrf_login_validation() {
    val client = OkHttpClient.Builder()
        .cookieJar(LocalTestCookieJar()) // uses the simple local cookie jar for test environment
        .followRedirects(true)
        .build()

    val baseUrl = "https://www.jiuspeak.com.br"
    val csrfUrl = "$baseUrl/api/csrf-token"
    val loginUrl = "$baseUrl/api/auth/login"

    var csrfToken: String? = null
    var receivedCookies = ""

    // PASSO 1 & 2: GET /api/csrf-token
    println("\n=== PASS 1: GETTING CSRF TOKEN ===")
    val csrfRequest = Request.Builder().url(csrfUrl).get().build()
    client.newCall(csrfRequest).execute().use { response ->
        println("CSRF Status: ${response.code}")
        val body = response.body?.string() ?: ""
        println("CSRF Body: $body")
        
        val setCookies = response.headers("Set-Cookie")
        receivedCookies = setCookies.joinToString("; ")
        println("CSRF Set-Cookies: $receivedCookies")

        // Parse CSRF
        try {
            val json = com.google.gson.JsonParser.parseString(body).asJsonObject
            csrfToken = json.get("csrfToken")?.asString
        } catch (e: Exception) {
            println("Parse error: ${e.message}")
        }
    }

    assertNotNull("CSRF Token should be fetched", csrfToken)
    println("Using CSRF Token: $csrfToken")

    // PASSO 3: POST /api/auth/login (with actual headers and body, using mock test credentials)
    println("\n=== PASS 2: SUBMITTING LOGIN POST ===")
    val bodyJson = "{\"email\":\"invalid_user_for_test@jiuspeak.com\",\"password\":\"wrongpassword123\"}"
    val requestBody = okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), bodyJson)
    
    val postBuilder = Request.Builder()
        .url(loginUrl)
        .post(requestBody)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")

    csrfToken?.let { token ->
        postBuilder.header("X-XSRF-TOKEN", token)
        postBuilder.header("X-CSRF-TOKEN", token)
        postBuilder.header("x-xsrf-token", token)
        postBuilder.header("x-csrf-token", token)
    }

    val loginRequest = postBuilder.build()
    println("Headers sent under request:")
    loginRequest.headers.forEach { pair ->
        println("  ${pair.first}: ${pair.second}")
    }

    client.newCall(loginRequest).execute().use { response ->
        println("\n=== RESPONSE RECEIVED ===")
        println("HTTP CODE: ${response.code}")
        val body = response.body?.string() ?: ""
        println("RESPONSE BODY: $body")

        // Assert that we do NOT get a 403 CSRF inválido!
        // We expect a 401 or 404 for invalid credentials, showing CSRF is valid.
        assertNotEquals("Should not be blocked by CSRF (403)", 403, response.code)
        assertTrue("Expected credential rejection instead of CSRF block", response.code == 401 || response.code == 404)
    }
  }
}
