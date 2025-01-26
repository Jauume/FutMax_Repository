package com.example.futmax2.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Interfaz para definir los endpoints de la API
interface ApiService {
    @GET("usuaris/get_users") // Endpoint para obtener todos los usuarios
    fun getUsers(): Call<ApiResponse>

    @POST("usuaris/validate_user")
    fun validateUser(@Body request: ValidateUserRequest): Call<ValidateUserResponse>

    @POST("usuaris/update_login_time")
    fun updateLastLogin(
        @Body request: UpdateLastLoginRequest
    ): Call<Void>


    @POST("usuaris/update_last_connection")
    fun updateLastConnection(@Body request: UpdateLastConnectionRequest): Call<Void>


    @POST("usuaris/get_userprofile_image")
    fun getUserProfileImage(@Body request: UserImageProfileRequest): Call<UserImageProfileResponse>


    @POST("seguidors/follow_user")
    fun updateFollowers(@Body request: UserImageProfileRequest): Call<UserImageProfileResponse>


    @POST("/seguidors/stats")
    fun getFollowStats(@Body request: FollowStatsRequest): Call<FollowStatsResponse>


}

// Clase para mapear la respuesta de la API
data class ApiResponse(
    val success: Boolean,
    val data: List<User>
)

// Clase para representar un usuario
data class User(
    val id: Int,
    val nickname: String,
    val contraseña: String
)

data class UpdateLastLoginRequest(
    val nickname: String,
)

data class UpdateLastConnectionRequest(
    val nickname: String,
)


// representar la solicitud de imagen de perfil
data class UserImageProfileRequest(
    val nickname: String // Nombre del usuario
)

// mapear la respuesta de la imagen de perfil
data class UserImageProfileResponse(
    val success: Boolean,
    val url_imatge_perfil: String? = null,
    val message: String? = null
)

// Solicitud para pedir el numero de seguidores/seguidos
data class FollowStatsRequest(
    val nickname: String
)

// Respuesta de cuantos seguidres/seguidos hay
data class FollowStatsResponse(
    val success: Boolean,
    val followers_count: Int? = null,
    val following_count: Int? = null,
    val message: String? = null
)

