package fr.traore.adama.facedetection

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

class MainActivity : AppCompatActivity() {
    var myImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Because we will draw the red rectangle over any detected face so we need to make sure
        //That the bitmap is Mutable
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inMutable = true

        val myImage: Bitmap = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.test1,
            options
        )

        imgToBlur.setImageBitmap(myImage)


        //Create the red rectangle
        val redRectangle: Paint = Paint()
        redRectangle.strokeWidth = 5f
        redRectangle.color = Color.RED
        redRectangle.style = Paint.Style.STROKE

        //Canvas to draw on
        val tempBitmap = Bitmap.createBitmap(myImage.width, myImage.height, Bitmap.Config.RGB_565)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(myImage, 0f, 0f, null)


        fabProcess.setOnClickListener{
            val faceDetector: FaceDetector = FaceDetector.Builder(applicationContext).setTrackingEnabled(false).build()
            if(!faceDetector.isOperational && myImage == null) {
                AlertDialog.Builder(this@MainActivity).setMessage("La détection n'a pas pu démarrer").show()
                return@setOnClickListener
            }

            val frame: Frame = Frame.Builder().setBitmap(myImage).build()
            val faces: SparseArray<Face> = faceDetector.detect(frame)

            faces.forEach { key, value ->
                val x1 = value.position.x
                val y1 = value.position.y
                val x2 = x1 + value.width
                val y2 = y1 + value.height
                tempCanvas.drawRoundRect(RectF(x1, y1, x2, y2), 2f, 2f, redRectangle)
            }

            imgToBlur.setImageBitmap(tempBitmap)
        }

    }

}
