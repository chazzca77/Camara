package com.geekpark.camara

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*Clase 9  Uso de Cámara
*
* Hay 2 formas de usar la cámara, por Intent y crearlo desde cero
* El intent es el mas sencillo, crearlo desde cero es para propositos especificos
* - Conoceremos como usar la cámara de nuestro dispositivo a través de un Intent
* - Mostrar la foto en un imageView
* - Guardar nuestra foto en almacenamiento Interno y Externo
* - Conoceremos el uso del FileProvider para implementar seguridad
* - Seleccionaremos imégenes de la galeria
* */
class MainActivity : AppCompatActivity() {

    //Creamos variables para permisos
    val SOLICITA_TOMAR_FOTO = 1
    val SOLICITA_SELECCIONAR_FOTO = 2
    val permisoCamara = android.Manifest.permission.CAMERA
    val permisoWriteStorage = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permisoReadStorage = android.Manifest.permission.READ_EXTERNAL_STORAGE

    //variable global para guardar el path de la foto guardada y poder traerla mas tarde
    var urlFotoActual = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Configuramos nuestro layout y damos eventos a los botones
        btnTomar.setOnClickListener {
            tomarFoto()
        }
        btnSeleccionar.setOnClickListener {
            seleccionarFoto()
        }
    }

    //Creamos funciones para separar las diferentes acciones que haremos
    @RequiresApi(Build.VERSION_CODES.M)
    fun tomarFoto(){
        pedirPermisos()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun seleccionarFoto(){
        pedirPermisosSeleccionarFoto()
    }

    //Comenzamos con la peticion de permisos
    @RequiresApi(Build.VERSION_CODES.M)
    fun pedirPermisos(){
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this,permisoCamara)

        if(deboProveerContexto){
            solicitudPermisos()
        }else{
            solicitudPermisos()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun pedirPermisosSeleccionarFoto(){
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this,permisoReadStorage)

        if(deboProveerContexto){
            solicitudPermisosSeleccionarFoto()
        }else{
            solicitudPermisosSeleccionarFoto()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun solicitudPermisos(){
        requestPermissions(arrayOf(permisoCamara,permisoReadStorage,permisoWriteStorage),SOLICITA_TOMAR_FOTO)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun solicitudPermisosSeleccionarFoto(){
        requestPermissions(arrayOf(permisoCamara,permisoReadStorage,permisoWriteStorage),SOLICITA_SELECCIONAR_FOTO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //determinar los permisos garantizados dependiendo la petición
        //Recuerden que grantResults es un arreglo de permisos dados
        when(requestCode){
            SOLICITA_TOMAR_FOTO ->{
                if(grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    //Enviamos nuestro intent de la cámara
                    dispararIntentTomarFoto()
                }else{
                    Toast.makeText(this,"No diste todos los permisos de la cámara",Toast.LENGTH_SHORT).show()
                }

            }
            SOLICITA_SELECCIONAR_FOTO->{
                if(grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Intent para seleccionar foto
                    dispararIntentSeleccionarFoto()
                }else{
                    Toast.makeText(this,"No diste todos los permisos para seleccionar fotos de galeria",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun dispararIntentSeleccionarFoto(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent,"Seleccionar una Foto"),SOLICITA_SELECCIONAR_FOTO)
    }


    fun dispararIntentTomarFoto(){
       val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(intent.resolveActivity(packageManager) != null){

            //Creamos nuestra imagen y la almacenamos en una variable
            var archivoFoto:File? = null
            archivoFoto = crearArchivoImagen()

            if(archivoFoto != null){
                //Creamos nuestro FileProvider para cambiar el path de la uri
                //el authority debe ser el nombre de nuestro paquete y debe ser el misqie que ponemos al inicializar nuestro provider en el AndroidManifest.xml
                var urlFoto = FileProvider.getUriForFile(this,"com.geekpark.camara.android.fileProvider",archivoFoto)

                //Añadimos la url a nuestro intent
                intent.putExtra(MediaStore.EXTRA_OUTPUT,urlFoto)
                startActivityForResult(intent,SOLICITA_TOMAR_FOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Recuperamos la foto guardada y la ponemos dentro de nuestro imageView
        when(requestCode){
            SOLICITA_TOMAR_FOTO->{
                if(resultCode == Activity.RESULT_OK){
                   /* val extras = data?.extras
                    val imageBitmap = extras!!.get("data") as Bitmap*/
                    val uri = Uri.parse(urlFotoActual)
                    val stream = contentResolver.openInputStream(uri)
                    val imageBitmap = BitmapFactory.decodeStream(stream)
                    imgFoto!!.setImageBitmap(imageBitmap)

                    anadirImagenGaleria()


                }else{
                    //Canceló la captura de foto
                    Toast.makeText(this,"Canceló la captura de foto",Toast.LENGTH_SHORT).show()
                }
            }
            SOLICITA_SELECCIONAR_FOTO->{
                if(resultCode == Activity.RESULT_OK){
                    val uri = Uri.parse(data?.data.toString())
                    val stream = contentResolver.openInputStream(uri)
                    val imageBitmap = BitmapFactory.decodeStream(stream)
                    imgFoto!!.setImageBitmap(imageBitmap)
                }else{
                    //Canceló la captura de foto
                    Toast.makeText(this,"Canceló la seleecion de foto",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    fun crearArchivoImagen():File{
        val timeStamp = SimpleDateFormat("yyyMMdd_HHmmss").format(Date())
        val nombreArchivoImagen = "JPEG_"+timeStamp+"_"

        //Esta parte es para almacenamiento interno
        //val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //val imagen = File.createTempFile(nombreArchivoImagen,".jpg",directorio)


        //Esta parte es para almacenamiento Externo (carpetas públicas)

        val directorio = Environment.getExternalStorageDirectory()
        val directorioPictures = File(directorio.absolutePath+"/Pictures")
        val imagen = File.createTempFile(nombreArchivoImagen,".jpg",directorioPictures)

        urlFotoActual = "file://"+imagen.absolutePath

        return imagen
    }

    fun anadirImagenGaleria(){
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(urlFotoActual)
        val uri = Uri.fromFile(file)
        intent.setData(uri)
        this.sendBroadcast(intent)
    }


}
