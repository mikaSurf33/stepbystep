package com.mtt.stepbystep

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator
import com.sap.cloud.mobile.foundation.common.SettingsParameters
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.foundation.user.UserInfo
import com.sap.cloud.mobile.foundation.user.UserRoles
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {
    var myTag: String = "myDebuggingTag"
    lateinit var myOkHttpClient: OkHttpClient
    lateinit var deviceID: String
    val serviceURL: String =
        "https://mobileservicecf-mobileservicespace-com-mtt-stepbystep.cfapps.eu10.hana.ondemand.com"
    val appID: String = "com.mtt.stepbystep"
    val connectionID: String = "com.sap.edm.sampleservice.v2"
    lateinit var messageToToast: String
    lateinit var toast: Toast
    var numberOfPresses: Int = 0

    inner class C : UserRoles.CallbackListener {
        override fun onSuccess(ui: UserInfo) {
            Log.d(myTag, "User Name: " + ui.getUserName())
            Log.d(myTag, "User Id: " + ui.getId())
            var roleList: Array<out String>? = ui.roles
            Log.d(myTag, "User has the following Roles")
            for (i in 0 until roleList!!.size) {
                Log.d(myTag, "Role Name " + roleList!![i])
            }
            var currentUser: String = ui.getId()
            toastMessage("Currently logged with " + ui.getId())
        }

        override fun onError(throwable: Throwable) {
            toastMessage("UserRoles onFailure " + throwable.message)
        }
    }

    inner class D : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.d(myTag, "onFailure called during authentication " + e.message)
            toastMessage("Registration failed " + e.message)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful()) {
                Log.d(myTag, "Successfully authenticated")
                toastMessage("Successfully authenticated")
                enableButtonsOnRegister(true)
                getUser()
            } else { //called if the credentials are incorrect
                Log.d(myTag, "Registration failed " + response.networkResponse());
                toastMessage("Registration failed " + response.networkResponse());
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(myTag, "onCreate")
        setContentView(R.layout.activity_main)
        deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    fun onLogALine(view: View) {
        Log.d(myTag, "In onLogALine")
        numberOfPresses = numberOfPresses + 1
        Log.d(myTag, "Button pressed " + numberOfPresses + " times")
    }

    fun onRegister(view: View) {
        Log.d(myTag, "In onRegister")

        myOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AppHeadersInterceptor(appID, deviceID, "1.0"))
            .authenticator(BasicAuthDialogAuthenticator())
            .cookieJar(WebkitCookieJar())
            .build()

        var request: Request =
            Request.Builder().get().url(serviceURL + "/" + connectionID + "/").build()
        var updateUICallback : Callback = D()

        myOkHttpClient.newCall(request).enqueue(updateUICallback)
    }

    fun onUploadLog(view: View) {
        Log.d(myTag, "In onUploadLog")
    }

    fun onOnlineOData(view: View) {
        Log.d(myTag, "In onOnlineOData")
    }

    fun onOfflineOData(view: View) {
        Log.d(myTag, "In onOfflineOData")
    }

    fun toastMessage(msg: String) {
        var msgTmp: String = msg
        if (toast != null && toast.getView().isShown()) {
            msgTmp = messageToToast + "\n" + msg
        } else {
            messageToToast = ""
        }
        messageToToast = msgTmp
        var handler: Handler = Handler(Looper.getMainLooper())
        var runnable: Runnable = Runnable() {
            fun run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(applicationContext, messageToToast, Toast.LENGTH_LONG)
            }
        }
        handler.post(runnable)
    }

    fun getUser() {
        Log.d(myTag, "In getUser")

        var sp: SettingsParameters = try {
            SettingsParameters(serviceURL, appID, deviceID, "1.0")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } as SettingsParameters

        var roles: UserRoles = UserRoles(myOkHttpClient, sp)
        var callbackListener: UserRoles.CallbackListener = C()


        roles.load(callbackListener)
    }

    fun enableButtonsOnRegister(enable: Boolean) {
        var uploadLogButton: Button = b_uploadLog
        var onlineODataButton: Button = b_odata
        var handler: Handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            fun run() {
                uploadLogButton.setEnabled(enable)
                onlineODataButton.setEnabled(enable)
            }
        })
    }
}
