package com.cross.privateperiodtracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrivatePeriodTrackerTheme {
                Login(loginFn = { pw: String ->
                    val encryptor = Encryptor(pw)

                    val dm = DataManager(applicationContext, encryptor)
                    if (dm.loadData() == null) {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.wrong_password),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Login
                    }

                    val k = Intent(this, HomeActivity::class.java)
                    k.putExtra(dataKey, dm)
                    startActivity(k)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    loginFn: (pw: String) -> Unit
) {
    val context = LocalContext.current
    val priv_policy_url = stringResource(id = R.string.priv_policy_url)
    val debug_menu_message = stringResource(R.string.opening_debug_menu_in__clicks)
    var pw by remember { mutableStateOf("") }
    var lastDebugClick by remember { mutableStateOf(0L) }
    var debugClickCount by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(
                style = Typography.bodySmall,
                text = stringResource(R.string.enter_password),
                modifier = Modifier.padding(8.dp)
            )
            TextField(
                value = pw,
                onValueChange = { s ->
                    pw = s
                },
                Modifier
                    .fillMaxWidth(1f)
                    .padding(8.dp)
                    .testTag("password")
            )
        }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        val uri = Uri.parse(priv_policy_url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        ContextCompat.startActivity(context, intent, null)
                    }
                ) {
                    Text(
                        stringResource(id = R.string.privacy_policy)
                    )
                }

                IconButton(
                    onClick = {
                        val curTime = System.currentTimeMillis()
                        if (lastDebugClick > curTime - 500) {
                            debugClickCount += 1
                        } else {
                            debugClickCount = 1
                        }
                        lastDebugClick = curTime

                        if (debugClickCount > 5) {
                            val k = Intent(context, DebugActivity::class.java)
                            startActivity(context, k, null)
                            return@IconButton
                        }

                        if (debugClickCount > 2) {
                            Toast.makeText(
                                context,
                                String.format(debug_menu_message, 5 - debugClickCount),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                }

                Button(
                    onClick = { loginFn(pw) },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(stringResource(id = R.string.login))
                    Icon(Icons.Filled.Login, "Login")
                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    PrivatePeriodTrackerTheme {
        Login { _: String -> }
    }
}
