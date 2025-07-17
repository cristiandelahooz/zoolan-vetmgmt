package com.wornux.services.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.File;

public class JasperCompiler {

    public static void main(String[] args) {
        String directoryPath = "./src/main/resources/report";

        deleteAllJasperFiles(directoryPath);
        compileAllReports(directoryPath);
    }

    public static void deleteAllJasperFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            deleteJasperFilesInDirectory(directory);
        } else {
            System.err.println("The directory does not exist or is not valid.");
        }
    }

    private static void deleteJasperFilesInDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jasper")) {
                    // Eliminar archivo .jasper
                    if (file.delete()) {
                        System.out.println("Deleted file: " + file.getAbsolutePath());
                    } else {
                        System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                } else if (file.isDirectory()) {
                    // Recursivamente procesar los subdirectorios
                    deleteJasperFilesInDirectory(file);
                }
            }
        } else {
            System.err.println("Error listing files in the directory.");
        }
    }

    public static void compileAllReports(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            compileReportsInDirectory(directory);
        } else {
            System.err.println("The directory does not exist or is not valid.");
        }
    }

    private static void compileReportsInDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jrxml")) {
                    compileReport(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    compileReportsInDirectory(file);
                }
            }
        } else {
            System.err.println("Error listing files in the directory.");
        }
    }

    public static void compileReport(String jrxmlFilePath) {
        try {
            System.out.println("Compiling report: " + jrxmlFilePath);

            // Compilar el archivo .jrxml y guardar el .jasper en la misma carpeta
            String compiledFilePath = jrxmlFilePath.replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(jrxmlFilePath, compiledFilePath);

            System.out.println("Compilation successful. Compiled file saved to: " + compiledFilePath);
        } catch (JRException e) {
            System.err.printf("Error compiling the report (%s): %s%n", jrxmlFilePath, e.getMessage());
        }
    }
}
