package com.idn.myalarmapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.ContentObservable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        //TODO Change value of const val TYPE_ONE_TIME & TYPE_REPEATING to Int
        const val TYPE_ONE_TIME = 0
        const val TYPE_REPEATING = 1

        const val EXTRA_MASSAGE = "message"
        const val EXTRA_TYPE = "type"

        //siapkan (2)dua ID untuk 2 macam alarm, one time dan repeating
        private const val ID_ONETIME = 100
        private const val ID_REPEATING = 101

        private const val DATE_FORMAT = "dd-MM-yyyy"
        private const val TIME_FORMAT = "HH:mm"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra(EXTRA_TYPE,0)
        val massage = intent.getStringExtra(EXTRA_MASSAGE)

        val title = if (type == TYPE_ONE_TIME) "One Time Alarm" else "Repeating Alarm"
        val notifId = if (type == TYPE_ONE_TIME) ID_ONETIME else ID_REPEATING

        if (massage != null) {
            showAlarmNotification(context, title, massage, notifId)
        }

    }

    private fun showAlarmNotification(context: Context, title: String, massage: String, notifId: Int) {

        val channelId = "Channel_1"
        val channelName = "AlarmManger channel1"
        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_one_time)
            .setContentTitle(title)
            .setContentText(massage)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000,))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel (
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(channelId)
            notificationManagerCompat.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notificationManagerCompat.notify(notifId, notification)

    }

    fun setOneTimeAlarm(context: Context, type: Int, date: String, time: String, massage: String){
        if(isDateInvalid(date, DATE_FORMAT) || isDateInvalid(time, TIME_FORMAT)) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MASSAGE, massage)
        intent.putExtra(EXTRA_TYPE, type)

        Log.e("One TIme", "$date $time")
        val dataArray = date.split("-").toTypedArray()
        val timeArray = time.split(":").toTypedArray()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, Integer.parseInt(dataArray[2]))
        calendar.set(Calendar.MONTH, Integer.parseInt(dataArray[1])-1)
        calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(dataArray[0]))
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_ONETIME, intent, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(context, "Succes Set Up One Time Alarm", Toast.LENGTH_SHORT).show()
    }

    fun setRepeatingAlarm(context: Context, type: Int, time: String, massage: String){
        if(isDateInvalid(time, TIME_FORMAT)) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MASSAGE, massage)

        val putExtra = intent.putExtra(EXTRA_TYPE, type)
        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_REPEATING, intent, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(context, "Succes Set Up Repeating Alarm", Toast.LENGTH_SHORT).show()
    }

    private fun isDateInvalid(date: String, format: String): Boolean {
        return try {
            val df = SimpleDateFormat(format, Locale.getDefault())
            df.isLenient = false
            df.parse(date)
            false
        }catch (e: ParseException) {
            true
        }

    }

    fun canceAlarm(context: Context, type: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        val requestCode = when (type) {
            TYPE_ONE_TIME -> ID_ONETIME
            TYPE_REPEATING -> ID_REPEATING
            else -> Log.i("cancel Alarm", "cancelAlarm: Unknow type of Alarm")
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
        if (type == TYPE_ONE_TIME){
            Toast.makeText(context, "Cancel One Time Alarm", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Cancel Repeating Alarm", Toast.LENGTH_SHORT).show()
        }
    }
}