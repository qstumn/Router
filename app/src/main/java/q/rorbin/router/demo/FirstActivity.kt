package q.rorbin.router.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import q.rorbin.router.findNavigator
import q.rorbin.router.registerForNavigatorResult

/**
 * @author 邱长海
 */
class FirstActivity : AppCompatActivity() {
    private val resultRegister = registerForNavigatorResult<String> {
        Log.i(
            "routerdemo",
            "result from ${RouterConst.ROUTE_SECOND}, value is : ${it.uniqueData()}"
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val button = Button(this)
        button.text = "navigate to SecondActivity"
        button.setOnClickListener {
//            findNavigator().path(RouterConst.ROUTE_SECOND).push(resultRegister)
            findNavigator().path(RouterConst.ROUTE_SECOND).push<String> {
                Log.i(
                    "routerdemo",
                    "result from ${RouterConst.ROUTE_SECOND}, value is : ${it.uniqueData()}"
                )
            }
        }
        setContentView(button)
    }
}