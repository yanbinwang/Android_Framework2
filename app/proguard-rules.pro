# ---------------------------- 基础优化配置 ----------------------------
# 指定代码的压缩级别（3-5次合理）
-optimizationpasses 5

# 禁止类名混合大小写（避免跨平台问题）
-dontusemixedcaseclassnames

# 输出混淆日志（开发阶段保留，发布可删除）
-verbose

# 允许反射访问类的成员
-allowaccessmodification

# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保留注解、泛型、内部类信息
-keepattributes *Annotation*, Signature, InnerClasses

# 兼容第三方库可能的旧支持库引用（必须保留）
-dontwarn android.support.**
# ---------------------------- 日志输出配置 ----------------------------
#未混淆的类和成员
-printseeds seeds.txt

#列出从 apk 中删除的代码
-printusage unused.txt

#混淆前后的映射
-printmapping mapping.txt
# ---------------------------- 系统核心组件（AndroidX适配） ----------------------------
# Application（全局上下文）
-keep public class * extends android.app.Application {
     public <init>();  # 无参构造（系统反射创建）
     public void onCreate();
     public void attachBaseContext(android.content.Context);  # 安卓15可能更早调用
}

# Activity 及子类（含 AppCompatActivity）的生命周期方法保留
# 同时适配原生 Activity 和 AndroidX 的 AppCompatActivity
-keepclassmembers class * extends android.app.Activity {
    # 构造方法
    public <init>();
    public <init>(android.os.Bundle);  # 某些场景下的带参构造
    # 核心生命周期
    public void onCreate(...);
    public void onStart();
    public void onResume();
    public void onPause();
    public void onStop();
    public void onDestroy();
    # 状态保存与恢复
    public void onSaveInstanceState(android.os.Bundle);
    public void onRestoreInstanceState(android.os.Bundle);  # 补充状态恢复方法
    # 高版本适配
    public void onNewIntent(android.content.Intent);  # 安卓13+强化
    public void onCreateContextMenu(...);  # 上下文菜单
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);  # 权限回调
    # 布局加载
    public void setContentView(...);
}

# 特别保留 AppCompatActivity 的额外方法（如支持库特有的生命周期）
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity {
    public void onSupportActionModeStarted(androidx.appcompat.view.ActionMode);
    public void onSupportActionModeFinished(androidx.appcompat.view.ActionMode);
}

# 保留权限相关的回调类（安卓13+权限申请必需）
-keep class androidx.core.app.ActivityCompat$OnRequestPermissionsResultCallback { *; }
-keepclassmembers class * implements androidx.core.app.ActivityCompat$OnRequestPermissionsResultCallback {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

# FragmentActivity（与Fragment配合使用）
-keepclassmembers public class * extends androidx.fragment.app.FragmentActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
    public androidx.fragment.app.FragmentManager getSupportFragmentManager();  # Fragment管理核心
}

# AndroidX Fragment（解决白屏/事件绑定问题）
-keepclassmembers public class * extends androidx.fragment.app.Fragment {
    public <init>();  # 无参构造（反射必需）
    public android.view.View onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle);  # 视图加载
    public void onViewCreated(android.view.View, android.os.Bundle);  # 事件绑定
    public void onCreate(android.os.Bundle);
    public void onResume();
    public void onAttach(android.content.Context);  # 与Activity关联
}

# Service
-keepclassmembers public class * extends android.app.Service {
    public void onCreate();
    public int onStartCommand(android.content.Intent, int, int);  # 服务启动
    public void onDestroy();
    public android.os.IBinder onBind(android.content.Intent);
}

# LifecycleService
-keep public class * extends androidx.lifecycle.LifecycleService {
    public <init>();  # 保留构造方法
    public void onCreate();
    public int onStartCommand(android.content.Intent, int, int);
    public void onDestroy();
    public android.os.IBinder onBind(android.content.Intent);
    # 保护 LifecycleService 特有的生命周期相关方法
    public androidx.lifecycle.Lifecycle getLifecycle();
}

