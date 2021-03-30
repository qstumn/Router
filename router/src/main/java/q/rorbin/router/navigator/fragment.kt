package q.rorbin.router.navigator

import androidx.fragment.app.Fragment
import q.rorbin.router.*

/**
 * @author changhai.qiu
 */

open class FragmentTargetNavigator(
    protected var target: Class<Fragment>,
    protected var route: Route
) : Navigator() {

    override fun <T> newTargetInstance(): T {
        return target.newInstance() as T
    }

    override fun path(path: String): FragmentTargetNavigator {
        val route = Router.route(path)
        if (route.target == null || !Fragment::class.java.isAssignableFrom(route.target)) {
            throw RouteErrorException("only support fragment target, but the target type is : $target")
        } else {
            this.route = route
            target = route.target as Class<Fragment>
        }
        return this
    }

    override fun params(data: Any?): FragmentTargetNavigator {
        throw RouteErrorException("not implemented")
    }

    override fun param(key: String, data: Any?): FragmentTargetNavigator {
        throw RouteErrorException("not implemented")
    }

    override fun <T> push(onResult: ResultCallback<T>?) {
        throw RouteErrorException("not implemented")
    }

    override fun push(resultRegister: NavigatorResultRegister<*>) {
        throw RouteErrorException("not implemented")
    }

    override fun pop(resultData: Any?) {
        throw RouteErrorException("not implemented")
    }
}