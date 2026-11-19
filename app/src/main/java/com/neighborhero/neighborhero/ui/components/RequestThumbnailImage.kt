package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.neighborhero.neighborhero.R
import java.util.Locale

object RequestCategoryImages {
    /**
     * Resolves an accurate category-specific placeholder drawable.
     * Guaranteed to never assign an image from another category.
     */
    fun getCategoryPlaceholder(category: String?): Int {
        val cat = category?.lowercase(Locale.ROOT)?.trim() ?: ""
        return when {
            cat.contains("grocer") -> R.drawable.img_thumb_supermarket
            cat.contains("book") || cat.contains("tutor") -> R.drawable.img_thumb_books
            cat.contains("repair") || cat.contains("yard") || cat.contains("garden") || cat.contains("handy") || cat.contains("fence") -> R.drawable.img_thumb_repairs
            cat.contains("transport") || cat.contains("ride") || cat.contains("moving") || cat.contains("car") || cat.contains("vehicle") -> R.drawable.img_thumb_transport
            cat.contains("pet") || cat.contains("dog") || cat.contains("cat") -> R.drawable.img_thumb_pets
            else -> R.drawable.img_thumb_community
        }
    }
}

/**
 * Standardized Help Request Thumbnail component.
 *
 * Priority:
 * 1. If requester uploaded a photo URL, display that exact photo via NetworkImage.
 * 2. If no photo exists or if loading fails, display the accurate category-specific placeholder.
 * 3. Never display an unrelated image or cross-contaminate cached images during scroll.
 */
@Composable
fun RequestThumbnailImage(
    category: String,
    photoUrl: String? = null,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val placeholderRes = RequestCategoryImages.getCategoryPlaceholder(category)

    if (!photoUrl.isNullOrBlank()) {
        NetworkImage(
            url = photoUrl,
            modifier = modifier,
            contentDescription = contentDescription ?: category,
            contentScale = contentScale,
            errorContent = {
                Image(
                    painter = painterResource(id = placeholderRes),
                    contentDescription = contentDescription ?: category,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            },
            fallback = {
                Image(
                    painter = painterResource(id = placeholderRes),
                    contentDescription = contentDescription ?: category,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    } else {
        Image(
            painter = painterResource(id = placeholderRes),
            contentDescription = contentDescription ?: category,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
