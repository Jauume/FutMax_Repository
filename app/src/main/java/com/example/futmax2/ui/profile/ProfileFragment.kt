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
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.example.futmax2.R
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.FollowStatsRequest
import com.example.futmax2.network.FollowStatsResponse


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
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

        // En vez de crear un nuevo Retrofit, usamos el que ya definiste en ApiClient
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val request = UserImageProfileRequest(nickname = nickname)

        // Llamar a la API
        apiService.getUserProfileImage(request).enqueue(object : Callback<UserImageProfileResponse> {
            override fun onResponse(
                call: Call<UserImageProfileResponse>,
                response: Response<UserImageProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile?.success == true) {
                        // Actualizar la foto de perfil
                        Glide.with(this@ProfileFragment)
                            .load(userProfile.url_imatge_perfil)
                            .placeholder(R.drawable.imatge_perfil)
                            .error(R.drawable.imatge_perfil)
                            .into(binding.profileImageView)

                        // Actualizar el nickname de usuario
                        binding.usernameTextView.text = nickname
                    } else {
                        binding.usernameTextView.text = "Usuario no encontrado"
                    }
                }
            }

            override fun onFailure(call: Call<UserImageProfileResponse>, t: Throwable) {
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
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats?.success == true) {
                        // Actualizamos los TextViews
                        binding.textViewSeguidoresCount.text = stats.followers_count.toString()
                        binding.textViewSeguidosCount.text = stats.following_count.toString()
                    } else {
                        // Manejar error, por ejemplo stats.message
                        binding.textViewSeguidoresCount.text = "-1"
                        binding.textViewSeguidosCount.text = "-2"
                    }
                } else {
                    binding.textViewSeguidoresCount.text = "-3"
                    binding.textViewSeguidosCount.text = "-4"
                }
            }

            override fun onFailure(call: Call<FollowStatsResponse>, t: Throwable) {
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

        // Cerrar la actividad actual
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
