package co.JoaoAgapito.netflixremake.util

import android.os.Looper
import android.os.Message
import android.util.Log
import co.JoaoAgapito.netflixremake.model.Category
import co.JoaoAgapito.netflixremake.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.logging.Handler

class CategoryTask (private val callback: Callback){
    private val handler = android.os.Handler(Looper.getMainLooper())
    interface Callback {
        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()
        //aqui ainda está sendo executado na UI-Thread ( thread princial)
        val executor = Executors.newSingleThreadExecutor()


        executor.execute {

            var urlConnection: HttpURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null

            try {

                //aqui em  baixo ele vai executar em uma thread separada
                val requestURL = URL(url) //abrir a URL
                urlConnection = requestURL.openConnection() as HttpURLConnection // Abrir a Conexão com o servidor
                urlConnection.readTimeout = 2000 // definir tempo de leitura (2s) + que isso dara erro
                urlConnection.connectTimeout = 2000 // tempo de conexão (2s) + que isso dara erro

                val statusCode: Int = urlConnection.responseCode // resposta do servidor
                if (statusCode > 400) {
                    throw  IOException("Erro na comunicação com o servidor!") // verificando se deu erro
                }

                // servidor respondeu um sucesso:
                stream = urlConnection.inputStream
                // forma simples e rapida de fazer:
                //val jsonAsString = stream.bufferedReader().use { it.readText() } // convertendo bytes em string

                //Segunda forma de fazer:
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                val categories = toCategories(jsonAsString)

                handler.post {
                    //aqui roda dentro da UI-Thread novamente
                    callback.onResult(categories)
                }


            } catch (e: IOException) {
                val message = e.message ?: "Erro desconhecido"
                Log.e("Teste", message, e)

                handler.post {
                    callback.onFailure(message)
                }

            } finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toCategories(jsonAsString: String): List<Category> {
        val categories = mutableListOf<Category>()

        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")
        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)

            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")

            val movies = mutableListOf<Movie>()
            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                movies.add(Movie(id, coverUrl))
            }

            categories.add(Category(title, movies))
        }

        return categories
    }

    private fun toString(stream: InputStream): String {
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read: Int

        while (true) {
            read = stream.read(bytes)
            if (read <= 0) {
                break
            }
            baos.write(bytes, 0, read)
        }
        return String(baos.toByteArray())
    }

}