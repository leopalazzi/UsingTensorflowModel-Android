package com.example.foodscan

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.common.util.IOUtils.toByteArray

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color

import kotlinx.android.synthetic.main.activity_main.*

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import java.io.IOException


import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.ThreadLocalRandom


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // val bm = BitmapFactory.decodeResource(resources, R.drawable.image)
        setContentView(R.layout.activity_main)
      /*  val bm = BitmapFactory.decodeResource(resources, R.drawable.image)
        Log.d("tag", bm.toString())
        val inputStream = assets.open("cristalline.jpg")
*///BUTTON CLICK
        img_pick_btn.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        //val bitmap = assetsToBitmap("cristalline.jpg")


        var conditionsBuilder: FirebaseModelDownloadConditions.Builder =
            FirebaseModelDownloadConditions.Builder().requireWifi()
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                .requireCharging()
                .requireDeviceIdle()
        }
        val conditions = conditionsBuilder.build()

// Build a remote model object by specifying the name you assigned the model
// when you uploaded it in the Firebase console.
        val remoteModel = FirebaseRemoteModel.Builder("model")
            .enableModelUpdates(true)
            .setInitialDownloadConditions(conditions)
            .setUpdatesDownloadConditions(conditions)
            .build()
        val firebaseModel = FirebaseModelManager.getInstance().registerRemoteModel(remoteModel)
        Log.d("Firebase"," firebase Model : " + firebaseModel.toString())
      /*  FirebaseModelManager.getInstance().downloadRemoteModelIfNeeded(remoteModel)
            .addOnSuccessListener {
               // ((TextView)view.findViewById(R.id.text1)).setText("Reussi");

                // Model downloaded successfully. Okay to use the model.
                print("super")
                Log.d("TAG", "You win !")

            }
            .addOnFailureListener {
                // Model couldn’t be downloaded or other internal error.
                Log.d("TAG", "You fail !")

                // ...
            }*/
        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1,64, 64, 1))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 3))
            .build()
        val input = Array(1) { Array(64) { Array(64) { FloatArray(1) } } }
        //val bitmap = Bitmap.createScaledBitmap(bm, 224, 224, true)

        for (x in 0..63) {
            for (y in 0..63) {
               // val pixel = bitmap.getPixel(x, y)
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[0][x][y][0] =  ThreadLocalRandom.current().nextDouble(0.0, 250.0).toFloat()

            }
        }
        val inputs = FirebaseModelInputs.Builder()
            .add(input) // add() as many input arrays as your model requires
            .build()

        Log.d("NO PB", "No problem here !")

        val localSource = FirebaseLocalModel.Builder("my_local_model") // Assign a name to this model
            .setAssetFilePath("model.tflite")
            .build()

        Log.d("NO PB", "No problem here !")

        FirebaseModelManager.getInstance().registerRemoteModel(remoteModel)
        FirebaseModelManager.getInstance().registerLocalModel(localSource)

        val options = FirebaseModelOptions.Builder()
            .setRemoteModelName("model")
            .setLocalModelName("my_local_model")
            .build()

        Log.d("NO PB", "No problem here !")

        val interpreter = FirebaseModelInterpreter.getInstance(options)

        Log.d("NO PB", "No problem here !")

        if(interpreter != null)
        {
            interpreter.
            run(inputs, inputOutputOptions)
            .addOnSuccessListener { result ->
                val output = result.getOutput<Array<FloatArray>>(0)
                val probabilities = output[0]
                Log.d("SUCCESS ", "You are a boss ! \n")
                Log.i("MLKit", String.format(" %1.4f", probabilities[2]))

                // ...
            }
            .addOnFailureListener(
                object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        // Task failed with an exception
                        Log.d("Failure intereprete", "FAILLLL : " + e)
                        // ...
                    }
                })
        }
        else{
            Log.d("Interprete null", "NULLL")

        }





        Log.d("DONE","Done !")
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            Log.d("Nom Image", data?.data.toString())
            val imageUri = data?.data

            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            bitmap= Bitmap.createScaledBitmap(bitmap, 64, 64, true)

