package com.example.exif

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var text : TextView
    var checksum = 0
    private fun getPhotoFile(){
        val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startForResult.launch(intent)
    }
    fun getRealPathFromURI(contentUri: Uri?): String? {

        // can post image
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var uri = contentUri
        val cursor: Cursor = managedQuery(
            uri,
            proj,  // Which columns to return
            null,  // WHERE clause; which rows to return (all rows)
            null,  // WHERE clause selection arguments (none)
            MediaStore.Images.Media.DEFAULT_SORT_ORDER
        ) // Order-by clause (ascending by name)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }//https://stackoverflow.com/questions/18590514/loading-all-the-images-from-gallery-into-the-application-in-android
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data?.clipData
            val count = intent?.itemCount
            val sb = StringBuilder()
            for (i in 0..count!!-1){
                val path = getRealPathFromURI(intent.getItemAt(i)?.uri)
                System.out.println(path)
                val file = File(path!!)
                val exif = ExifInterface(file)
                val array = FloatArray(2)
                exif.getLatLong(array)
                //default value is 0/0

                val formatterDate = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                val formatterTime = SimpleDateFormat("hh:mm:ss", Locale.US)
                sb.append("Photo $i: lat: " + array[0] + " long: " + array[1]  + " date: " + formatterDate.format(exif.dateTimeOriginal) + " time: " + formatterTime.format(exif.dateTimeOriginal) + "\n")
            }
            text.setText(sb.toString())

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        text = findViewById(R.id.text)
        checkPermission(Manifest.permission.ACCESS_MEDIA_LOCATION, 0)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 1)
        if (checksum ==2) getPhotoFile()
        else{
            text.setText("Permissions are needed to use this app. Please reload this app again.")
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        System.out.println(permission)
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            checksum += 1
        }
    }
}