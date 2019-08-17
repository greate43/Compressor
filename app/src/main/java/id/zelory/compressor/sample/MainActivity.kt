package id.zelory.compressor.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.Random

import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var actualImageView: ImageView? = null
    private var compressedImageView: ImageView? = null
    private var actualSizeTextView: TextView? = null
    private var compressedSizeTextView: TextView? = null
    private var actualImage: File? = null
    private var compressedImage: File? = null

    private val randomColor: Int
        get() {
            val rand = Random()
            return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actualImageView = findViewById(R.id.actual_image)
        compressedImageView = findViewById(R.id.compressed_image)
        actualSizeTextView = findViewById(R.id.actual_size)
        compressedSizeTextView = findViewById(R.id.compressed_size)

        actualImageView!!.setBackgroundColor(randomColor)
        clearImage()
    }

    fun chooseImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    fun compressImage(view: View) {
        if (actualImage == null) {
            showError("Please choose an image!")
        } else {

            // Compress image in main thread
            //compressedImage = new Compressor(this).compressToFile(actualImage);
            //setCompressedImage();

            // Compress image to bitmap in main thread
            //compressedImageView.setImageBitmap(new Compressor(this).compressToBitmap(actualImage));

            // Compress image using RxJava in background thread
            Compressor(this)
                    .compressToFileAsFlowable(actualImage!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ file ->
                        compressedImage = file
                        setCompressedImage()
                    }, { throwable ->
                        throwable.printStackTrace()
                        showError(throwable.message)
                    })
        }
    }

    fun customCompressImage(view: View) {
        if (actualImage == null) {
            showError("Please choose an image!")
        } else {
            // Compress image in main thread using custom Compressor
            try {
                compressedImage = Compressor(this)
                        .setMaxWidth(640f)
                        .setMaxHeight(480f)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).absolutePath)
                        .compressToFile(actualImage!!)

                setCompressedImage()
            } catch (e: IOException) {
                e.printStackTrace()
                showError(e.message)
            }

            // Compress image using RxJava in background thread with custom Compressor
            /*new Compressor(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFileAsFlowable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            compressedImage = file;
                            setCompressedImage();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            showError(throwable.getMessage());
                        }
                    });*/
        }
    }

    private fun setCompressedImage() {
        compressedImageView!!.setImageBitmap(BitmapFactory.decodeFile(compressedImage!!.absolutePath))
        compressedSizeTextView!!.text = String.format("Size : %s", getReadableFileSize(compressedImage!!.length()))

        Toast.makeText(this, "Compressed image save in " + compressedImage!!.path, Toast.LENGTH_LONG).show()
        Log.d("Compressor", "Compressed image save in " + compressedImage!!.path)
    }

    private fun clearImage() {
        actualImageView!!.setBackgroundColor(randomColor)
        compressedImageView!!.setImageDrawable(null)
        compressedImageView!!.setBackgroundColor(randomColor)
        compressedSizeTextView!!.text = "Size : -"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError("Failed to open picture!")
                return
            }
            try {
                actualImage = data.data?.let { FileUtil.from(this, it) }
                actualImageView!!.setImageBitmap(BitmapFactory.decodeFile(actualImage!!.absolutePath))
                actualSizeTextView!!.text = String.format("Size : %s", getReadableFileSize(actualImage!!.length()))
                clearImage()
            } catch (e: IOException) {
                showError("Failed to read picture data!")
                e.printStackTrace()
            }

        }
    }

    fun showError(errorMessage: String?) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    companion object {
        private val PICK_IMAGE_REQUEST = 1
    }
}
