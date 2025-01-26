package com.example.futmax2.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.futmax2.R
import com.example.futmax2.ui.loginactivity.LoginActivity

class RegisterActivity1 : AppCompatActivity() {

    private var selectedLayout: LinearLayout? = null // Mantiene el elemento seleccionado
    private var selectedRole: String? = null // Nombre del rol seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register1)

        // Obtén referencias a los LinearLayouts
        val jugadorLayout = findViewById<LinearLayout>(R.id.ll_jugador)
        val entrenadorLayout = findViewById<LinearLayout>(R.id.ll_entrenador)
        val preparadorLayout = findViewById<LinearLayout>(R.id.ll_preparador)
        val agenteLayout = findViewById<LinearLayout>(R.id.ll_agente)
        val analistaLayout = findViewById<LinearLayout>(R.id.ll_analista)
        val aficionadoLayout = findViewById<LinearLayout>(R.id.ll_aficionado)
        val clubLayout = findViewById<LinearLayout>(R.id.ll_club)
        val fisioLayout = findViewById<LinearLayout>(R.id.ll_fisio)
        val psicoLayout = findViewById<LinearLayout>(R.id.ll_psico)

        // Botón siguiente
        val nextButton = findViewById<Button>(R.id.btn_siguiente)

        // Configura el listener para cada LinearLayout
        jugadorLayout.setOnClickListener { onItemSelected(jugadorLayout, "Jugador") }
        entrenadorLayout.setOnClickListener { onItemSelected(entrenadorLayout, "Entrenador") }
        preparadorLayout.setOnClickListener { onItemSelected(preparadorLayout, "Preparador") }
        agenteLayout.setOnClickListener { onItemSelected(agenteLayout, "Agente") }
        analistaLayout.setOnClickListener { onItemSelected(analistaLayout, "Analista") }
        aficionadoLayout.setOnClickListener { onItemSelected(aficionadoLayout, "Aficionado") }
        clubLayout.setOnClickListener { onItemSelected(clubLayout, "Club") }
        fisioLayout.setOnClickListener { onItemSelected(fisioLayout, "Fisio") }
        psicoLayout.setOnClickListener { onItemSelected(psicoLayout, "Psicólogo") }

        // Listener para el botón "Siguiente"
        nextButton.setOnClickListener {
            if (selectedRole != null) {
                // Navegar a la siguiente actividad y pasar el rol seleccionado
                Toast.makeText(this, "Has seleccionado $selectedRole.", Toast.LENGTH_SHORT).show()
                navigateToRegisterActivity2(selectedRole!!)
            } else {
                Toast.makeText(this, "Por favor, selecciona un rol para continuar.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón atras

        val register2_backbutton = findViewById<Button>(R.id.btn_back1)

        register2_backbutton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

    }

    private fun onItemSelected(selected: LinearLayout, role: String) {
        try {
            // Evita realizar cambios si el elemento ya está seleccionado
            if (selectedLayout == selected) {
                Toast.makeText(this, "Ya estaba seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            // Si había algún rol seleccionado, deseleccionarlo
            selectedLayout?.isSelected = false

            // Marcar el nuevo rol como seleccionado
            selected.isSelected = true

            // Actualizar las referencias del elemento y rol seleccionado
            selectedLayout = selected
            selectedRole = role

            //Toast.makeText(this, "Rol seleccionado: $role", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al seleccionar el rol", Toast.LENGTH_SHORT).show()
        }
    }

    // ve a la segunda pantalla del registro y pasa el rol seleccionado
    private fun navigateToRegisterActivity2(role: String) {
        val intent = Intent(this, RegisterActivity2::class.java)
        intent.putExtra("SELECTED_ROLE", role) // Pasar el rol seleccionado
        startActivity(intent)
        finish()
    }
}
