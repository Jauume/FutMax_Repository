package com.example.futmax2.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.futmax2.BuildConfig
import com.example.futmax2.R
import com.example.futmax2.databinding.ActivityRegister2Binding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class RegisterActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister2Binding

    // Adapter para ir actualizando sugerencias
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Inicializar Places
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(this)

        // 2) Configurar el AutoCompleteTextView
        autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        binding.autocompleteCity.setAdapter(autoCompleteAdapter)

        // Cuando el usuario escribe, buscamos predicciones
        binding.autocompleteCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // de momento nada
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // de momento nada
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.length >= 1) {
                    // Llamamos a la API de autocompletado
                    val request = FindAutocompletePredictionsRequest.builder()
                        // para restringir un país, descomenta:
                        // .setCountries("ES")
                        .setQuery(query)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            val suggestions = response.autocompletePredictions
                            // Transformamos a texto completo
                            val newData = suggestions.map { it.getFullText(null).toString() }
                            // Actualizamos el Adapter
                            autoCompleteAdapter.clear()
                            autoCompleteAdapter.addAll(newData)
                            autoCompleteAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            // Manejo de error
                            Toast.makeText(this@RegisterActivity2,
                                "Error en autocompletado: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }
            }
        })

        /*
        binding.autocompleteCity.setOnItemClickListener { parent, view, position, id ->
            val selectedText = autoCompleteAdapter.getItem(position)

            Toast.makeText(this@RegisterActivity2,
                "Has seleccionado: $selectedText",
                Toast.LENGTH_SHORT).show()
        }
        */



        // Obtener el rol seleccionado del Intent
        val selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "desconocido"



        // Botón siguiente
        binding.btnSiguiente2.setOnClickListener {
            val nom_complet = binding.etNombreCompleto.text.toString()
            val data_naixement = "messi" // Simulación

            if (nom_complet.isNotEmpty()) {
                Toast.makeText(this, "Adelante a Registro3.", Toast.LENGTH_SHORT).show()
                navigateToRegisterActivity3(selectedRole, nom_complet, data_naixement)
            } else {
                Toast.makeText(this, "Por favor, introduce todos los datos.", Toast.LENGTH_SHORT).show()
            }
        }

        val backbutton = findViewById<Button>(R.id.btn_back2)

        backbutton.setOnClickListener {
            val intent = Intent(this, RegisterActivity1::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }


    }

    // ve a la tercera pantalla del registro y pasa la información seleccionada
    private fun navigateToRegisterActivity3(role: String, name: String, date: String) {
        val intent = Intent(this, RegisterActivity3::class.java)
        intent.putExtra("SELECTED_ROLE", role)
        intent.putExtra("NAME", name)
        intent.putExtra("DATE", date)
        startActivity(intent)
        finish()
    }
}
