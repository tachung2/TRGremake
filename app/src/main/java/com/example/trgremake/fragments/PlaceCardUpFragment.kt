import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trgremake.Place
import com.example.trgremake.R
import com.example.trgremake.RouteActivity

class PlaceCardUpFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var placesAdapter: PlacesAdapter
    private var fullPlacesList: List<Place> = listOf()
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_pop_up_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.placeRCV)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        placesAdapter = PlacesAdapter(emptyList())
        recyclerView.adapter = placesAdapter
        titleTextView = view.findViewById(R.id.title)

    }

    fun updatePlacesList(places: List<Place>) {
        placesAdapter.updatePlaces(places)
    }

    fun updatePlacesQuery(query: String) {
        titleTextView.text = query
    }

    private inner class PlacesAdapter(private var places: List<Place>) :
        RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

        fun updatePlaces(newPlaces: List<Place>) {
            this.places = newPlaces
            notifyDataSetChanged()
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_place_pop_up_list_dialog_item, parent, false)
            return PlaceViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
            val place = places[position]
            holder.bind(place)

            holder.itemView.setOnClickListener {
                (activity as? RouteActivity)?.moveToLocationAndAddMarker(place)
            }
        }

        override fun getItemCount() = places.size

        inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.placeName)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.placeDescription)
            private val imageView: ImageView = itemView.findViewById(R.id.placeImage)

            fun bind(place: Place) {
                nameTextView.text = trimText(place.title, 20)
                descriptionTextView.text = trimText(place.address, 50)
                Glide.with(itemView.context)
                    .load(place.image)
                    .into(imageView)

                // 여기서 클릭 리스너를 설정하여 팝업을 띄울 수 있습니다.
                itemView.setOnClickListener {
                    (activity as? RouteActivity)?.moveToLocationAndAddMarker(place)
                }
            }


            private fun trimText(text: String, maxLength: Int): String {
                return if (text.length > maxLength) text.substring(0, maxLength) + "..." else text
            }
        }
    }
}
