package com.example.futmax2.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.futmax2.R
import com.example.futmax2.databinding.FragmentDashboardBinding
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.GetUsersMapInfoResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<MyItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer)
                as? SupportMapFragment
        mapFragment?.getMapAsync(this)
            ?: Log.e("DashboardFragment", "No se encontró el SupportMapFragment")

        // Botón info: mostrar la “leyenda”
        val buttonInfo = binding.buttonInfo
        val cardLegend = binding.cardLegend
        buttonInfo.setOnClickListener {
            cardLegend.visibility = if (cardLegend.visibility == View.VISIBLE)
                View.GONE
            else
                View.VISIBLE
        }



        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap




        // Inicializar clusterManager
        clusterManager = ClusterManager(requireContext(), mMap)
        val renderer = MyClusterRenderer(requireContext(), mMap, clusterManager)
        clusterManager.renderer = renderer

        // (NUEVO) Listener para hacer zoom al clicar un clúster:
        // Se sube un poco el zoom respecto al actual, sin exceder un máximo.
        clusterManager.setOnClusterClickListener { cluster ->
            val latLng = cluster.position
            val currentZoom = mMap.cameraPosition.zoom
            val maxZoom = 20f

            // Queremos aumentar, por ejemplo, 2 niveles, o quedarnos en 13 si es menor.
            val desiredZoom = 13f
            val finalZoom = if (currentZoom < desiredZoom) {
                desiredZoom
            } else {
                (currentZoom + 1f).coerceAtMost(maxZoom)
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, finalZoom))
            true
        }

        // Recalcular clusters al mover/zoom
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        // Intentar centrar en la ubicación real; si falla, fallback a Madrid
        centrarEnUbicacion()

        // Llamada a la API para añadir marcadores
        val service = ApiClient.getClient().create(ApiService::class.java)
        service.getUsersMapInfo().enqueue(object : Callback<GetUsersMapInfoResponse> {
            override fun onResponse(
                call: Call<GetUsersMapInfoResponse>,
                response: Response<GetUsersMapInfoResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        body.data.forEach { user ->
                            val lat = user.latitud
                            val lng = user.longitud
                            if (lat != null && lng != null) {
                                val item = MyItem(
                                    lat = lat,
                                    lng = lng,
                                    nickname = user.nickname,
                                    rol = user.rol,
                                    imageUrl = user.url_imatge_perfil
                                )
                                clusterManager.addItem(item)
                            }
                        }
                        clusterManager.cluster()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error en la respuesta del servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error HTTP: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetUsersMapInfoResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Fallo en la petición: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0 // Valor por defecto si no se encuentra
        }
    }

    /**
     * Intenta centrar el mapa en la ubicación actual del dispositivo.
     * Si no hay permisos o no se puede obtener ubicación, se centrará en Madrid.
     */
    private fun centrarEnUbicacion() {
        // Verificamos permisos (ACCESS_FINE_LOCATION o ACCESS_COARSE_LOCATION)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No tenemos permisos, centramos en Madrid como fallback
            moverCamaraMadrid()
            return
        }

        // Habilitamos la capa de “Mi ubicación” en el mapa (el puntito azul)
        mMap.isMyLocationEnabled = true

        // Obtenemos la última ubicación conocida con FusedLocationProvider
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12f))
                } else {
                    // Si no hay ubicación disponible
                    moverCamaraMadrid()
                }
            }
            .addOnFailureListener {
                moverCamaraMadrid()
            }
    }

    private fun moverCamaraMadrid() {
        val madridLatLng = LatLng(40.4167, -3.70325)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridLatLng, 10f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
