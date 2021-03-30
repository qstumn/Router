package q.rorbin.router

import android.content.Intent
import android.os.Bundle

/**
 * @author changhai.qiu
 */
/**
 * @param path routing path
 * @param target routing location, support activity and fragment
 * @param interceptor the interceptor for this path, if set this interceptor will be called and the
 *                    global interceptor will be ignored
 */
open class Route(
    val target: Class<*>? = null, private val interceptor: NavigatorInterceptor? = null,
    val transition: RouteTransition? = null
) {
    var path: String = ""
    fun getInterceptor(): NavigatorInterceptor? {
        return interceptor ?: Router.interceptor
    }
}

data class RouteTransition(val enterAnim: Int, val exitAnim: Int)

class RouteErrorException(message: String? = null) : Exception(message)

internal const val UNIQUE_DATA_KEY = "navigator_transmit_data_auto_key"

data class NavigatorResult<T>(val requestCode: Int, val intent: Intent) {

    fun uniqueData(): T {
        return intent.extras?.get(UNIQUE_DATA_KEY) as T
    }

    fun datas(): Bundle {
        return intent.extras ?: Bundle()
    }
}

data class NavigatorResultRegister<R>(
    internal var requestCode: Int,
    internal val resultCallback: ResultCallback<R>
)

interface NavParamsMixin {
    fun getBundle(): Bundle?

    fun <T> navParams(key: String = UNIQUE_DATA_KEY): Lazy<T> = lazy {
        val bundle = getBundle()
        bundle?.get(key) as T
    }

    fun <T> navParams(key: String = UNIQUE_DATA_KEY, default: T): Lazy<T> = lazy {
        val bundle = getBundle()
        (bundle?.get(key) as? T) ?: default
    }
}