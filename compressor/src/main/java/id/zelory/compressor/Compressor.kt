package id.zelory.compressor

import android.content.Context
import android.graphics.Bitmap

import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

import io.reactivex.Flowable

/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class Compressor(context: Context) {
    //max width and height values of the compressed image is taken as 612x816
    private var maxWidth = 612.0f
    private var maxHeight = 816.0f
    private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    private var quality = 80
    private var destinationDirectoryPath: String? = null

    init {
        destinationDirectoryPath = context.cacheDir.path + File.separator + "images"
    }

    fun setMaxWidth(maxWidth: Float): Compressor {
        this.maxWidth = maxWidth
        return this
    }

    fun setMaxHeight(maxHeight: Float): Compressor {
        this.maxHeight = maxHeight
        return this
    }

    fun setCompressFormat(compressFormat: Bitmap.CompressFormat): Compressor {
        this.compressFormat = compressFormat
        return this
    }

    fun setQuality(quality: Int): Compressor {
        this.quality = quality
        return this
    }

    fun setDestinationDirectoryPath(destinationDirectoryPath: String): Compressor {
        this.destinationDirectoryPath = destinationDirectoryPath
        return this
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun compressToFile(imageFile: File, compressedFileName: String = imageFile.name): File {
        return ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality,
                destinationDirectoryPath + File.separator + compressedFileName)
    }

    @Throws(IOException::class)
    fun compressToBitmap(imageFile: File): Bitmap? {
        return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight)
    }

    @JvmOverloads
    fun compressToFileAsFlowable(imageFile: File, compressedFileName: String = imageFile.name): Flowable<File> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToFile(imageFile, compressedFileName))
            } catch (e: IOException) {
                return@Callable Flowable.error(e)
            }
        })
    }

    fun compressToBitmapAsFlowable(imageFile: File): Flowable<Bitmap> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToBitmap(imageFile)!!)
            } catch (e: IOException) {
                return@Callable Flowable.error(e)
            }
        })
    }
}
