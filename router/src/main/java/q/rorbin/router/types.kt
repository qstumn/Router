package q.rorbin.router

import android.os.Bundle
import q.rorbin.router.navigator.Navigator

/**
 * @author changhai.qiu
 */
typealias RouteGenerator = (path: String) -> Route?

typealias NavigatorEngine = (source: Any, route: Route, requestIntentBundle: Bundle) -> Navigator?

/**
 * @param next if you do not need to interrupt, you should actively call next()
 */
typealias NavigatorInterceptor = (route: Route, params: Bundle, next: () -> Unit) -> Unit

typealias ResultCallback<R> = (NavigatorResult<R>) -> Unit