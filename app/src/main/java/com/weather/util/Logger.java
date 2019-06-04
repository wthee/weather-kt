package com.weather.util;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.Html;
import android.text.Spanned;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Logger extends FrameLayout implements Thread.UncaughtExceptionHandler {
    private static boolean debuggable = true; //正式环境(false)不打印日志，也不能唤起app的debug界面
    private static Logger me;
    private Context mCurrentActivity;
    private static Thread.UncaughtExceptionHandler mDefaultHandler;


    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreLoggerView {
        // 有些自定义view在解绑时会跟本工具冲突(onPause后view空白)
        // 可以在activity上打上此注解忽略本工具View
        // 当然忽略后不能在界面上唤起悬浮窗
    }

    private Logger(final Context context) {
        super(context);
    }

    /**
     * 在application 的 onCreate() 方法初始化
     *
     * @param application
     */
    public static void init(Application application) {
        if (debuggable && me == null) {
            synchronized (Logger.class) {
                if (me == null) {
                    me = new Logger(application.getApplicationContext());
                    //获取系统默认异常处理器
                    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
                    //线程空闲时设置异常处理，兼容其他框架异常处理能力
                    Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                        @Override
                        public boolean queueIdle() {
                            Thread.setDefaultUncaughtExceptionHandler(me);//线程异常处理设置为自己
                            return false;
                        }
                    });
                }
            }
        }
    }

    /**
     * 捕获崩溃信息
     *
     * @param t
     * @param e
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 打印异常信息
        e.printStackTrace();
        // 我们没有处理异常 并且默认异常处理不为空 则交给系统处理
        if (!handleException(t, e) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler.uncaughtException(t, e);
        }
    }

    /*自己处理崩溃事件*/
    private boolean handleException(final Thread t, final Throwable e) {
        if (e == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(baos);
                e.printStackTrace(printStream);
                String s = baos.toString();
                String[] split = s.split("\t");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < split.length; i++) {
                    String s1 = split[i];
                    if ((!s1.contains("android.") && !s1.contains("java."))
                            && s1.contains("at") && i > 0) {
                        s1 = String.format("<br> <font color='#ff0000'>%s</font>", s1);
                    }
                    sb.append(s1).append("\t ");
                }
                mCurrentActivity = ActivityUtil.Companion.getInstance().getCurrentActivity();
                Spanned spanned = Html.fromHtml(sb.toString());
                Looper.prepare();
                Toast.makeText(mCurrentActivity, "无法运行，建议截图当前日志并反馈", Toast.LENGTH_LONG)
                        .show();
                AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                builder.setTitle("App Crash,Log:");
                builder.setMessage(spanned);
                builder.setPositiveButton("关闭app", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDefaultHandler.uncaughtException(t, e);
                    }
                });
                builder.setCancelable(false);
                builder.show();
                Looper.loop();
            }
        }.start();
        return true;
    }

}
