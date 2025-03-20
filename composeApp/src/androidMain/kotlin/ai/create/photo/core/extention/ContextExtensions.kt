package ai.create.photo.core.extention

import android.content.Context
import android.widget.Toast

fun Context.toast(text: String?, length: Int = Toast.LENGTH_LONG) {
    if (text == null) return
    Toast.makeText(applicationContext, text, length).show()
}