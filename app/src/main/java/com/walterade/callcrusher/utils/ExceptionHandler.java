package com.walterade.callcrusher.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Process;
import android.support.v4.content.FileProvider;

import com.walterade.callcrusher.BuildConfig;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String FILENAME = "out-of-memory.hprof";

    public static void install(Context context, String emailTo, String emailSubject) {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultHandler instanceof ExceptionHandler) {
            return;
        }
        ExceptionHandler oomHandler = new ExceptionHandler(emailTo, emailSubject, defaultHandler, context);
        Thread.setDefaultUncaughtExceptionHandler(oomHandler);
    }

    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;
    private final String emailTo;
    private final String emailSubject;

    public ExceptionHandler(String emailTo, String emailSubject, Thread.UncaughtExceptionHandler defaultHandler, Context context) {
        this.defaultHandler = defaultHandler;
        this.context = context.getApplicationContext();
        this.emailTo = emailTo;
        this.emailSubject = emailSubject;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (containsOom(ex)) {
            File heapDumpFile = new File(context.getFilesDir(), FILENAME);
            try {
                Debug.dumpHprofData(heapDumpFile.getAbsolutePath());
                Uri heapDumpUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", heapDumpFile);
                StringWriter stackTrace = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTrace));
                String message = String.format("Application: %s (%s)\nDevice: %s\nAndroid: %s\nStack trace:\n%s",
                    context.getPackageName(),
                    BuildConfig.VERSION_NAME,
                    AndroidUtils.getDeviceName(),
                    Build.VERSION.RELEASE,
                    stackTrace
                );
                Intent sendMail = IntentUtils.email(emailTo, emailSubject, message, new ArrayList<>(Collections.singletonList(heapDumpUri)));
                context.startActivity(Intent.createChooser(sendMail, "Out of Memory, send HPROF..."));
                Process.killProcess(Process.myPid());
                System.exit(0);
            } catch (Throwable ignored) {
                Timber.d(ignored.getMessage());
            }
        }
        else {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Intent sendMail = IntentUtils.email(emailTo, emailSubject, exceptionAsString, new ArrayList<>());
            context.startActivity(Intent.createChooser(sendMail, "Send Error Logs..."));
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
        //defaultHandler.uncaughtException(thread, ex);
    }

    private boolean containsOom(Throwable ex) {
        if (ex instanceof OutOfMemoryError) {
            return true;
        }
        while ((ex = ex.getCause()) != null) {
            if (ex instanceof OutOfMemoryError) {
                return true;
            }
        }
        return false;
    }
}