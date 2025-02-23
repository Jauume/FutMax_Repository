package com.example.futmax2.ui.register

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.futmax2.BuildConfig
import com.example.futmax2.R
import com.example.futmax2.databinding.ActivityRegister2Binding

// Google Places
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

// Fused Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



class RegisterActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister2Binding
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Adapter para mostrar las sugerencias de ciudades (texto completo)
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>

    // Listas para almacenar el nombre completo de la ciudad y su placeId en paralelo
    private val cityNamesList = mutableListOf<String>()
    private val placeIdList = mutableListOf<String>()

    // Variables para guardar las coordenadas de la ciudad seleccionada o de la ubicación actual
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 2) Inicializar Places
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(this)

        // 3) Inicializar el Adapter y asignarlo a nuestro AutoCompleteTextView
        autoCompleteAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            cityNamesList
        )
        binding.autocompleteCity.setAdapter(autoCompleteAdapter)

        // --- Escucha de cambios de texto para autocompletar ---
        binding.autocompleteCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* No lo usamos */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* No lo usamos */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.length >= 1) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        // .setCountries("ES") // Si quieres restringir a un país específico
                        .setQuery(query)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            cityNamesList.clear()
                            placeIdList.clear()

                            for (prediction in response.autocompletePredictions) {
                                // Agregamos el texto completo y el placeId
                                cityNamesList.add(prediction.getFullText(null).toString())
                                placeIdList.add(prediction.placeId)
                            }
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
                    // Si el usuario borra el texto, vaciamos
                    cityNamesList.clear()
                    placeIdList.clear()
                    autoCompleteAdapter.clear()
                    autoCompleteAdapter.notifyDataSetChanged()
                }
            }
        })

        // --- Manejo de selección de la lista autocompletada ---
        binding.autocompleteCity.setOnItemClickListener { _, _, position, _ ->
            val placeId = placeIdList[position]
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

        // Obtener el rol seleccionado del Intent (por ejemplo, "Jugador", "Entrenador", etc.)
        val selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "desconocido"

        // --- Botón Siguiente ---
        binding.btnSiguiente2.setOnClickListener {
            val nom_complet = binding.etNombreCompleto.text.toString()
            val data_naixement = "messi" // Simulación de fecha (solo para ejemplo)

            if (nom_complet.isNotEmpty()) {
                // Verifica que tengamos las coordenadas
                if (selectedLat == null || selectedLng == null) {
                    Toast.makeText(
                        this,
                        "Por favor, selecciona una ciudad válida o tu ubicación actual.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Adelante a Registro3.", Toast.LENGTH_SHORT).show()
                    navigateToRegisterActivity3(selectedRole, nom_complet, data_naixement)
                }
            } else {
                Toast.makeText(this, "Por favor, introduce todos los datos.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Botón Volver Atrás ---
        binding.btnBack2.setOnClickListener {
            val intent = Intent(this, RegisterActivity1::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // --- Botón para usar ubicación actual ---
        binding.btnCurrentLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    /**
     * Verifica si tenemos permiso de localización y, si es así, obtiene la ubicación;
     * si no, solicita los permisos.
     */
    private fun checkLocationPermission() {
        val permissionFine = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionCoarse = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ActivityCompat.checkSelfPermission(this, permissionFine) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, permissionCoarse) == PackageManager.PERMISSION_GRANTED
        ) {
            // Ya tenemos los permisos
            getCurrentLocation()
        } else {
            // Pedir permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissionFine, permissionCoarse),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Maneja la respuesta del usuario a la solicitud de permisos.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permiso denegado. No se puede obtener la ubicación.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Obtiene la ubicación actual del dispositivo usando FusedLocationProviderClient.
     */
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        selectedLat = location.latitude
                        selectedLng = location.longitude
                        Toast.makeText(
                            this,
                            "Ubicación actual: $selectedLat, $selectedLng",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "No se pudo determinar la ubicación actual (nulo).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al obtener ubicación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (ex: SecurityException) {
            // Este catch salta si no se tienen permisos en tiempo de ejecución
            Toast.makeText(this, "Error de permisos: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Navega hacia la siguiente pantalla de registro (RegisterActivity3),
     * pasando los datos necesarios (rol, nombre, fecha, lat, lng).
     */
    private fun navigateToRegisterActivity3(role: String, name: String, date: String) {
        val intent = Intent(this, RegisterActivity3::class.java).apply {
            putExtra("SELECTED_ROLE", role)
            putExtra("NAME", name)
            putExtra("DATE", date)
            putExtra("LATITUDE", selectedLat)
            putExtra("LONGITUDE", selectedLng)
        }
        startActivity(intent)
        finish()
    }
}
