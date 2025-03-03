import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.j_mabmobile.R
import com.squareup.picasso.Picasso

class ImageCarouselAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.carousel_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_carousel, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Picasso.get()
            .load(imageUrls[position])
            .placeholder(R.drawable.jmab_logo) // Placeholder image while loading
            .error(R.drawable.jmab_logo) // Error image if loading fails
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size
}
