package com.example.futmax2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.futmax2.databinding.ActivityMainBinding
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.RetrofitClient
import com.example.futmax2.network.ApiResponse
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.UpdateLastConnectionRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.futmax2.ui.loginactivity.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si hay sesión iniciada
        if (!checkSession()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Actualizar la última conexión
        updateLastConnection()

        // Configurar la vista principal
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Quitar esto, ya que no tienes ActionBar por tema:
        //supportActionBar?.hide()

        // Configuración del BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_profile, R.id.navigation_messages)
        )

        // COMENTAR O QUITAR, porque no tenemos una ActionBar real:
        //setupActionBarWithNavController(navController, appBarConfiguration)

        // El BottomNavigation sí puede seguir usando Navigation:
        navView.setupWithNavController(navController)

        // Llamar a la API al iniciar
        fetchUsersFromApi()
    }


    // Método para verificar la sesión
    private fun checkSession(): Boolean {
        val sharedPref: SharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    private fun updateLastConnection() {
        val sharedPref: SharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname_key", null) ?: return

        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val request = UpdateLastConnectionRequest(nickname = nickname)

        Log.d("MainActivity", "Enviando solicitud para actualizar última conexión con usuario: $nickname")

        apiService.updateLastConnection(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("MainActivity", "Respuesta código: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("MainActivity", "Última conexión actualizada correctamente")
                } else {
                    Log.e("MainActivity", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MainActivity", "Error de red al actualizar última conexión: ${t.message}")
            }
        })
    }

    // Método para realizar la llamada a la API
    private fun fetchUsersFromApi() {
        Log.d("MainActivity", "Iniciando llamada a la API...")
        RetrofitClient.apiService.getUsers().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val users = response.body()?.data
                    users?.forEach { user ->
                        Log.d("MainActivity", "Usuario: ${user.nickname}, Contraseña: ${user.contraseña}")
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("MainActivity", "Error al conectar: ${t.message}")
            }
        })
    }
}
