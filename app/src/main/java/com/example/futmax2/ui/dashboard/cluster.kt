package com.example.futmax2.ui.dashboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.futmax2.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

// -------------------------------
// 1) MyItem: cada usuario
class MyItem(
    val lat: Double,
    val lng: Double,
    val nickname: String,
    val rol: String,
    val imageUrl: String
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat, lng)
    override fun getTitle(): String = nickname
    override fun getSnippet(): String = rol
    override fun getZIndex(): Float = 0f
}

// -------------------------------
// 2) MyClusterRenderer: personaliza el icono
class MyClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyItem>
) : DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

    /**
     * Decide si se hace cluster o no. Aquí decimos que solo se agrupe
     * si hay 2 o más usuarios juntos.
     */
    override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
        return cluster.size > 1
    }

    /**
     * Se llama antes de “construir” el marcador de un ítem individual.
     * Aquí podemos poner un ícono base (sin foto) para no mostrar el
     * ícono por defecto de Google.
     */
    override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
        // Icono de “placeholder” solo con el color según rol.
        val colorPin = when (item.rol) {
            "1" -> Color.BLUE     // Jugador
            "2" -> Color.RED      // Entrenador
            else -> Color.GRAY
        }

        val pinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_base)!!.mutate()
        pinDrawable.setTint(colorPin)

        val width = pinDrawable.intrinsicWidth
        val height = pinDrawable.intrinsicHeight
        pinDrawable.setBounds(0, 0, width, height)

        val pinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(pinBitmap)
        pinDrawable.draw(canvas)

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
        markerOptions.title(item.nickname)
        markerOptions.snippet(item.rol)
    }

    /**
     * Se llama cuando el ítem individual ya está renderizado.
     * Aquí es donde hacemos la carga asíncrona con Glide y,
     * una vez descargada la foto, actualizamos el icono.
     */
    override fun onClusterItemRendered(clusterItem: MyItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)

        // Cargar la foto con Glide (asíncrono)
        Glide.with(context)
            .asBitmap()
            .load(clusterItem.imageUrl)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val finalIcon = createPinWithPhoto(resource, clusterItem.rol)
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(finalIcon))
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    /**
     * Se llama antes de “construir” el marcador de un cluster (2+ ítems).
     * Aquí puedes dibujar un ícono que muestre el número de usuarios.
     */
    override fun onBeforeClusterRendered(cluster: Cluster<MyItem>, markerOptions: MarkerOptions) {
        val descriptor = makeClusterIcon(cluster.size)
        markerOptions.icon(descriptor)
    }

    /**
     * Dibuja un pin en color `rolColor` con la foto circular sobre el “cabezal”.
     */
    private fun createPinWithPhoto(photo: Bitmap, rol: String): Bitmap {
        val rolColor = when (rol) {
            "1" -> Color.BLUE //jugador
            "2" -> Color.RED //

            else -> Color.GRAY
        }

        // 1) Pin base
        val pinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_base)!!.mutate()
        pinDrawable.setTint(rolColor)

        val width = pinDrawable.intrinsicWidth
        val height = pinDrawable.intrinsicHeight
        pinDrawable.setBounds(0, 0, width, height)

        val pinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(pinBitmap)
        pinDrawable.draw(canvas)

        // 2) Superponer la foto circular en el centro del “cabezal”
        val centerX = width / 2
        val centerY = height / 4  // Ajusta según tu pin
        val photoSize = 80 // px
        val scaledPhoto = Bitmap.createScaledBitmap(photo, photoSize, photoSize, true)

        canvas.drawBitmap(
            scaledPhoto,
            (centerX - scaledPhoto.width / 2).toFloat(),
            (centerY - scaledPhoto.height / 2).toFloat(),
            null
        )

        return pinBitmap
    }

    /**
     * Dibuja el ícono de cluster (un círculo verde con el número de ítems).
     */
    private fun makeClusterIcon(clusterSize: Int): BitmapDescriptor {
        val sizePx = 80
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Círculo
        val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paintCircle)

        // Texto
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = (sizePx / 2f) - ((paintText.descent() + paintText.ascent()) / 2f)
        canvas.drawText(clusterSize.toString(), sizePx / 2f, textY, paintText)

        return BitmapDescriptorFactory.fromBitmap(bmp)
    }
}
