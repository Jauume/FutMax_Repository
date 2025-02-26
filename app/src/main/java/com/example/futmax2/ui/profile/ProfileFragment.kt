package com.example.futmax2.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.futmax2.databinding.FragmentProfileBinding
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.UserImageProfileRequest
import com.example.futmax2.network.UserImageProfileResponse
import com.example.futmax2.ui.loginactivity.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.futmax2.R
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.FollowStatsRequest
import com.example.futmax2.network.FollowStatsResponse

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // Al usar ‘binding’ con get() = _binding!!,
    // es fundamental asegurarse de que _binding no sea null al acceder.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Botón logout
        binding.buttonLogout.setOnClickListener {
            logout()
        }

        // Cargar la foto/nickname de usuario
        fetchUserProfile()

        // Cargar el número de seguidores/seguidos
        fetchFollowStats()

        return root
    }

    private fun fetchUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname_key", "") ?: ""

        // Usamos la instancia de Retrofit de ApiClient
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val request = UserImageProfileRequest(nickname = nickname)

        apiService.getUserProfileImage(request).enqueue(object : Callback<UserImageProfileResponse> {
            override fun onResponse(
                call: Call<UserImageProfileResponse>,
                response: Response<UserImageProfileResponse>
            ) {
                // Verificamos que el fragment siga “vivo” (no se haya destruido la vista)
                if (!isAdded || _binding == null) return

                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile?.success == true) {
                        // Actualizamos la foto de perfil con Glide
                        Glide.with(this@ProfileFragment)
                            .load(userProfile.url_imatge_perfil)
                            .placeholder(R.drawable.imatge_perfil)
                            .error(R.drawable.imatge_perfil)
                            .into(binding.profileImageView)

                        // Actualizamos el nickname
                        binding.usernameTextView.text = nickname
                    } else {
                        binding.usernameTextView.text = "Usuario no encontrado"
                    }
                } else {
                    // Maneja el caso de respuesta no exitosa
                    binding.usernameTextView.text = "Error en la respuesta"
                }
            }

            override fun onFailure(call: Call<UserImageProfileResponse>, t: Throwable) {
                // Verificamos que el fragment siga “vivo”
                if (!isAdded || _binding == null) return

                binding.usernameTextView.text = "Error de conexión"
                t.printStackTrace()
            }
        })
    }

    private fun fetchFollowStats() {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname_key", "") ?: ""

        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val request = FollowStatsRequest(nickname = nickname)

        apiService.getFollowStats(request).enqueue(object : Callback<FollowStatsResponse> {
            override fun onResponse(
                call: Call<FollowStatsResponse>,
                response: Response<FollowStatsResponse>
            ) {
                // De nuevo, comprobamos que el fragment siga existiendo
                if (!isAdded || _binding == null) return

                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats?.success == true) {
                        binding.textViewSeguidoresCount.text = stats.followers_count.toString()
                        binding.textViewSeguidosCount.text = stats.following_count.toString()
                    } else {
                        // Maneja el caso de error en la respuesta de la API
                        binding.textViewSeguidoresCount.text = "-1"
                        binding.textViewSeguidosCount.text = "-2"
                    }
                } else {
                    binding.textViewSeguidoresCount.text = "-3"
                    binding.textViewSeguidosCount.text = "-4"
                }
            }

            override fun onFailure(call: Call<FollowStatsResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return

                t.printStackTrace()
                binding.textViewSeguidoresCount.text = "-5"
                binding.textViewSeguidosCount.text = "-6"
            }
        })
    }

    private fun logout() {
        // Eliminar la sesión guardada
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        // Redirigir a LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Importante: liberamos la referencia al binding para evitar fugas de memoria
        _binding = null
    }
}
