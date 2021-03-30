# Router

纯kotlin编写的Android路由导航库

## how to use:

### 1. 初始化路由
```kotlin
Router.init(application, { path ->
    when (path) {
        "/route/second" -> Route(SecondActivity::class.java)
        else -> null
    }
})
```
### 2. 导航
```kotlin
Navigator.with(activity).path("/route/second").push()
findNavigator().path("/route/second").push()

//携带参数
findNavigator().path("/route/second").param("id", 100).push()
findNavigator().path("/route/second").params(User()).push()

//获取参数
class SecondActivity : AppCompatActivity() {
    private val id by navParams("id", default = -1)
    private val user by navParams<User>()
}
```

### 3. 回退
```kotlin
findNavigator().pop()

//携带返回值
findNavigator().pop("value")

//获取返回值有2种方式
//方式1 如上个页面此期间在后台被回收, 则该处监听会无效
findNavigator().path("/route/second").push<String> { result -> ...}

//方式2 调用registerForNavigatorResult扩展函数预注册返回结果监听, 任何情况下都有效
class FirstActivity : AppCompatActivity() {
    private val resultRegister = registerForNavigatorResult<String> { result -> ...}

    findNavigator().path("/route/second").push(resultRegister)
}
```

### 4. 常用API

#### 4.1 拦截器
```kotlin
//全局拦截器
Router.init(application, {...}, interceptor = { route, params, next ->
    val path = route.path
    val target = route.target
    //如无需拦截需主动调用next继续后续流程
    next()
})

//路由拦截器, 只针对该路由地址有效
"/route/second" -> Route(SecondActivity::class.java, interceptor = { route, params, next -> ...})
```

#### 4.2 路由创建器
```kotlin
Router.addRouteGenerator { path ->
    when (path) {
        "/route/second" -> Route(SecondActivity::class.java)
        else -> null
    }
}
```

#### 4.3 ActivityOptions
```kotlin
findNavigator().path("/route/second").cast<ActivityTargetNavigator>().activityOptions(...).push()
```

#### 4.4 单Activity的Fragment之间导航暂未支持, 可自行继承FragmentTargetNavigator后通过NavigatorEngine实现