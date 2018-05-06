/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conver_manager;

import java.io.File;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author nowik
 */
class ImageProcessingJob {    
    
    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_INIT = "in progress";
    public static final String STATUS_DONE = "done";

    final File file;
    final File outputDir;
    final SimpleStringProperty status;
    final DoubleProperty progress;

    public ImageProcessingJob(File file, File outputDir) {
        this.file = file;
        this.outputDir = outputDir;
        this.status = new SimpleStringProperty(STATUS_WAITING);
        this.progress = new SimpleDoubleProperty(0);
    }
    
    public static ImageProcessingJob of(File file, File outputDir) {       
            ImageProcessingJob job = new ImageProcessingJob(file, outputDir);
            return job;        
    }

    public File getFile() {
        return file;
    }
    
    public File getOutputDir() {
        return outputDir;
    }   
    

    public Double getProgress() {
        return progress.get();
    }

    public void setProgress(Double progress) {
        this.progress.set(progress);
    }

    public String getStatus() {
        return this.status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public DoubleProperty getProgressProperty() {
        return progress;
    }

    public SimpleStringProperty getStatusProperty() {
        return status;
    }
    
}
