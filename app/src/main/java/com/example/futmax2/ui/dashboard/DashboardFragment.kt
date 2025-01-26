package com.example.futmax2.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futmax2.databinding.FragmentDashboardBinding
import android.text.Editable
import android.text.TextWatcher
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.futmax2.R


class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Lista de unos nombres de ejemplo
        val nombres = listOf(
            "Alejandro", "Beatriz", "Carlos", "Daniela", "Eduardo", "Francisca", "Gabriel",
            "Helena", "Isabel", "Javier", "Karla", "Luis", "Marta", "Nicolás", "Olga", "Pablo",
            "Quintín", "Rosa", "Sofía", "Tomás", "Úrsula", "Valeria", "William", "Ximena", "Yolanda",
            "Zoe", "Andrés", "Blanca", "Cristina", "Diana", "Esteban", "Felipe", "Guillermo",
            "Héctor", "Inés", "Jorge", "Katia", "Luz", "Mariana", "Nerea", "Omar", "Patricia",
            "Gustavo", "Hilda", "Ignacio", "Jimena", "Karen", "Leonardo", "Manuela", "Natalia",
            "Fabio", "Gloria", "Hernán", "Irene", "José", "Karla", "Laura", "Mauricio", "Nora"
        )

        // Obtén el AutoCompleteTextView y el RecyclerView desde el binding
        val autoCompleteTextView = binding.autoCompleteTextView
        val recyclerView = binding.recyclerViewSuggestions

        // Crea el adaptador de sugerencias para el RecyclerView
        val suggestionAdapter = SuggestionAdapter(emptyList()) { suggestion ->
            autoCompleteTextView.setText(suggestion) // Muestra el nombre seleccionado en el AutoCompleteTextView
            recyclerView.visibility = View.GONE // Oculta el RecyclerView al seleccionar una sugerencia
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = suggestionAdapter

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitas hacer nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    // Filtra los nombres según el texto ingresado
                    val filteredSuggestions = nombres.filter { it.contains(query, ignoreCase = true) }
                    suggestionAdapter.updateSuggestions(filteredSuggestions)
                    recyclerView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No necesitas hacer nada aquí
            }
        })

        // Configura el botón Lupa con su listener
        val buttonLupa: ImageButton = binding.buttonLupa
        buttonLupa.setOnClickListener {
            // Aquí defines lo que quieres que haga el botón al hacer clic
            Toast.makeText(context, "Botón clicado", Toast.LENGTH_SHORT).show()
        }

        // Inicialización del mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuración inicial del mapa
        val initialLocation = LatLng(41.38879, 2.15899)
        mMap.addMarker(MarkerOptions().position(initialLocation).title("Marker in Barcelona"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }








}
