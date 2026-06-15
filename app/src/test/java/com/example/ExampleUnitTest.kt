package com.example

import com.example.data.network.JiuSpeakApi
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun retrofit_creation_is_successful() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.jiuspeak.com.br/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Eagerly parse all methods, parameter annotations, and Gson adapters to ensure no conversion issues
    val api = retrofit.newBuilder().validateEagerly(true).build().create(JiuSpeakApi::class.java)
    assertNotNull(api)
  }
}