// pixel information
            var A: Int
            var R: Int
            var G: Int
            var B: Int
            var pixel: Int
            // constant factors
            val GS_RED = 0.299;
            val GS_GREEN = 0.587;
            val GS_BLUE = 0.114;
            // get image size
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            // scan through every single pixel
            val bmOut = Bitmap.createBitmap(64,64, bitmap.getConfig())
            val input = Array(1) { Array(64) { Array(64) { FloatArray(1) } } }

            //image_view.setImageURI(data?.data)

            for (x in 0..63) {
                for (y in 0..63) {
                    // get one pixel color
                    pixel = bitmap.getPixel(x, y)
                    // retrieve color of all channels
                    A = Color.alpha(pixel)
                    R = Color.red(pixel)
                    G = Color.green(pixel)
                    B = Color.blue(pixel)


                    // take conversion up to one single value
                    B = (GS_RED * R + GS_GREEN * G + GS_BLUE * B).toInt()
                    G = B
                    R = G
                    input[0][x][y][0] = ((R+G+B)/3.toFloat());
                   // Log.d("COLOR",input[0][x][y][0].toString() )

                    // set new pixel color to output bitmap
                    bmOut.setPixel(x, y, Color.argb(A, R, G, B))
                }
            }
            val bmpGrayscale = Bitmap.createBitmap(64   , 64, Bitmap.Config.RGB_565)
            image_view.setImageBitmap(bmOut)


            var conditionsBuilder: FirebaseModelDownloadConditions.Builder =
                FirebaseModelDownloadConditions.Builder().requireWifi()
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Enable advanced conditions on Android Nougat and newer.
                conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle()
            }
            val conditions = conditionsBuilder.build()

// Build a remote model object by specifying the name you assigned the model
// when you uploaded it in the Firebase console.
            val remoteModel = FirebaseRemoteModel.Builder("model")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build()
            val firebaseModel = FirebaseModelManager.getInstance().registerRemoteModel(remoteModel)
            Log.d("Firebase"," firebase Model : " + firebaseModel.toString())
            /*  FirebaseModelManager.getInstance().downloadRemoteModelIfNeeded(remoteModel)
                  .addOnSuccessListener {
                     // ((TextView)view.findViewById(R.id.text1)).setText("Reussi");

                      // Model downloaded successfully. Okay to use the model.
                      print("super")
                      Log.d("TAG", "You win !")

                  }
                  .addOnFailureListener {
                      // Model couldn’t be downloaded or other internal error.
                      Log.d("TAG", "You fail !")

                      // ...
                  }*/
            val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1,64, 64, 1))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 3))
                .build()

            val inputs = FirebaseModelInputs.Builder()
                .add(input) // add() as many input arrays as your model requires
                .build()

            Log.d("NO PB", "No problem here !")

            val localSource = FirebaseLocalModel.Builder("my_local_model") // Assign a name to this model
                .setAssetFilePath("model.tflite")
                .build()

            Log.d("NO PB", "No problem here !")

            FirebaseModelManager.getInstance().registerRemoteModel(remoteModel)
            FirebaseModelManager.getInstance().registerLocalModel(localSource)

            val options = FirebaseModelOptions.Builder()
                .setRemoteModelName("model")
                .setLocalModelName("my_local_model")
                .build()

            Log.d("NO PB", "No problem here !")

            val interpreter = FirebaseModelInterpreter.getInstance(options)

            Log.d("NO PB", "No problem here !")

            if(interpreter != null)
            {
                interpreter.
                    run(inputs, inputOutputOptions)
                    .addOnSuccessListener { result ->
                        val output = result.getOutput<Array<FloatArray>>(0)
                        val probabilities = output[0]
                        Log.d("SUCCESS ", "You are a boss ! \n")
                        Log.i("MLKit", String.format("Cristalline %1.4f", probabilities[0]))
                        Log.i("MLKit", String.format("Pates %1.4f", probabilities[1]))
                        Log.i("MLKit", String.format("Evian %1.4f", probabilities[2]))
                        // ...
                    }
                    .addOnFailureListener(
                        object : OnFailureListener {
                            override fun onFailure(e: Exception) {
                                // Task failed with an exception
                                Log.d("Failure intereprete", "FAILLLL : " + e)
                                // ...
                            }
                        })
            }
            else{
                Log.d("Interprete null", "NULLL")

            }

            //image_view.setImageDrawable(bitmapToDrawable(bitmap))
        }
    }

    // Method to convert a bitmap to bitmap drawable
    private fun bitmapToDrawable(bitmap:Bitmap):BitmapDrawable{
        return BitmapDrawable(resources,bitmap)
    }

    fun decodeUriToBitmap(mContext: Context, sendUri: Uri): Bitmap? {
        var getBitmap: Bitmap? = null
        try {
            val image_stream: InputStream
            try {
                image_stream = mContext.getContentResolver().openInputStream(sendUri)!!
                getBitmap = BitmapFactory.decodeStream(image_stream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return getBitmap
    }
}