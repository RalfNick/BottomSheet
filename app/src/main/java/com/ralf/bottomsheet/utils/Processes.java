package com.ralf.bottomsheet.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Processes {

    // 执行command并且读取第一行结果，如果进程执行失败返回null。
    // 注意：即使程序正常执行完，也可能返回null（没有输出）
    // 注意：不要执行交互型程序，可能导致函数无限挂起。例如不带任何参数的"cat"
    @Nullable
    public static String readFirstLine(String command) {
        try {
            return executeAndConsume(command, input -> {
                String result = input.readLine();
                // Consume the rest and do nothing.
                while (input.readLine() != null) ;
                return result;
            });
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    // 执行command并且读取所有结果，如果进程执行失败返回EmptyList。
    // 注意：即使程序正常执行完，也可能返回EmptyList（没有输出）
    // 注意：不要执行交互型程序，可能导致函数无限挂起。例如不带任何参数的"cat"
    @NonNull
    public static List<String> readAllLines(String command) {
        try {
            return executeAndConsume(command, input -> {
                List<String> result = new ArrayList<>();
                for (; ; ) {
                    String line = input.readLine();
                    if (line == null) {
                        break;
                    }
                    result.add(line);
                }
                return result;
            });
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private interface OutputConsumer<T> {
        T process(BufferedReader input) throws IOException;
    }

    private static <T> T executeAndConsume(
            String command,
            OutputConsumer<T> consumer
    ) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            NoopStreamConsumer error = new NoopStreamConsumer(process.getErrorStream());
            error.start();
            T result;
            try (BufferedReader input = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                result = consumer.process(input);
            }
            error.join();
            process.waitFor();
            return result;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    static class NoopStreamConsumer extends Thread {

        private static final String TAG = "NoopStreamConsumer";
        private InputStream mInputStream;

        public NoopStreamConsumer(InputStream inputStream) {
            super(TAG);
            mInputStream = inputStream;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream))) {
                // Consume stream and do nothing.
                while (reader.readLine() != null) ;
            } catch (IOException e) {
                // "IOException: Stream closed" may be encountered if we destroy process.
            }
        }
    }
}
