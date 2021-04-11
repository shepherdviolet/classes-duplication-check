package com.github.shepherdviolet.classesduplicationcheck;

import sviolet.thistle.util.judge.CheckUtils;

import java.io.File;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Jar包类冲突检查器
 *
 * @author shepherdviolet
 */
class ClassesDuplicationChecker implements AutoCloseable {

    private String jarPath;

    private Map<String, Set<String>> duplicatedClasses = new HashMap<>(0);
    private Map<String, JarDuplicationInfo> duplicatedJars = new HashMap<>(0);

    public ClassesDuplicationChecker(String jarPath) {
        if (CheckUtils.isEmptyOrBlank(jarPath)) {
            throw new IllegalArgumentException("jarPath is null or empty");
        }
        this.jarPath = jarPath;
    }

    public void check() throws Exception {
        if (jarPath == null) {
            throw new IllegalStateException("ClassesDuplicationChecker has beeb closed");
        }

        File jarPathFile = new File(jarPath);
        SimpleLogger.print("Checking duplications, jarPath: " + jarPathFile.getAbsolutePath());

        // 遍历jar包记录每个类所在jar包

        Map<String, Set<String>> classes = new HashMap<>(10240);
        Map<String, Integer> classNumOfJar = new HashMap<>(128);

        File[] jarFiles = Optional.ofNullable(jarPathFile.listFiles(this::isJarFile))
                .orElse(new File[0]);

        for (File file : jarFiles) {
            if (SimpleLogger.DEBUG) {
                SimpleLogger.print("DEBUG: Reading jar " + file.getName());
            }
            try {
                JarFile jarFile = new JarFile(file);
                int classNum = 0;
                //遍历JAR包内的所有资源
                for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = entries.nextElement();
                    //资源路径
                    String resourcePath = entry.getName();
                    if (isClassResource(resourcePath)) {
                        classes.computeIfAbsent(resourcePath, k -> new HashSet<>())
                                .add(getJarFileName(file));
                        classNum++;
                    }
                }
                classNumOfJar.put(getJarFileName(file), classNum);
            } catch (Throwable t) {
                throw new Exception("Failed to read jar file: " + file, t);
            }
        }

        // 找出哪些类存在多个jar包中(即冲突)

        if (SimpleLogger.DEBUG) {
            SimpleLogger.print("DEBUG: Finding duplicated classes");
        }

        this.duplicatedClasses = classes.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 类 -> 冲突的Jar包 转为 冲突的Jar包 -> 类

        if (SimpleLogger.DEBUG) {
            SimpleLogger.print("DEBUG: Finding duplicated jars");
        }

        Map<String, JarDuplicationInfo> duplicatedJars = new HashMap<>(0);

        for (Map.Entry<String, Set<String>> entry : duplicatedClasses.entrySet()) {
            Map<String, Integer> duplicatedFiles = entry.getValue().stream()
                    .collect(Collectors.toMap(jarFile -> jarFile, classNumOfJar::get));
            duplicatedJars.computeIfAbsent(entry.getValue().toString(),
                    k -> new JarDuplicationInfo(duplicatedFiles))
                    .addDuplicatedClass(entry.getKey());
        }

        this.duplicatedJars = duplicatedJars;

        SimpleLogger.print("Check completed");

    }

    protected boolean isJarFile(File dir, String name) {
        return name.endsWith(".jar");
    }

    protected String getJarFileName(File file) {
        return file.getName();
    }

    protected boolean isClassResource(String resourcePath) {
        return resourcePath.endsWith(".class");
    }

    public Map<String, Set<String>> getDuplicatedClasses() {
        return duplicatedClasses;
    }

    public Map<String, JarDuplicationInfo> getDuplicatedJars() {
        return duplicatedJars;
    }

    public String getJarPath() {
        return jarPath;
    }

    @Override
    public void close() throws Exception {
        jarPath = null;
        duplicatedClasses = new HashMap<>(0);
        duplicatedJars = new HashMap<>(0);
    }

    /**
     * Jar包类冲突信息
     */
    public static class JarDuplicationInfo {

        /**
         * 哪几个Jar包有同名的类
         */
        private final Map<String, Integer> duplicatedFiles;

        /**
         * 哪些类冲突了
         */
        private Set<String> duplicatedClasses = new HashSet<>(32);

        private JarDuplicationInfo(Map<String, Integer> duplicatedFiles) {
            this.duplicatedFiles = duplicatedFiles;
        }

        private void addDuplicatedClass(String clazz) {
            duplicatedClasses.add(clazz);
        }

        public Map<String, Integer> getDuplicatedFiles() {
            return duplicatedFiles;
        }

        public Set<String> getDuplicatedClasses() {
            return duplicatedClasses;
        }

    }

}
