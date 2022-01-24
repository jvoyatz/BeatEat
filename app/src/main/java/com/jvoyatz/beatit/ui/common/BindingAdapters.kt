package com.jvoyatz.beatit.ui.common

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jvoyatz.beatit.R

/**
 * custom xml attributes declared here
 */

@BindingAdapter("imgUrl")
fun bindImage(imgView: ImageView, imageUrl: String?){
    imageUrl?.let {
        val context = imgView.context
        Glide.with(context)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.ic_baseline_restaurant_24))
            .into(imgView)
    }
}