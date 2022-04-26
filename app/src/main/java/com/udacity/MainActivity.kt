package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class Status{
    FAILED,
    SUCCESS
}


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var selectedGitHubRepository: REPOS? = null

    private var fileName = "Unknown file name"


    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager

        createChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name))

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent?.action
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) && context != null && id != null){
                val query = DownloadManager.Query()
                query.setFilterById(id, 0)
                val manager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor: Cursor = manager.query(query)
                if (cursor.moveToFirst()){
                    if (cursor.count > 0){
                        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            notificationManager.sendNotification("Your download status is ready", fileName, context, Status.SUCCESS)
                        }else{
                            onFailError()
                        }
                    }
                }
            }
        }
    }

    private fun download() {
        selectedGitHubRepository?.let {
            val request =
                DownloadManager.Request(Uri.parse(selectedGitHubRepository!!.url))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/zipfiles/")

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.

            CoroutineScope(Dispatchers.Default).launch {
                var finishDownload = false
                var progress = 0
                while (!finishDownload){
                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                    if (cursor.moveToFirst()){
                        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                        when(status){
                            DownloadManager.STATUS_FAILED -> {
                                Log.i("MainActivity", "Download Field")
                                fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                                finishDownload = true;
                                onFailError()
                            }
                            DownloadManager.STATUS_PAUSED -> {
                                Log.i("MainActivity", "Download paused")
                                fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                                finishDownload = true;
                                onFailError()
                                downloadManager.remove(downloadID)
                            }
                            DownloadManager.STATUS_PENDING -> {
                                Log.i("MainActivity", "Download Pending")
                                fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                                finishDownload = true;
//                                onFailError()
//                                downloadManager.remove(downloadID)
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Log.i("MainActivity", "Download Successful")
                                fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                                finishDownload = true;
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                if (total >= 0) {
                                    val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                    progress = ((downloaded * 100L) / total).toInt();
                                    Log.i("MainActivity", "Download progress: $progress")
                                }
                            }
                        }
                    }
                }
            }

        } ?: Toast.makeText(this, getString(R.string.no_choice_toast), Toast.LENGTH_SHORT).show()

    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }

    private enum class REPOS(val url: String) {
        ND940C3("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
        GLIDE("https://github.com/bumptech/glide/archive/master.zip"),
        RETROFIT("https://github.com/square/retrofit/archive/master.zip")
    }

    fun onItemSelected(view: View) {
        val radioButton = view as RadioButton
        selectedGitHubRepository = when(radioButton.id){
            R.id.project_radio -> REPOS.ND940C3
            R.id.glide_radio -> REPOS.GLIDE
            R.id.retrofit_radio -> REPOS.RETROFIT
            else -> null
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = "Download Status"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun onFailError() {
        notificationManager.sendNotification("Something went wrong", fileName, this, Status.FAILED)
    }

}
