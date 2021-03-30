package q.rorbin.router.demo

import android.app.Application
import android.util.Log
import q.rorbin.router.Route
import q.rorbin.router.Router

/**
 * @author 邱长海
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Router.init(this, {
            when (it) {
                RouterConst.ROUTE_FIRST -> Route(FirstActivity::class.java)
                RouterConst.ROUTE_SECOND -> Route(SecondActivity::class.java)
                else -> null
            }
        }, interceptor = { route, _, next ->
            Log.i("routerdemo", "${route.path} in")
            next()
        })
    }
}