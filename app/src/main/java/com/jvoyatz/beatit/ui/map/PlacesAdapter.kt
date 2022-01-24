package com.jvoyatz.beatit.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jvoyatz.beatit.R
import com.jvoyatz.beatit.databinding.*
import com.jvoyatz.beatit.domain.Place
import com.jvoyatz.beatit.ui.map.DataItem.PlaceAdapterItem
import kotlinx.coroutines.*
import java.lang.IllegalStateException

private const val LOADING_ITEM = -1
private const val HEADER_ITEM = 0
private const val ADAPTER_ITEM = 1

private const val TAG = "PlacesAdapter"

class PlacesAdapter(
    val context: Context,
    val clickListener: (Place) -> Unit,
    val defaultScope: CoroutineScope): ListAdapter<DataItem, ViewHolder>(PlaceDiffUtil()) {


    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return when(item){
            is DataItem.Header -> HEADER_ITEM
            is PlaceAdapterItem -> ADAPTER_ITEM
            is DataItem.Loading -> LOADING_ITEM
            else -> throw IllegalStateException("what?")
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            HEADER_ITEM -> HeaderViewHolder.create(parent)
            LOADING_ITEM -> LoadingViewHolder.create(parent)
            ADAPTER_ITEM -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = FragmentPlaceItemBinding.inflate(inflater, parent, false)

                return PlaceItemViewHolder(binding){ position ->
                    when(val dataItem = getItem(position)){
                        is PlaceAdapterItem -> {
                            clickListener(dataItem.item)
                            val currentList = currentList.toMutableList()
                            currentList.removeAt(position)
                            when(currentList.size) {
                                1 -> submit(listOf())
                                else -> submitList(currentList)
                            }
                        }
                        else -> {}
                    }
                }
            }
            else -> throw IllegalStateException("unknown viewtype")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                val header = getItem(position) as DataItem.Header
                holder.bind(header.message)
            }
            is PlaceItemViewHolder -> {
                val placeItem = getItem(position) as PlaceAdapterItem
                holder.bind(placeItem.item)

            }
        }
    }

    fun submitLoading(){
        submitList(listOf(DataItem.Loading))
    }

    fun submit(places: List<Place> = listOf()){
        defaultScope.launch {
            delay(750)

            val items: List<DataItem> = when {
                places.isNullOrEmpty() -> {
                    DataItem.Header.message = context.getString(R.string.places_header_exhausted)
                    listOf(DataItem.Header)
                }
                else -> {
                    DataItem.Header.message = context.getString(R.string.places_header_results)
                    listOf(DataItem.Header) + places.map { PlaceAdapterItem(it) }
                }
            }

            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }
}

class PlaceItemViewHolder(val binding: FragmentPlaceItemBinding, clickPosition: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root){
    init {
        itemView.setOnClickListener {
            clickPosition(adapterPosition)
        }
    }
    fun bind(place: Place){
        binding.apply {
            this.place = place
            executePendingBindings()
        }
    }
}

class LoadingViewHolder(binding: LoadingItemBinding): RecyclerView.ViewHolder(binding.root){
    companion object {
        fun create(parent: ViewGroup): LoadingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LoadingItemBinding.inflate(inflater, parent, false)
            return LoadingViewHolder(binding)
        }
    }
}
class HeaderViewHolder(val binding: HeaderItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(message: String){
        binding.setText(message)
        binding.executePendingBindings()
    }

    companion object{
        fun create(parent: ViewGroup): HeaderViewHolder{
            val inflater = LayoutInflater.from(parent.context)
            val binding = HeaderItemBinding.inflate(inflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }
}


interface PlaceItemsListener{
    fun onClick(item: Place)
}

class PlaceDiffUtil: DiffUtil.ItemCallback<DataItem>(){
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        when{
            oldItem is PlaceAdapterItem &&
                    newItem is PlaceAdapterItem -> {
                       return oldItem.item == newItem.item
            }
            else -> {
                return oldItem == newItem
            }
        }
    }
}

sealed class DataItem{
    abstract val id: Int

    data class PlaceAdapterItem(val item: Place): DataItem(){
        override val id = 1
    }
    object Header: DataItem(){
        override val id = -234
        lateinit var message: String
    }
    object Loading: DataItem(){
        override val id = -235
    }
}