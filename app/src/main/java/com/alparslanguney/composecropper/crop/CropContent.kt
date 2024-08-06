package com.alparslanguney.composecropper.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alparslanguney.composecropper.R
import com.alparslanguney.composecropper.ui.theme.ComposeCropperTheme
import kotlinx.coroutines.launch

/**
 * Created by Alparslan Güney 30.07.2024
 * Contact : seminihi@gmail.com
 */
@Composable
fun CropContent(
    modifier: Modifier = Modifier,
    image: ImageBitmap
) {

    val coroutineScope = rememberCoroutineScope()

    var croppedImage by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val density = LocalDensity.current.density

    val cropZoneSize = remember {
        Size(
            width = 215.times(density),
            height = 275.times(density)
        )
    }

    var scale by remember {
        mutableFloatStateOf(1f)
    }

    var transform by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    var isPreviewImageDialogShow by remember {
        mutableStateOf(false)
    }

    if (isPreviewImageDialogShow) {
        Dialog(onDismissRequest = {
            isPreviewImageDialogShow = false
        }) {
            Box(modifier = Modifier.background(
                color = Color.White,
                shape = MaterialTheme.shapes.medium
            )) {
                croppedImage?.let {
                    Image(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.medium),
                        bitmap = it,
                        contentDescription = "Cropped Image"
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = croppedImage) {
        if (croppedImage != null) {
            isPreviewImageDialogShow = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        CropZone(
            modifier = Modifier.fillMaxSize(),
            cropZoneSize = cropZoneSize,
            image = image,
            onScaleChange = {
                scale = it
            },
            onTransformChange = {
                transform = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.75f),
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = painterResource(id = R.drawable.ic_crop),
                onClick = {
                    coroutineScope.launch {
                        croppedImage = cropImage(
                            targetBitmap = image,
                            cropSize = cropZoneSize,
                            transform = transform,
                            scale = scale
                        ).asImageBitmap()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun cropImage(
    targetBitmap: ImageBitmap,
    cropSize: Size,
    transform: Offset,
    scale: Float
): Bitmap {
    val matrix = Matrix()

    matrix.postScale(
        scale,
        scale
    )

    val scaledBitmap = Bitmap.createBitmap(
        targetBitmap.asAndroidBitmap(),
        0,
        0,
        targetBitmap.width,
        targetBitmap.height,
        matrix,
        true
    )

    val cropX = scaledBitmap.width.minus(cropSize.width).div(2)
        .minus(transform.x).toInt()
    val cropY = scaledBitmap.height.minus(cropSize.height).div(2)
        .minus(transform.y).toInt()

    val croppedBitmap = Bitmap.createBitmap(
        scaledBitmap,
        cropX,
        cropY,
        cropSize.width.toInt(),
        cropSize.height.toInt()
    )

    return croppedBitmap
}

@Composable
private fun ActionButton(
    modifier: Modifier,
    icon: Painter,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.small,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    tint = Color.White,
                    painter = icon,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier,
                    text = "Crop",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun CropZone(
    modifier: Modifier,
    cropZoneSize: Size,
    image: ImageBitmap,
    onScaleChange: (Float) -> Unit = {},
    onTransformChange: (Offset) -> Unit = {}
) {

    var scale by remember {
        mutableFloatStateOf(1f)
    }

    var transform by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val constraintWidth = constraints.maxWidth
        val constraintHeight = constraints.maxHeight

        val rectDraw = remember {
            Rect(
                offset = Offset(
                    x = constraintWidth.div(2)
                        .minus(cropZoneSize.width.div(2)),
                    y = constraintHeight.div(2)
                        .minus(cropZoneSize.height.div(2))
                ),
                size = cropZoneSize
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->

                        val transformRestrictionTransformXMax =
                            image.width
                                .times(scale)
                                .minus(cropZoneSize.width)
                                .div(2)
                        val transformRestrictionTransformXMin =
                            transformRestrictionTransformXMax.unaryMinus()

                        val transformRestrictionTransformYMax =
                            image.height
                                .times(scale)
                                .minus(cropZoneSize.height)
                                .div(2)
                        val transformRestrictionTransformYMin =
                            transformRestrictionTransformYMax.unaryMinus()

                        scale = scale
                            .times(zoom)
                            .coerceIn(
                                minimumValue = 1f,
                                maximumValue = 10f
                            )
                        transform = Offset(
                            transform.x
                                .plus(pan.x)
                                .coerceIn(
                                    minimumValue = transformRestrictionTransformXMin,
                                    maximumValue = transformRestrictionTransformXMax
                                ),
                            transform.y
                                .plus(pan.y)
                                .coerceIn(
                                    minimumValue = transformRestrictionTransformYMin,
                                    maximumValue = transformRestrictionTransformYMax
                                )
                        )
                        onScaleChange(scale)
                        onTransformChange(transform)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                val canvasWidth = size.width
                val canvasHeight = size.height

                withTransform(
                    {
                        translate(
                            left = transform.x,
                            top = transform.y
                        )
                        scale(
                            scaleX = scale,
                            scaleY = scale,
                            pivot = Offset(
                                canvasWidth.div(2),
                                canvasHeight.div(2)
                            )
                        )
                    }
                ) {
                    drawImage(
                        image = image,
                        dstOffset = IntOffset(
                            x = canvasWidth.div(2)
                                .minus(image.width.div(2)).toInt(),
                            y = canvasHeight.div(2)
                                .minus(image.height.div(2)).toInt()
                        )
                    )
                }

                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    drawRect(Color(0x75000000))
                    drawRoundRect(
                        topLeft = rectDraw.topLeft,
                        size = rectDraw.size,
                        cornerRadius = CornerRadius(30f, 30f),
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                    restoreToCount(checkPoint)
                }
            }
        }
    }
}

@Composable
@Preview
fun CropUserPhotoContentPreview() {
    ComposeCropperTheme {
        val image = ImageBitmap.imageResource(id = R.drawable.cat)
        CropContent(image = image)
    }
}