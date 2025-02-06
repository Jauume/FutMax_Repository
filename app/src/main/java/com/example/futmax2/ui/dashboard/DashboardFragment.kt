package com.example.futmax2.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.futmax2.R
import com.example.futmax2.databinding.FragmentDashboardBinding
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.GetUsersMapInfoResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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


        val mapFragment = childFragmentManager.findFragmentById(
            R.id.mapFragmentContainer
        ) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
            ?: Log.e("DashboardFragment", "No se encontró el SupportMapFragment")


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

        // 1) clusterManager
        clusterManager = ClusterManager(requireContext(), mMap)

        // 2) renderer personalizado
        val renderer = MyClusterRenderer(requireContext(), mMap, clusterManager)
        clusterManager.renderer = renderer

        // 3) Al mover/zoom, recalcular
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        // Centra en Barcelona
        val initialLocation = LatLng(41.38879, 2.15899)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // Llamada a API
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
