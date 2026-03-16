package com.carlos.autoflow.foundation.image

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.request.ImageRequest

class FoundationImageLoader(context: Context) {
    private val imageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .build()

    fun load(
        url: String,
        target: ImageView,
        placeholderRes: Int? = null,
        errorRes: Int? = null,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        val request = ImageRequest.Builder(target.context)
            .data(url)
            .target(
                onSuccess = {
                    target.setImageDrawable(it)
                    onSuccess?.invoke()
                },
                onError = {
                    errorRes?.let { id -> target.setImageResource(id) }
                    onError?.invoke()
                }
            )
        placeholderRes?.let { request.placeholder(it) }
        errorRes?.let { request.error(it) }
        imageLoader.enqueue(request.build())
    }
}