# BroadcastReceiver
-keepclassmembers public class * extends android.content.BroadcastReceiver {
    public void onReceive(android.content.Context, android.content.Intent);  # 接收广播
}

# ContentProvider
-keepclassmembers public class * extends android.content.ContentProvider {
    public boolean onCreate();
    public android.database.Cursor query(...);
    public android.net.Uri insert(...);
    public int update(...);
    public int delete(...);
    public java.lang.String getType(...);
}

# 保留 RecyclerView.Adapter的所有子类不被混淆
-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    public <init>();
    public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView$ViewHolder, int);
    public int getItemCount();
    public androidx.recyclerview.widget.RecyclerView$ViewHolder onCreateViewHolder(android.view.ViewGroup, int);
}
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);
}

# 保留自定义 Dialog 子类
-keep public class * extends android.app.Dialog { *; }

# 保留自定义 PopupWindow 子类
-keep public class * extends android.widget.PopupWindow { *; }

# 保留自定义 ViewGroup 子类
-keep public class * extends android.view.ViewGroup { *; }

# Fragment管理核心类
-keep class androidx.fragment.app.FragmentManager { *; }
-keep class androidx.fragment.app.FragmentTransaction { *; }

# 保留所有匿名内部类（避免回调逻辑被移除）
-keepclassmembers class * {
    **$Lambda$*(...);
}
# 保留接口实现类的回调方法（如OnClickListener）
-keepclassmembers class * implements android.view.View$OnClickListener {
    public void onClick(android.view.View);
}
# ----------------------------  AndroidX架构组件 (ViewModel & LiveData) ----------------------------
# ViewModel（避免反射创建失败）
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);  # 保留所有构造（包括通过Factory创建的带参构造）
}

# LiveData（观察者模式核心）
-keep class androidx.lifecycle.LiveData { *; }
-keepclassmembers class * extends androidx.lifecycle.LiveData {
    public void observe(androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Observer);
    public void observeForever(androidx.lifecycle.Observer);
}

# Lifecycle（生命周期管理）
-keep class androidx.lifecycle.Lifecycle { *; }
-keep class androidx.lifecycle.LifecycleOwner { *; }
-keep class androidx.lifecycle.LifecycleObserver { *; }
-keep class androidx.lifecycle.Observer { *; }
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent <methods>;  # 保留生命周期注解方法
}
# ---------------------------- AndroidX 视图绑定 (DataBinding/ViewBinding) ----------------------------
# 保留ViewDataBinding子类（生成的绑定类）
-keep class * extends androidx.databinding.ViewDataBinding { *; }

# 保留绑定类的inflate方法（布局加载）
-keepclassmembers class * extends androidx.databinding.ViewDataBinding {
    public static ** inflate(android.view.LayoutInflater);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# 保留项目中DataBinding生成的包（替换为你的实际包名）
-keep class com.sqkj.lpevidence.databinding.** { *; }
# ---------------------------- AndroidX 常用UI组件 (RecyclerView等) ----------------------------
# RecyclerView（列表组件）
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    public void onBindViewHolder(...);  # 数据绑定
    public int getItemCount();
}
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);  # 构造方法（创建视图持有者）
}

