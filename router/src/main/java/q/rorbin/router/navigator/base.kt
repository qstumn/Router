package q.rorbin.router.navigator

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import q.rorbin.router.NavigatorResultRegister
import q.rorbin.router.ResultCallback
import q.rorbin.router.putToBundle

/**
 * @author changhai.qiu
 */
abstract class Navigator internal constructor() {
    protected val requestIntentBundle: Bundle = Bundle()

    companion object;

    open fun <N : Navigator> cast(): N {
        return this as N
    }

    abstract fun <T> newTargetInstance(): T

    abstract fun path(path: String): Navigator

    open fun params(data: Any?): Navigator {
        data?.also { it.putToBundle(bundle = requestIntentBundle) }
        return this
    }

    open fun param(key: String, data: Any?): Navigator {
        data?.also { it.putToBundle(key, bundle = requestIntentBundle) }
        return this
    }

    fun push() {
        this.push<Any>(null)
    }

    fun pop() {
        this.pop(null)
    }

    fun pushReplacement() {
        this.push<Any>(null)
        this.pop(null)
    }

    /**
     * navigate and listen its return value, but [onResult] will not work after the activity is
     * killed in the background and rebuilt by returned
     * so it is only suitable for next page will return quickly in the short time
     */
    abstract fun <T> push(onResult: ResultCallback<T>?)

    /**
     * navigate and listen its return value, effective in all scenarios
     * but[resultRegister]needs to call[registerForNavigatorResult]to pre register
     */
    abstract fun push(resultRegister: NavigatorResultRegister<*>)

    /**
     * return to the pre page and carry return value
     */
    abstract fun pop(resultData: Any?)

}

fun Navigator.activityOptions(options: ActivityOptionsCompat): Navigator {
    (this as? ActivityTargetNavigator)?.activityOptions(options)
    return this
}

fun Navigator.intent(intent: Intent): Navigator {
    (this as? ActivityTargetNavigator)?.intent(intent)
    return this
}