package com.github.shepherdviolet.classesduplicationcheck;

import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.conversion.DateTimeUtils;
import sviolet.thistle.util.conversion.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class SimpleLogger {

    private static final File LOG_FILE = new File("./classes-duplications-" + DateTimeUtils.currentDateTimeString("yyyyMMdd-HH-mm-ss") + ".log");
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static boolean DEBUG = false;

    private static final ExecutorService WRITER_THREAD_POOL = ThreadPoolExecutorUtils.createLazy(5, "log-writer");
    private static final LinkedBlockingQueue<String> MESSAGE_QUEUE = new LinkedBlockingQueue<>(128);

    public static void print(String msg) {
        putMessage(msg);
        WRITER_THREAD_POOL.execute(WRITE_TASK);
    }

    public static void print(String msg, Throwable t) {
        putMessage(msg);
        putMessage(StringUtils.throwableToString(t));
        WRITER_THREAD_POOL.execute(WRITE_TASK);
    }

    private static final Runnable WRITE_TASK = new Runnable() {
        @Override
        public void run() {
            BufferedWriter writer = null;
            String msg;
            try {
                while ((msg = MESSAGE_QUEUE.poll(5, TimeUnit.SECONDS)) != null) {
                    if (writer == null) {
                        writer = getWriter();
                    }
                    writer.write(msg);
                    writer.newLine();
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable t) {
                // 日志写入异常, 只能输出STDOUT了
                t.printStackTrace();
            } finally {
                CloseableUtils.closeQuiet(writer);
            }
        }
    };

    private static void putMessage(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
        try {
            MESSAGE_QUEUE.put(msg);
        } catch (InterruptedException ignore) {
        }
    }

    private static BufferedWriter getWriter() throws Throwable {
        File dirFile = LOG_FILE.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory before write string to file, path:" + dirFile.getAbsolutePath());
            }
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(LOG_FILE, true);
            return new BufferedWriter(new OutputStreamWriter(outputStream, CHARSET));
        } catch (Throwable t) {
            // 避免创建Writer过程中报错导致文件流未关闭
            CloseableUtils.closeQuiet(outputStream);
            throw t;
        }
    }

}
