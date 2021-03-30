package q.rorbin.router.demo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import q.rorbin.router.findNavigator
import q.rorbin.router.navParams

/**
 * @author changhai.qiu
 */
class SecondActivity : AppCompatActivity() {
    private val params by navParams("key", default = "free!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val button = Button(this)
        button.text = "return to FirstActivity"
        button.setOnClickListener {
            findNavigator().pop(params)
        }
        setContentView(button)
    }
}