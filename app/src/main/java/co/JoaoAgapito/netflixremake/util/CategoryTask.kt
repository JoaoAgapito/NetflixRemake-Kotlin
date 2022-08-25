package co.JoaoAgapito.netflixremake.util

import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class CategoryTask {

    fun execute(url: String) {
        //aqui ainda está sendo executado na UI-Thread ( thread princial)
        val executor = Executors.newSingleThreadExecutor()


        executor.execute {
            try {


                //aqui em  baixo ele vai executar em uma thread separada
                val requestURL = URL(url) //abrir a URL
                val urlConnection =
                    requestURL.openConnection() as HttpURLConnection // Abrir a Conexão com o servidor
                urlConnection.readTimeout =
                    2000 // definir tempo de leitura (2s) + que isso dara erro
                urlConnection.connectTimeout = 2000 // tempo de conexão (2s) + que isso dara erro

                val statusCode: Int = urlConnection.responseCode // resposta do servidor
                if (statusCode > 400) {
                    throw  IOException ("Erro na comunicação com o servidor!") // verificando se deu erro
                }

                // servidor respondeu um sucesso:
                val stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() } // convertendo bytes em string
                Log.i("Teste", jsonAsString)

            } catch (e: IOException) {
                Log.e("Teste", e.message ?: "Erro desconhecido", e)

            }


        }
    }

}