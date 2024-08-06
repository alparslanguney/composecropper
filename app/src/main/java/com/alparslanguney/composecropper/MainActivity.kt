package com.alparslanguney.composecropper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.alparslanguney.composecropper.crop.CropContent
import com.alparslanguney.composecropper.ui.theme.ComposeCropperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeCropperTheme {
                val image = ImageBitmap.imageResource(id = R.drawable.cat)
                CropContent(image = image)
            }
        }
    }
}