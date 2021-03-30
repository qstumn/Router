package q.rorbin.router

import android.app.Application

/**
 * @author changhai.qiu
 */
object Router {
    var initialized = false
        private set
    lateinit var application: Application
    internal var engine: NavigatorEngine? = null
    private var routeGenerators: MutableList<RouteGenerator> = mutableListOf()
    internal var interceptor: NavigatorInterceptor? = null

    /**
     * @param interceptor for global, if any route does not set its own interceptor, this global
     *                    interceptor will bt used for it
     * @param onGenerateRoute route generator
     * @param engine navigation creation engine, you can provide a custom engine, but the system
     *               has default engine, if custom engine not provided or cannot create a navigator,
     *               it will be created by the default engine
     */
    fun init(application: Application, onGenerateRoute: RouteGenerator, engine: NavigatorEngine? = null,
             interceptor: NavigatorInterceptor? = null) {
        if (initialized) {
            throw RouteErrorException("Router has been initialized")
        }
        this.application = application
        this.engine = engine
        this.routeGenerators.add(onGenerateRoute)
        this.interceptor = interceptor
        initialized = true
    }

    fun addRouteGenerator(onGenerateRoute: RouteGenerator) {
        this.routeGenerators.add(onGenerateRoute)
    }

    fun route(path: String): Route {
        for (routeGenerator in this.routeGenerators) {
            val route = routeGenerator(path)
            if (route != null) {
                route.path = path
                return route
            }
        }
        throw RouteErrorException("there is no route can match this path: $path")
    }
}