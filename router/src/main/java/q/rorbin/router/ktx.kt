package q.rorbin.router

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import q.rorbin.router.navigator.Navigator
import q.rorbin.router.navigator.SourceNavigator
import q.rorbin.router.navigator.UriNavigator
import java.io.Serializable

/**
 * @author changhai.qiu
 */
internal fun Any.putToBundle(bundleKey: String = UNIQUE_DATA_KEY, bundle: Bundle? = null): Bundle {
    if (this is Bundle) {
        if (bundle != null) {
            bundle.putAll(this)
            return bundle
        }
        return this
    }
    val newBundle = bundle ?: Bundle()
    if (this is Parcelable) {
        newBundle.putParcelable(bundleKey, this)
    } else if (this is Serializable) {
        newBundle.putSerializable(bundleKey, this)
    } else if (this is Map<*, *>) {
        for ((key, value) in this.entries) {
            if (value == null || key !is String) {
                break
            }
            value.putToBundle(key, newBundle)
        }
    }
    return newBundle
}

fun Navigator.Companion.with(activity: Activity): Navigator {
    return SourceNavigator(activity)
}

fun Navigator.Companion.with(context: Context): Navigator {
    return SourceNavigator(context)
}

fun Navigator.Companion.with(fragment: Fragment): Navigator {
    return SourceNavigator(fragment)
}

fun Navigator.Companion.uri(uri: String): Navigator {
    return UriNavigator(uri)
}

fun Activity.findNavigator(): Navigator {
    return Navigator.with(this)
}

fun Fragment.findNavigator(): Navigator {
    return Navigator.with(this)
}

fun Context.findNavigator(): Navigator {
    return Navigator.with(this)
}

fun <T> Activity.navParams(key: String = UNIQUE_DATA_KEY): Lazy<T> = lazy {
    val bundle = intent.extras
    bundle?.get(key) as T
}

fun <T> Activity.navParams(key: String = UNIQUE_DATA_KEY, default: T): Lazy<T> = lazy {
    val bundle = intent.extras
    (bundle?.get(key) as? T) ?: default
}

fun <T> Fragment.navParams(key: String = UNIQUE_DATA_KEY): Lazy<T> = lazy {
    val bundle = arguments
    bundle?.get(key) as T
}

fun <T> Fragment.navParams(key: String = UNIQUE_DATA_KEY, default: T): Lazy<T> = lazy {
    val bundle = arguments
    (bundle?.get(key) as? T) ?: default
}


/**
 * register the listener for the return value, must be called at or before [androidx.lifecycle.Lifecycle.State.CREATED] lifecycle
 */
fun <R> FragmentActivity.registerForNavigatorResult(resultCallback: ResultCallback<R>): NavigatorResultRegister<R> {
    val register = NavigatorResultRegister(-1, resultCallback)
    registerForNavigatorResult(register, { supportFragmentManager }, lifecycle)
    return register
}

/**
 * register the listener for the return value, must be called at or before [androidx.lifecycle.Lifecycle.State.CREATED] lifecycle
 */
fun <R> Fragment.registerForNavigatorResult(resultCallback: ResultCallback<R>): NavigatorResultRegister<R> {
    val register = NavigatorResultRegister(-1, resultCallback)
    registerForNavigatorResult(register, { childFragmentManager }, lifecycle)
    return register
}

private fun <R> registerForNavigatorResult(register: NavigatorResultRegister<R>, fragmentManager: () -> FragmentManager,
                                           lifecycle: Lifecycle
) {
    val bindRequest = {
        val fragment = ResultBlankFragment.obtainSelfByFragmentManager(fragmentManager())
        fragment.bindRequestByRegister(register as NavigatorResultRegister<Any>)
    }
    when (lifecycle.currentState) {
        Lifecycle.State.DESTROYED, Lifecycle.State.RESUMED, Lifecycle.State.STARTED ->
            throw IllegalStateException("You can only register in onCreate or the previous lifecycle")
        Lifecycle.State.INITIALIZED ->
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                fun onCreate() {
                    bindRequest()
                }
            })
        Lifecycle.State.CREATED -> bindRequest()
    }
}