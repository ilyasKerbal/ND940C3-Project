package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var selectedGitHubRepository: REPOS? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
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
                                finishDownload = true;
                            }
                            DownloadManager.STATUS_PAUSED -> {
                                Log.i("MainActivity", "Download paused")
                            }
                            DownloadManager.STATUS_PENDING -> {
                                Log.i("MainActivity", "Download Pending")
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Log.i("MainActivity", "Download Successful")
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

}
