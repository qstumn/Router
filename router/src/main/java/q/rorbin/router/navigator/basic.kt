package q.rorbin.router.navigator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import q.rorbin.router.*

/**
 * @author changhai.qiu
 */
open class SourceNavigator(protected val source: Any) : Navigator() {

    override fun <T> newTargetInstance(): T {
        throw RouteErrorException("not implemented")
    }

    override fun path(path: String): Navigator {
        val route = Router.route(path)
        var navigator = Router.engine?.invoke(source, route, requestIntentBundle)
        if (navigator != null) {
            return navigator
        }
        val target = route.target
        navigator = when {
            target != null && Activity::class.java.isAssignableFrom(target) ->
                ActivityTargetNavigator(
                    source,
                    target as Class<Activity>,
                    requestIntentBundle,
                    route
                )
            target != null && Fragment::class.java.isAssignableFrom(target) ->
                FragmentTargetNavigator(target as Class<Fragment>, route)
            else -> null
        }
        return navigator
            ?: throw RouteErrorException("failed to create navigator, path is : ${route.path} ")
    }

    override fun <T> push(onResult: ((NavigatorResult<T>) -> Unit)?) {
        throw RouteErrorException("must call path() first to get the route before you can call push()")
    }

    override fun push(resultRegister: NavigatorResultRegister<*>) {
        throw RouteErrorException("must call path() first to get the route before you can call push()")
    }

    override fun pop(resultData: Any?) {
        when (source) {
            is Activity -> {
                if (resultData != null) {
                    source.intent.putExtras(resultData.putToBundle(bundle = source.intent.extras))
                    source.setResult(Activity.RESULT_OK, source.intent)
                }
                ActivityCompat.finishAfterTransition(source)
            }
            is Fragment -> {
                val navigator =
                    Router.engine?.invoke(source, Route(Fragment::class.java), requestIntentBundle)
                        ?: throw RouteErrorException("cannot find a navigator that can handle fragment pop with Route(Fragment::class.java)")
                navigator.pop(resultData)
            }
            else -> {
                throw RouteErrorException("cannot handle pop of the source type : $source")
            }
        }
    }
}

internal class UriNavigator(private val uri: String) :
    ActivityTargetNavigator(uri, Activity::class.java, Bundle(), Route().apply { path = uri }) {

    override fun path(path: String): UriNavigator {
        throw RouteErrorException("not implemented")
    }

    override fun <T> push(onResult: ResultCallback<T>?) {
        val intent = buildIntent(Router.application)
        intent.component = null
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(uri)
        intent.putExtras(requestIntentBundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ActivityCompat.startActivity(
            Router.application,
            intent,
            getOptionsBundle(Router.application)
        )
    }

    override fun pop(resultData: Any?) {
        throw RouteErrorException("not implemented")
    }
}