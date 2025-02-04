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

    override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
        // Solo agrupa si hay 2 o más items
        return cluster.size > 1
    }

    // Para items sueltos
    override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
        // No llamamos a super para evitar el icono por defecto
        //super.onBeforeClusterItemRendered(item, markerOptions)

        // Descargamos la foto
        Glide.with(context)
            .asBitmap()
            .load(item.imageUrl)
            .circleCrop() // la recortamos circular
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Generamos la "chincheta" con color según rol + la foto centrada
                    val finalIcon = createPinWithPhoto(resource, item.rol)

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalIcon))
                    markerOptions.title(item.nickname)
                    markerOptions.snippet(item.rol)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    // Para clusters de varios items
    override fun onBeforeClusterRendered(cluster: Cluster<MyItem>, markerOptions: MarkerOptions) {
        // Podrías usar un círculo con el número, etc.
        val descriptor = makeClusterIcon(cluster.size)
        markerOptions.icon(descriptor)
    }

    /**
     * Crea un pin en color 'rolColor' con la foto circular en el centro
     */
    private fun createPinWithPhoto(photo: Bitmap, rol: String): Bitmap {
        // 1) Determinamos color según el rol
        val rolColor = when (rol) {
            "1" -> Color.BLUE
            "2" -> Color.RED
            else -> Color.GRAY
        }

        // 2) Tomamos una base (drawable) de una chincheta (ic_pin_base.png).
        //    Debe estar en res/drawable. Ajusta el nombre como quieras.
        val pinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_base)!!.mutate()

        // 3) Teñimos la chincheta con el color según rol
        pinDrawable.setTint(rolColor)

        val width = pinDrawable.intrinsicWidth
        val height = pinDrawable.intrinsicHeight
        pinDrawable.setBounds(0, 0, width, height)

        // 4) Creamos un bitmap + canvas para dibujar
        val pinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(pinBitmap)
        pinDrawable.draw(canvas)

        // 5) Colocamos la foto circular en la "cabeza" del pin
        //    Ajusta 'centerX' y 'centerY' para que se vea bien
        val centerX = width / 2
        val centerY = height / 4  // un cuarto de la altura, por ejemplo

        val photoSize = 80  // px
        val scaledPhoto = Bitmap.createScaledBitmap(photo, photoSize, photoSize, true)

        canvas.drawBitmap(
            scaledPhoto,
            (centerX - scaledPhoto.width/2).toFloat(),
            (centerY - scaledPhoto.height/2).toFloat(),
            null
        )

        return pinBitmap
    }

    /**
     * Crea un ícono para el cluster (cuando hay 2+ items)
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

        // Texto con el número
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
