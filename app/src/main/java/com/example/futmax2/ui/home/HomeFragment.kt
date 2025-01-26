package com.example.futmax2.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.futmax2.databinding.FragmentHomeBinding
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.ValidateUserRequest
import com.example.futmax2.network.ValidateUserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
