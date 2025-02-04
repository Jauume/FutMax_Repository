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
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class RegisterActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister2Binding
    private lateinit var placesClient: PlacesClient

    // Adapter para mostrar las sugerencias de ciudades (texto completo)
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>

    // Listas para almacenar la ciudad (texto) y su placeId en paralelo
    private val cityNamesList = mutableListOf<String>()
    private val placeIdList = mutableListOf<String>()

    // Variables para guardar las coordenadas de la ciudad seleccionada
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Inicializar Places
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(this)

        // 2) Inicializar el Adapter y asignarlo a nuestro AutoCompleteTextView
        autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNamesList)
        binding.autocompleteCity.setAdapter(autoCompleteAdapter)

        // 3) Cuando el usuario escribe, buscamos predicciones
        binding.autocompleteCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* No usado */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* No usado */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.length >= 1) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        // Si quieres restringir a un país en concreto, descomenta:
                        // .setCountries("ES")
                        .setQuery(query)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            // Vaciamos las listas para actualizar con nuevos resultados
                            cityNamesList.clear()
                            placeIdList.clear()

                            for (prediction in response.autocompletePredictions) {
                                // Usamos el texto completo para mostrar en el dropdown
                                cityNamesList.add(prediction.getFullText(null).toString())
                                // Guardamos el placeId en la misma posición
                                placeIdList.add(prediction.placeId)
                            }

                            // Actualizamos el adaptador
                            autoCompleteAdapter.clear()
                            autoCompleteAdapter.addAll(cityNamesList)
                            autoCompleteAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@RegisterActivity2,
                                "Error en autocompletado: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Si el usuario borra todo, limpiamos
                    cityNamesList.clear()
                    placeIdList.clear()
                    autoCompleteAdapter.clear()
                    autoCompleteAdapter.notifyDataSetChanged()
                }
            }
        })

        // 4) Cuando el usuario selecciona una de las sugerencias
        binding.autocompleteCity.setOnItemClickListener { parent, view, position, id ->
            val placeId = placeIdList[position]  // Obtenemos el placeId correspondiente

            // Hacemos fetchPlace para obtener la lat/lng
            val placeFields = listOf(Place.Field.LAT_LNG)
            val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

            placesClient.fetchPlace(fetchPlaceRequest)
                .addOnSuccessListener { fetchPlaceResponse ->
                    val place = fetchPlaceResponse.place
                    val latLng = place.latLng
                    if (latLng != null) {
                        selectedLat = latLng.latitude
                        selectedLng = latLng.longitude
                        Toast.makeText(
                            this,
                            "Coordenadas seleccionadas: $selectedLat, $selectedLng",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@RegisterActivity2,
                        "No se pudo obtener el lugar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Obtener el rol seleccionado del Intent
        val selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "desconocido"

        // Botón siguiente
        binding.btnSiguiente2.setOnClickListener {
            val nom_complet = binding.etNombreCompleto.text.toString()
            val data_naixement = "messi" // Simulación de la fecha

            // Validamos que el nombre esté lleno
            if (nom_complet.isNotEmpty()) {
                // Validamos también que hayamos seleccionado una ciudad (lat/lng)
                if (selectedLat == null || selectedLng == null) {
                    Toast.makeText(
                        this,
                        "Por favor, selecciona una ciudad válida para obtener coordenadas.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Navegamos a la siguiente Activity
                    Toast.makeText(this, "Adelante a Registro3.", Toast.LENGTH_SHORT).show()
                    navigateToRegisterActivity3(selectedRole, nom_complet, data_naixement)
                }
            } else {
                Toast.makeText(this, "Por favor, introduce todos los datos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de volver atrás
        val backbutton = findViewById<Button>(R.id.btn_back2)
        backbutton.setOnClickListener {
            val intent = Intent(this, RegisterActivity1::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    // Pasa la info necesaria a la tercera pantalla del registro
    private fun navigateToRegisterActivity3(role: String, name: String, date: String) {
        val intent = Intent(this, RegisterActivity3::class.java).apply {
            putExtra("SELECTED_ROLE", role)
            putExtra("NAME", name)
            putExtra("DATE", date)
            // Añadimos lat y lng además del resto
            putExtra("LATITUDE", selectedLat)
            putExtra("LONGITUDE", selectedLng)
        }
        startActivity(intent)
        finish()
    }
}
