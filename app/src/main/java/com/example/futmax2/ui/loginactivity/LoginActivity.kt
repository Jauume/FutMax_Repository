package com.example.futmax2.ui.loginactivity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.futmax2.MainActivity
import com.example.futmax2.databinding.ActivityLoginBinding
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.ValidateUserRequest
import com.example.futmax2.network.ValidateUserResponse
import com.example.futmax2.network.UpdateLastLoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.example.futmax2.ui.register.RegisterActivity1



class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //esto sirve para eliminar la barra superior donde pone login
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el botón para validar usuario
        binding.buttonValidateUser.setOnClickListener {
            validateUser()
        }

        //aquí pongo el código de si se pulsa el botón de registrarse
        binding.buttonRegister.setOnClickListener{
            //Toast.makeText(this@LoginActivity, "Registro pulsado", Toast.LENGTH_SHORT).show()
            navigateToRegisterActivity()
        }
    }

    private fun validateUser() {
        val nickname = binding.editTextUsername.text.toString()
        val password = binding.editTextPassword.text.toString()

        if (nickname.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val request = ValidateUserRequest(nickname = nickname, contra = password)

        apiService.validateUser(request).enqueue(object : Callback<ValidateUserResponse> {
            override fun onResponse(call: Call<ValidateUserResponse>, response: Response<ValidateUserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val exists = response.body()?.exists ?: false
                    if (exists) {
                        // Guarda la sesión y actualiza última conexión
                        saveSession(nickname)
                        updateLastConnection(nickname) // Enviar la fecha a la API
                        navigateToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error al validar usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ValidateUserResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun updateLastConnection(username: String) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        // Crea un objeto con solo el nombre de usuario
        val request = UpdateLastLoginRequest(username)

        // Llamada a la API
        apiService.updateLastLogin(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("LoginActivity", "Último login actualizada correctamente")
                } else {
                    Log.e("LoginActivity", "Error en la respuesta del update Login: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("LoginActivity", "Error al actualizar último login: ${t.message}")
            }
        })
    }




    private fun saveSession(username: String) {
        val sharedPref: SharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nickname_key", username)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }


    //sirve para redirigirte a la página principal de la aplicación
    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //sirve para llevarte a la página de registro
    private fun navigateToRegisterActivity() {
        startActivity(Intent(this, RegisterActivity1::class.java))
        finish()
    }


}