# 自定义View（继承AndroidX控件）
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 常用AppCompat控件补充
-keep public class * extends androidx.appcompat.widget.AppCompatButton {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keep public class * extends androidx.appcompat.widget.AppCompatTextView {
    public <init>(android.content.Context, android.util.AttributeSet);
}
# ---------------------------- Kotlin协程 ----------------------------
# 保留协程核心类和反射所需的元数据
-keepattributes InnerClasses, Signature  # 补充泛型信息，Flow 依赖泛型
-keep class kotlinx.coroutines.** {
    public protected *;  # 保留公共/保护成员（核心API）
}

# 保留 Flow 核心类（数据流处理必需）
-keep class kotlinx.coroutines.flow.** {
    public protected *;
}

# 保留协程的 lambda 表达式生成的匿名类（避免逻辑丢失）
-keepclassmembers class kotlinx.coroutines.** {
    *** lambda$*(...);
}
-keepclassmembers class kotlinx.coroutines.flow.** {
    *** lambda$*(...);
}

# 保留协程的调度器和异常处理器（避免崩溃时无法捕获异常）
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class * {
    @kotlinx.coroutines.CoroutineScope <fields>;
}
# ---------------------------- 资源与权限相关 ----------------------------
# 保留资源ID（布局/控件引用不失效）
-keepclassmembers class **.R$* {
    public static <fields>;  # 保留所有资源字段（id、layout、drawable等）
}

# 权限与ActivityResult回调
-keep class androidx.activity.result.** { *; }
-keepclassmembers class * {
    public void onActivityResult(int, int, android.content.Intent);
}
# ---------------------------- ID/编译数据类 ----------------------------
# 保留所有native方法（JNI调用必需）
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# 不混淆资源ID（布局引用必需）
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留Parcelable（跨进程传输数据必需）
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留Serializable（Java序列化必需）
-keepnames class * implements java.io.Serializable  # 保留类名
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;  # 版本号
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;  # 非静态非 transient 字段
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);  # 序列化方法
    private void readObject(java.io.ObjectInputStream);  # 反序列化方法
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# ---------------------------- 按需添加的组件（根据项目使用情况选择） ----------------------------
# 若使用Navigation导航组件
# -keep class androidx.navigation.** { *; }
# -keepclassmembers class * extends androidx.navigation.fragment.FragmentNavigatorDestinationBuilder {
#     public <init>(...);
# }

# 若使用Room数据库
# -keep class androidx.room.** { *; }
# -keepclassmembers class * extends androidx.room.RoomDatabase {
#     public static ** getInstance(...);
# }

# 若使用WorkManager后台任务
# -keep class androidx.work.** { *; }
# -keepclassmembers class * extends androidx.work.Worker {
#     public <init>(android.content.Context, androidx.work.WorkerParameters);
# }
# ---------------------------- h5混淆 ----------------------------
# 保留 H5 与原生交互的关键注解和方法
-keepattributes *JavascriptInterface*

# 保留项目中实际的 JS 交互内部类（替换为你的实际类名）
-keepclassmembers class com.example.home.utils.WebJavaScriptObject { *; }

# 保留所有被 @JavascriptInterface 注解的方法（核心）
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# ---------------------------- Glide图片库混淆 ----------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
# ---------------------------- OKHttp + Retrofit2混淆 ----------------------------
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# ---------------------------- Begin: proguard configuration for Gson ----------------------------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
# ---------------------------- 高德地图混淆 ----------------------------
# 3D 地图
-keep class com.amap.api.maps.**{*;}
-keep class com.autonavi.**{*;}
-keep class com.amap.api.trace.**{*;}
# 定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
# 搜索u
-keep class com.amap.api.services.**{*;}
# 2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}
# 导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}
# ---------------------------- 支付宝混淆 ----------------------------
#-libraryjars  libs/alipaySdk-20170725.jar
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
# ---------------------------- 微信分享 ----------------------------
-keep class com.tencent.mm.opensdk.** {
    *;
}
-keep class com.tencent.wxop.** {
    *;
}
-keep class com.tencent.mm.sdk.** {
    *;
}
# ---------------------------- 图片裁剪混淆 ----------------------------
-dontwarn com.yanzhenjie.durban.**
-keep class com.yanzhenjie.durban.**{*;}
-dontwarn com.yanzhenjie.loading.**
-keep class com.yanzhenjie.loading.**{*;}
# ---------------------------- 图片库混淆 ----------------------------
-dontwarn com.yanzhenjie.album.**
-keep class com.yanzhenjie.album.**{*;}
# ---------------------------- 阿里oss混淆 ----------------------------
-keep class com.alibaba.sdk.android.oss.** { *; }
-dontwarn okio.**
-dontwarn org.apache.commons.codec.binary.**
# ---------------------------- 阿里ARouter混淆 ----------------------------
# 1. 保留所有带 @Route 注解的类（类名不混淆、不被移除）
-keep @com.alibaba.android.arouter.facade.annotation.Route class * { *; }

