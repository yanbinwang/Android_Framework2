# 注意事项
1. 目前项目的minSdk是23(安卓6),targetSdk和compileSdk均采用最新的36(安卓16)故而之前版本标注的警告/淘汰写法,高版本的安卓手机都会强制执行
   1) 创建机制延后
      旧版中我们可以在super.onCreate前做一些代码操作,这本身其实并不规范但是不影响使用,新版必须在super.x后执行,确保类构建完成.安卓15+对于activity持有的window,intent等都有了更强制的管控,之前获取则可能为null
        override fun onCreate(savedInstanceState: Bundle?) {
            //dosomething-->旧版
            super.onCreate(savedInstanceState)
            //dosomething-->新版
        }
   2) 通知权限强制执行
      旧版中只要配置正常(对应通知创建渠道,id等)使用notify就能拉起通知,无需多余的操作.安卓13+开始对于通知的弹出,除了需要注册 <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />权限外,还需要跳转系统界面并授予应用通知弹出,该权限被划归为敏感权限,不授予应用不会报错,但是通知是绝对不可能展示的了
   3) 如果要在后台弹出提示框,还需要让用户授权在应用上显示的权限,至于Toast提示,安卓15+直接禁止,市面上目前所有的app只要不是厂商自家的,退到后台都是不会有Toast弹出的,不过可以弹出dialog(授权后)
   4) 存储权限进一步缩减和调整
     安卓6+开始引入了敏感权限组,对于文件读写的权限是以下两个
     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     安卓9+收紧了部分权限,引入了分区存储特性的概念,故而此时的权限变为   
     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     安卓13+重构了权限,变为
     <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
     <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
     <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
   5) 部分第三方库里使用了旧的权限检测代码,可能会引起功能调用的回调错误,目前直接把源码拉到本地,并剔除了其中权限检测的部分
   6) 安卓15+对于底部导航栏又有了新增的一个配置,应该是叫手势触控,寻常的导航栏操作代码只能操作UI部分,背景的话得调专门的api做处理
   7) 安卓10+多了屏幕边缘滑动关闭,可以不通过点击app的按钮和底部虚拟按键直接关闭页面,故而OnBackPressedCallback来接收所有的返回事件,应用内也不应采用onKeyDown回调来捕捉返回键
   8) 启动前台服务安卓15+强制要求使用startForegroundService不然直接失败,api调用规整更加严格,针对后台的服务必须配置foregroundServiceType属性
      不同属性对应的权限也要配置:
      <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
      <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
      <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
      <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />  
   9) 混淆文件调整,之前不过非kts架构的配置,使用的混淆文件是proguard,调整kts后,格式变为R8,导致部分混淆代码直接被移除,部分直接不执行,部分写法调整,kts文档本身内部的写法也有很多修改的地方
