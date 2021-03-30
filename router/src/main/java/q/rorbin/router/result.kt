package q.rorbin.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author 邱长海
 */
internal class ResultBlankFragment : Fragment() {

    companion object {
        private const val TAG = "Router.Navigator.ResultBlankFragment"
        fun obtainSelfByFragmentManager(manager: FragmentManager): ResultBlankFragment {
            var fragment = manager.findFragmentByTag(TAG)
            if (fragment == null) {
                fragment = ResultBlankFragment()
                manager.beginTransaction().add(fragment, TAG).commitNowAllowingStateLoss()
            }
            return fragment as ResultBlankFragment
        }

        fun findFragmentManagerBySource(source: Any): FragmentManager {
            return when (source) {
                is FragmentActivity -> {
                    source.supportFragmentManager
                }
                is Fragment -> {
                    source.childFragmentManager
                }
                else -> {
                    throw RouteErrorException("only support activity or fragment source, but the source type is : $source")
                }
            }
        }
    }


    private val requestCodePool = RequestCodePool()
    private val resultCallbacks: MutableMap<Int, ResultCallback<Any>> = mutableMapOf()

    private val requestCodePoolForRegister = RequestCodePool(2001, 2500)
    private val resultCallbacksForRegister: MutableMap<Int, NavigatorResultRegister<Any>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    internal fun bindRequest(resultCallback: ResultCallback<Any>, lifecycleOwner: Any? = null): Int {
        val requestCode = requestCodePool.next()
        resultCallbacks[requestCode] = resultCallback
        if (lifecycleOwner is LifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestory() {
                    resultCallbacks.remove(requestCode)
                }
            })
        }
        return requestCode
    }

    internal fun bindRequestByRegister(register: NavigatorResultRegister<Any>) {
        val requestCode = requestCodePoolForRegister.next()
        register.requestCode = requestCode
        resultCallbacksForRegister[requestCode] = register
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
            resultCallbacks[requestCode]?.invoke(NavigatorResult(requestCode, data))
            resultCallbacksForRegister[requestCode]?.resultCallback?.invoke(NavigatorResult(requestCode, data))
        }
        resultCallbacks.remove(requestCode)
    }

}

internal class RequestCodePool(initValue: Int = 1500, private val limit: Int = 2000) {
    private val init = AtomicInteger(initValue)
    private var code = init

    @Synchronized
    fun next(): Int {
        if (code.get() == limit) {
            code = init
        } else {
            code.getAndIncrement()
        }
        return code.get()
    }
}