# 2. 保留 ARouter 路由表和核心接口
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep public class com.alibaba.android.arouter.facade.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}
-keep class * implements com.alibaba.android.arouter.facade.service.InterceptorService { *; }

# 3. 保护 IProvider 接口及实现类（如果使用）
-keep interface * implements com.alibaba.android.arouter.facade.template.IProvider
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider { *; }

# 4. 保留带 @Route 注解的 Activity/Fragment 的核心方法
-keepclassmembers @com.alibaba.android.arouter.facade.annotation.Route class * extends android.app.Activity {
    public void onCreate(android.os.Bundle);
    public void onResume();
    public void onPause();
    public void onDestroy();
}
-keepclassmembers @com.alibaba.android.arouter.facade.annotation.Route class * extends androidx.fragment.app.Fragment {
    public void onCreate(android.os.Bundle);
    public android.view.View onCreateView(...);
    public void onViewCreated(android.view.View, android.os.Bundle);
}
# 保留 ARouter 生成的所有路由表类
-keep class *$$ARouter$$Group$$* { *; }
-keep class *$$ARouter$$Provider$$* { *; }
-keep class *$$ARouter$$Inject$$* { *; }
# 保留 @Autowired 注解
-keep class com.alibaba.android.arouter.facade.annotation.Autowired { *; }
# 保留所有类中被 @Autowired 标记的字段
-keepclassmembers class * {
    @com.alibaba.android.arouter.facade.annotation.Autowired <fields>;
}
# 保留 Kotlin 元数据（避免 Lambda 和扩展函数被误处理）
-keepattributes KotlinMetadata
-keep class kotlin.Metadata { *; }
# 保留ARouter的LogisticsCenter类及其init方法（用于反射调用）
-keep class com.alibaba.android.arouter.core.LogisticsCenter {
    public static void init(android.content.Context, java.util.concurrent.ThreadPoolExecutor);
}
# 允许反射访问ARouter路由表的字段
-keepclassmembers class *$$ARouter$$Group$$* {
    public static <fields>;
    public static <methods>;
}

