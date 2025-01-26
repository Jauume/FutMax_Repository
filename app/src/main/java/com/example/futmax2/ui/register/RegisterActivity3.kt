package com.example.futmax2.ui.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.futmax2.R
import com.example.futmax2.databinding.ActivityRegister3Binding
import android.widget.Button



class RegisterActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister3Binding
    private val PICK_IMAGE_REQUEST = 1 // Código para la galería
    private val TAKE_PHOTO_REQUEST = 2 // Código para la cámara
    private val PERMISSION_REQUEST_CODE_GALLERY = 100
    private val PERMISSION_REQUEST_CODE_CAMERA = 101

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

        // Botón atras

        val register_backbutton = findViewById<Button>(R.id.btn_back3)

        register_backbutton.setOnClickListener {
            val intent = Intent(this, RegisterActivity2::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
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
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        binding.profileImageView.setImageURI(selectedImageUri)
                    } else {
                        Toast.makeText(this, "No se pudo cargar la imagen.", Toast.LENGTH_SHORT).show()
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    val photo: Bitmap? = data?.extras?.get("data") as Bitmap?
                    if (photo != null) {
                        binding.profileImageView.setImageBitmap(photo)
                    } else {
                        Toast.makeText(this, "No se pudo capturar la foto.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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
}
