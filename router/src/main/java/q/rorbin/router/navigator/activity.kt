package q.rorbin.router.navigator

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import q.rorbin.router.*

/**
 * @author 邱长海
 */

open class ActivityTargetNavigator(
    source: Any, private var target: Class<Activity>, requestIntentBundle: Bundle,
    private var route: Route
) : SourceNavigator(source) {
    init {
        this.requestIntentBundle.putAll(requestIntentBundle)
    }

    private var intent: Intent? = null
    private var options: ActivityOptionsCompat? = null

    override fun path(path: String): ActivityTargetNavigator {
        val route = Router.route(path)
        if (route.target == null || !Activity::class.java.isAssignableFrom(route.target)) {
            throw RouteErrorException("only support activity target, but the target type is : $target")
        } else {
            this.route = route
            target = route.target as Class<Activity>
        }
        return this
    }

    /**
     * 添加activity自定义启动选项
     */
    fun activityOptions(options: ActivityOptionsCompat): ActivityTargetNavigator {
        this.options = options
        return this
    }

    /**
     * 使用自定义intent
     */
    fun intent(intent: Intent): ActivityTargetNavigator {
        this.intent = intent
        return this
    }

    protected fun buildIntent(context: Context): Intent {
        val intent = this.intent ?: Intent()
        intent.component = ComponentName(context, target)
        return intent
    }

    protected fun getOptionsBundle(context: Context): Bundle? {
        return options?.toBundle() ?: route.transition?.let {
            ActivityOptionsCompat.makeCustomAnimation(context, it.enterAnim, it.exitAnim).toBundle()
        } ?: ActivityOptionsCompat.makeBasic().toBundle()
    }


    override fun <T> push(onResult: ResultCallback<T>?) {
        val pushImpl: () -> Unit = {
            val fragment = if (onResult != null) {
                val fragmentManager = ResultBlankFragment.findFragmentManagerBySource(source)
                ResultBlankFragment.obtainSelfByFragmentManager(fragmentManager)
            } else null

            val requestCode = if (fragment != null && onResult != null)
                fragment.bindRequest(onResult as ResultCallback<Any>, source) else -1
            pushImpl(requestCode, fragment)
        }
        if (route.getInterceptor() != null) {
            route.getInterceptor()?.invoke(route, requestIntentBundle, pushImpl)
        } else {
            pushImpl()
        }
    }

    override fun push(resultRegister: NavigatorResultRegister<*>) {
        val pushImpl: () -> Unit = {
            val fragmentManager = ResultBlankFragment.findFragmentManagerBySource(source)
            val fragment = ResultBlankFragment.obtainSelfByFragmentManager(fragmentManager)
            if (resultRegister.requestCode == -1) {
                pushImpl(fragment.bindRequest(resultRegister.resultCallback as ResultCallback<Any>, source), fragment)
            } else {
                pushImpl(resultRegister.requestCode, fragment)
            }
        }
        if (route.getInterceptor() != null) {
            route.getInterceptor()?.invoke(route, requestIntentBundle, pushImpl)
        } else {
            pushImpl()
        }
    }

    private fun pushImpl(requestCode: Int, fragment: ResultBlankFragment?) {
        when (source) {
            is Activity -> {
                val intent = buildIntent(source).putExtras(requestIntentBundle)
                if (requestCode != -1) {
                    fragment?.startActivityForResult(intent, requestCode, getOptionsBundle(source))
                } else {
                    ActivityCompat.startActivity(source, intent, getOptionsBundle(source))
                }
            }
            is Fragment -> {
                if (source.context == null) {
                    throw RouteErrorException("source fragment must be attached to an activity")
                } else {
                    val intent = buildIntent(source.requireContext()).putExtras(requestIntentBundle)
                    if (requestCode != -1) {
                        fragment?.startActivityForResult(intent, requestCode, getOptionsBundle(source.requireContext()))
                    } else {
                        source.startActivity(intent, getOptionsBundle(source.requireContext()))
                    }
                }
            }
            is Context -> {
                val intent = buildIntent(source).putExtras(requestIntentBundle)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ActivityCompat.startActivity(source, intent, getOptionsBundle(source))
            }
            else -> {
                throw RouteErrorException("only support activity or fragment or context source, but the source type is : ${source::class.java.name}")
            }
        }
    }

    override fun <T> newTargetInstance(): T {
        return target.newInstance() as T
    }
}