# 5. 忽略编译期注解类的缺失（运行时无需存在）
-dontwarn javax.lang.model.**
-dontwarn com.sun.source.**
-dontwarn javax.annotation.**
# ---------------------------- 腾讯x5混淆 ----------------------------
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
# ------------------ Keep LineNumbers and properties ---------------- #
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
# --------------------------------------------------------------------------
#-keep class com.tencent.smtt.export.external.**{
#    *;
#}
#-keep class  com.tencent.smtt.export.internal.**{
#    *;
#}
#-keep class com.tencent.tbs.video.interfaces.IUserStateChangedListener {
#	*;
#}
#-keep class com.tencent.smtt.sdk.CacheManager {
#	public *;
#}
#-keep class com.tencent.smtt.sdk.CookieManager {
#	public *;
#}
#-keep class com.tencent.smtt.sdk.WebHistoryItem {
#	public *;
#}
#-keep class com.tencent.smtt.sdk.WebViewDatabase {
#	public *;
#}
#-keep class com.tencent.smtt.sdk.WebBackForwardList {
#	public *;
#}
#-keep public class com.tencent.smtt.sdk.WebView {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebView$HitTestResult {
#	public static final <fields>;
#	public java.lang.String getExtra();
#	public int getType();
#}
#-keep public class com.tencent.smtt.sdk.WebView$WebViewTransport {
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebView$PictureListener {
#	public <fields>;
#	public <methods>;
#}
#-keepattributes InnerClasses
#-keep public enum com.tencent.smtt.sdk.WebSettings$** {
#    *;
#}
#-keep public enum com.tencent.smtt.sdk.QbSdk$** {
#    *;
#}
#-keep public class com.tencent.smtt.sdk.WebSettings {
#    public *;
#}
#-keepattributes Signature
#-keep public class com.tencent.smtt.sdk.ValueCallback {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebViewClient {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.DownloadListener {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebChromeClient {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebChromeClient$FileChooserParams {
#	public <fields>;
#	public <methods>;
#}
#-keep class com.tencent.smtt.sdk.SystemWebChromeClient{
#	public *;
#}
## 1. extension interfaces should be apparent
#-keep public class com.tencent.smtt.export.external.extension.interfaces.* {
#	public protected *;
#}
## 2. interfaces should be apparent
#-keep public class com.tencent.smtt.export.external.interfaces.* {
#	public protected *;
#}
#-keep public class com.tencent.smtt.sdk.WebViewCallbackClient {
#	public protected *;
#}
#-keep public class com.tencent.smtt.sdk.WebStorage$QuotaUpdater {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebIconDatabase {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.WebStorage {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.DownloadListener {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.QbSdk {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.QbSdk$PreInitCallback {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.CookieSyncManager {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.Tbs* {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.utils.LogFileUtils {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.utils.TbsLog {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.utils.TbsLogClient {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.CookieSyncManager {
#	public <fields>;
#	public <methods>;
#}
## Added for game demos
#-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
#	public <fields>;
#	public <methods>;
#}
#-keep public class com.tencent.smtt.utils.Apn {
#	public <fields>;
#	public <methods>;
#}
#-keep class com.tencent.smtt.** {
#	*;
#}
#-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
#	public <fields>;
#	public <methods>;
#}
#-keep class MTT.ThirdAppInfoNew {
#	*;
#}
#-keep class com.tencent.mtt.MttTraceEvent {
#	*;
#}
## Game related
#-keep public class com.tencent.smtt.gamesdk.* {
#	public protected *;
#}
#-keep public class com.tencent.smtt.sdk.TBSGameBooter {
#        public <fields>;
#        public <methods>;
#}
#-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
#	public protected *;
#}
#-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
#	public protected *;
#}
#-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
#	public *;
#}
-keep class com.tencent.smtt.** {*;}
-keep class com.tencent.tbs.** {*;}
# ---------------------------- 今日头条兼容 ----------------------------
-keep class me.jessyan.autosize.** { *; }
-keep interface me.jessyan.autosize.** { *; }
## ---------------------------- GreenDao混淆 ----------------------------
#-keep class org.greenrobot.greendao.**{*;}
#-keep public interface org.greenrobot.greendao.**
#-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
#public static java.lang.String TABLENAME;
#}
#-keep class **$Properties
#-keep class net.sqlcipher.database.**{*;}
#-keep public interface net.sqlcipher.database.**
#-dontwarn net.sqlcipher.database.**
#-dontwarn org.greenrobot.greendao.**
# ---------------------------- 播放器混淆 ----------------------------
-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
# ---------------------------- 刷新混淆 ----------------------------
-keep class com.scwang.smart.** { *; }
-dontwarn com.scwang.smart.**
# ---------------------------- 测试库混淆 ----------------------------
# 1. 忽略测试库所有类的缺失警告（解决 R8 报错）
-dontwarn com.example.debugging.**
# 2. 如果主项目有用反射/条件调用测试库代码，保留相关类名/方法名
-keepnames class com.example.debugging.utils.DebuggingUtil {
    public static void init(android.content.Context, java.lang.Class);
}
# ---------------------------- 项目库混淆 ----------------------------
-keep class com.example.topsheet.** {*;}
-keep class com.example.objectbox.dao.** {*;}
-keep class com.example.thirdparty.media.oss.bean.** {*;}
-keep class com.example.thirdparty.pay.bean.** {*;}

-keep class com.example.common.databinding.** {*;}
-keep class com.example.common.base.** {*;}
-keep class com.example.common.bean.** {*;}
-keep class com.example.common.event.** {*;}
-keep class com.example.common.network.repository.** {*;}

-keep class com.example.home.databinding.** {*;}
-keep class com.example.home.bean.** {*;}

-keep class com.example.evidence.databinding.** {*;}
-keep class com.example.evidence.bean.** {*;}

-keep class com.example.account.databinding.** {*;}
-keep class com.example.account.bean.** {*;}
-keep class com.example.account.utils.faceverify.** {*;}