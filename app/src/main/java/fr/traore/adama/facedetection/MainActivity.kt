package fr.traore.adama.facedetection

import android.app.Activity
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.util.forEach
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.widget.Toast
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.R.attr.data
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mainJob: Job

    override val coroutineContext: CoroutineContext
        get() = mainJob + Dispatchers.Main

    var myImage: Bitmap? = null
    var redRectangle: Paint = Paint()
    var tempBitmap: Bitmap? = null
    var tempCanvas: Canvas? = null
    val options: BitmapFactory.Options = BitmapFactory.Options()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainJob = Job()

        //Because we will draw the red rectangle over any detected face so we need to make sure
        //That the bitmap is Mutable
        options.inMutable = true


        //Create the red rectangle
        redRectangle.strokeWidth = 5f
        redRectangle.color = Color.RED
        redRectangle.style = Paint.Style.STROKE



        imgPlaceHolder.setOnClickListener{
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG)
        }

        fabProcess.setOnClickListener{

            launch {

                //Work on background
                val deferredProcessing = async(Dispatchers.Default){
                    val faceDetector: FaceDetector = FaceDetector.Builder(applicationContext).setTrackingEnabled(false).build()

                    if(!faceDetector.isOperational && myImage == null) {
                        AlertDialog.Builder(this@MainActivity).setMessage("La détection n'a pas pu démarrer").show()
                        return@async
                    }

                    val frame: Frame = Frame.Builder().setBitmap(myImage).build()
                    val faces: SparseArray<Face> = faceDetector.detect(frame)

                    faces.forEach { _, value ->
                        val x1 = value.position.x
                        val y1 = value.position.y
                        val x2 = x1 + value.width
                        val y2 = y1 + value.height
                        tempCanvas?.drawRoundRect(RectF(x1, y1, x2, y2), 2f, 2f, redRectangle)
                    }

                    imgToBlur.setImageBitmap(tempBitmap)
                }

                deferredProcessing.await()

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOAD_IMG){
            if(data == null) return

            try {
                val imageUri = data.data
                val imageStream = if(imageUri != null) contentResolver.openInputStream(imageUri) else return
                myImage = BitmapFactory.decodeStream(imageStream, null, options)
                imgToBlur.setImageBitmap(myImage)

                //Canvas to draw on
                tempBitmap = if(myImage != null) Bitmap.createBitmap(myImage!!.width, myImage!!.height, Bitmap.Config.RGB_565) else return
                tempCanvas = Canvas(tempBitmap!!)
                tempCanvas!!.drawBitmap(myImage!!, 0f, 0f, null)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainJob.cancel()
    }

    companion object {
        const val REQUEST_LOAD_IMG = 141
    }

}
