package com.github.shepherdviolet.classesduplicationcheck;

import sviolet.thistle.util.conversion.DateTimeUtils;

import java.util.Map;

/**
 * 程序入口
 *
 * @author shepherdviolet
 */
public class Main {
    
    public static void main(String[] args) {

        if ("true".equalsIgnoreCase(System.getProperty("debug", "false"))) {
            SimpleLogger.DEBUG = true;
        }

        SimpleLogger.print("==============================================================================================");
        SimpleLogger.print("Classes (Jars) duplication checker");
        SimpleLogger.print("==============================================================================================");
        SimpleLogger.print("Start time: " + DateTimeUtils.currentDateTimeString());

        try (ClassesDuplicationChecker checker = new ClassesDuplicationChecker(".")) {
            checker.check();

            SimpleLogger.print("\n==============================================================================================");
            SimpleLogger.print("Overview");
            SimpleLogger.print("==============================================================================================");

            for (ClassesDuplicationChecker.JarDuplicationInfo duplicationInfo : checker.getDuplicatedJars().values()) {
                StringBuilder logBuilder = new StringBuilder();
                for (Map.Entry<String, Integer> entry : duplicationInfo.getDuplicatedFiles().entrySet()) {
                    logBuilder.append(entry.getKey())
                            .append(" (")
                            .append(entry.getValue())
                            .append(" classes) ");
                }
                logBuilder.append("  -->  duplicated classes: ")
                        .append(duplicationInfo.getDuplicatedClasses().size());
                SimpleLogger.print(logBuilder.toString());
            }

            SimpleLogger.print("\n==============================================================================================");
            SimpleLogger.print("Details");
            SimpleLogger.print("==============================================================================================");

            for (ClassesDuplicationChecker.JarDuplicationInfo duplicationInfo : checker.getDuplicatedJars().values()) {

                SimpleLogger.print("\n-----------------------------------------------------------------");

                StringBuilder logBuilder = new StringBuilder("Duplication between: ");
                for (Map.Entry<String, Integer> entry : duplicationInfo.getDuplicatedFiles().entrySet()) {
                    logBuilder.append(entry.getKey())
                            .append(" (")
                            .append(entry.getValue())
                            .append(" classes) ");
                }

                SimpleLogger.print(logBuilder.toString());
                SimpleLogger.print("-----------------------------------------------------------------");

                for (String className : duplicationInfo.getDuplicatedClasses()) {
                    SimpleLogger.print(className + " -> DUPLICATED");
                }

            }

            SimpleLogger.print("\n==============================================================================================");
            SimpleLogger.print("Finished");
            SimpleLogger.print("==============================================================================================");

        } catch (Throwable t) {
            SimpleLogger.print("ERROR", t);
        }
    }

}
