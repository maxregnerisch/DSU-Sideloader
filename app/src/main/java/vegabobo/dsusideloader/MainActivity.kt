package vegabobo.dsusideloader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    companion object {
        private const val DOWNLOAD_PATH = "mytap/downloads/"
        private const val REQUEST_CODE = 1
    }

    private lateinit var downloadUrl: String
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_select_file.setOnClickListener {
            checkStoragePermission()
        }

        button_download.setOnClickListener {
            download()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            } else {
                selectFile()
            }
        } else {
            selectFile()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val fileNameArray: List<String> = uri.pathSegments
                fileName = fileNameArray[fileNameArray.size - 1]

                textView_file_name.text = fileName
                downloadUrl = uri.toString()
            }
        }
    }

    private fun download() {
        val path = File(getExternalFilesDir(null), DOWNLOAD_PATH)
        if (!path.exists()) {
            path.mkdirs()
        }

        val progress = createProgressDialog()

        doAsync {
            val file = File(path, fileName)
            var input: InputStream? = null
            var output: FileOutputStream? = null
            try {
                val url = URL(downloadUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@doAsync
                }

                val fileLength: Int = connection.contentLength

                input = connection.inputStream
                output = FileOutputStream(file)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    if (fileLength > 0) {
                        progress.progress = (total * 100 / fileLength).toInt()
                    }
                    output.write(data, 0, count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            uiThread {
                progress.dismiss()
                Toast.makeText(
                    this@MainActivity,
                    "Download complete",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createProgressDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setTitle("Downloading")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            show()
        }
    }
}
