package com.example.futmax2.ui.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.futmax2.R
import com.example.futmax2.databinding.ActivityRegister3Binding
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.futmax2.MainActivity
import com.example.futmax2.network.ApiClient
import com.example.futmax2.network.ApiService
import com.example.futmax2.network.UpdateLastLoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.futmax2.network.RegisterUserRequest
import com.example.futmax2.network.RegisterUserResponse
import java.io.File
import java.io.FileOutputStream
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull





class RegisterActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister3Binding
    private val PICK_IMAGE_REQUEST = 1 // Código para la galería
    private val TAKE_PHOTO_REQUEST = 2 // Código para la cámara
    private val PERMISSION_REQUEST_CODE_GALLERY = 100
    private val PERMISSION_REQUEST_CODE_CAMERA = 101
    private var selectedImageFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val nom_complet = intent.getStringExtra("NAME") ?: "desconocido"
        val nom_sol = nom_complet.split(" ").firstOrNull() ?: "desconocido"

        // Actualizar el TextView con el mensaje personalizado
        binding.introduccio3.text = "Hola, $nom_sol. Por último, añade tu foto de perfil."

        // Configurar el botón para seleccionar o tomar una foto
        binding.selectPhotoButton.setOnClickListener {
            showImagePickerDialog()
        }



        // Obtener los datos del Intent
        val selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "desconocido"
        val name = intent.getStringExtra("NAME") ?: "desconocido"
        val date = intent.getStringExtra("DATE") ?: "desconocido"




        // Botón siguiente
        binding.btnSiguiente3.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            val password1 = binding.etContra1.text.toString()
            val password2 = binding.etContra2.text.toString()



            if (password1 != password2){
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()

            }
            else{
                registerUser(nickname, name, password1, selectedImageFile, selectedRole.toInt())
            }

        }




        // Botón atras
        val backbutton = findViewById<Button>(R.id.btn_back3)

        backbutton.setOnClickListener {
            val intent = Intent(this, RegisterActivity2::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }


    }






    private fun registerUser(nickname: String, name: String, password: String, imageFile: File?, role: Int) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        val nicknamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), nickname)
        val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val passwordPart = RequestBody.create("text/plain".toMediaTypeOrNull(), password)
        val rolePart = RequestBody.create("text/plain".toMediaTypeOrNull(), role.toString())

// Preparar la imagen como MultipartBody.Part
        val imagePart = if (imageFile != null) {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            MultipartBody.Part.createFormData("foto_perfil", imageFile.name, requestFile)
        } else null



        val latitud = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitud = intent.getDoubleExtra("LONGITUDE", 0.0)


        val latitudPart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitud.toString())
        val longitudPart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitud.toString())


        // Hacer la solicitud a la API
        apiService.registerUser(
            nickname = nicknamePart,
            name = namePart,
            contra = passwordPart,
            rolSelected = rolePart,
            foto_perfil = imagePart,
            latitud = latitudPart,
            longitud = longitudPart
        ).enqueue(object : Callback<RegisterUserResponse> {
            override fun onResponse(call: Call<RegisterUserResponse>, response: Response<RegisterUserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity3, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity(nickname)
                } else {
                    Toast.makeText(this@RegisterActivity3, "Error: ${response}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity3, "Fallo: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }









    // Mostrar diálogo para seleccionar entre galería o cámara
    private fun showImagePickerDialog() {
        val options = arrayOf("Desde galería", "Cámara")
        AlertDialog.Builder(this)
            .setTitle("Elige una opción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestGalleryPermission() // Seleccionar de galería
                    1 -> checkAndRequestCameraPermission()  // Tomar una foto
                }
            }
            .show()
    }

    // Verificar y solicitar permisos para la galería
    private fun checkAndRequestGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionExplanationDialog(
                    "Permiso necesario",
                    "Esta aplicación necesita acceso a la galería para seleccionar una foto de perfil.",
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    PERMISSION_REQUEST_CODE_GALLERY
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE_GALLERY
                )
            }
        } else {
            openGallery()
        }
    }

    // Verificar y solicitar permisos para la cámara
    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showPermissionExplanationDialog(
                    "Permiso necesario",
                    "Esta aplicación necesita acceso a la cámara para tomar una foto de perfil.",
                    Manifest.permission.CAMERA,
                    PERMISSION_REQUEST_CODE_CAMERA
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE_CAMERA
                )
            }
        } else {
            openCamera()
        }
    }

    // Mostrar un diálogo para explicar por qué se necesita el permiso
    private fun showPermissionExplanationDialog(
        title: String,
        message: String,
        permission: String,
        requestCode: Int
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    requestCode
                )
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Mostrar un diálogo para redirigir al usuario a la configuración
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("Los permisos son necesarios para usar esta funcionalidad. Por favor, actívelos en la configuración de la aplicación.")
            .setPositiveButton("Configuración") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Abrir la galería
    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } else {
            Toast.makeText(this, "Permiso para galería no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Abrir la cámara
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        } else {
            Toast.makeText(this, "Permiso para cámara no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Manejar la respuesta de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_REQUEST_CODE_GALLERY -> openGallery()
                PERMISSION_REQUEST_CODE_CAMERA -> openCamera()
            }
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                // El permiso fue denegado con "No volver a preguntar"
                showSettingsDialog()
            } else {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Manejar el resultado de la selección de imagen o la captura de foto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    // Imagen seleccionada desde la galería
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        binding.profileImageView.setImageBitmap(bitmap)
                        // Convertir el URI a un archivo
                        selectedImageFile = uriToFile(it, this)
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    // Imagen tomada con la cámara
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.profileImageView.setImageBitmap(imageBitmap)
                    // Convertir el Bitmap a un archivo
                    selectedImageFile = bitmapToFile(imageBitmap, "profile_image.jpg")
                }
            }
        }
    }


    fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }


    // Verificar permisos en `onResume`
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permisos habilitados", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Por favor, habilita los permisos para continuar", Toast.LENGTH_SHORT).show()
        }
    }




    private fun updateLastConnection(username: String) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        // Crea un objeto con solo el nombre de usuario
        val request = UpdateLastLoginRequest(username)

        // Llamada a la API
        apiService.updateLastLogin(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("LoginActivity", "Último login actualizada correctamente")
                } else {
                    Log.e("LoginActivity", "Error en la respuesta del update Login: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("LoginActivity", "Error al actualizar último login: ${t.message}")
            }
        })
    }




    private fun saveSession(username: String) {
        val sharedPref: SharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nickname_key", username)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }



    //sirve para redirigirte a la página principal de la aplicación
    private fun navigateToMainActivity(username: String) {
        Toast.makeText(this, "Navigating to main activity", Toast.LENGTH_SHORT).show()
        saveSession(username)
        updateLastConnection(username)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }



}
