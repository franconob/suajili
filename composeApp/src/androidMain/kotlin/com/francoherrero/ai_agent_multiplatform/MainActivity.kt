package com.francoherrero.ai_agent_multiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.francoherrero.ai_agent_multiplatform.di.initKoin
import com.parkwoocheol.kmpdatastore.platform.KmpDataStoreContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        KmpDataStoreContext.init(this)

        setContent {
            initKoin()
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
