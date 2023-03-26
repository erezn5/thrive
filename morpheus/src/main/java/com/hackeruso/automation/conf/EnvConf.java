package com.hackeruso.automation.conf;

import com.hackeruso.automation.utils.FileUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;


public class EnvConf {

    private static Configuration properties;

    static {
        load();
    }

    private EnvConf() {}

    private static void load() {
        Configurations configs = new Configurations();
        try {
            properties = configs.properties(getEnvPropFilePath());
            System.getProperties().forEach((key1, value) -> {
                String key = (String) key1;
                properties.setProperty(key, value);
            });
        } catch (ConfigurationException e) {
            System.err.println("failed to load '" + getEnvPropFilePath() + "' resource file");
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key){
        return properties.getString(key);
    }

    private static String getEnvPropFilePath(){
        String envPath = System.getProperty("env.conf");
        envPath = (envPath == null) ? FileUtil.getFile("/morpheus/src/main/resources/env/env.properties") : envPath;
        return envPath;
    }

    public static boolean getAsBoolean(String key){
        return Boolean.parseBoolean(getProperty(key));
    }

    public static int getAsInteger(String s) {
        return Integer.parseInt(getProperty(s));
    }

    public static String getDefaultTimeZone(){
        return getProperty("env.timezone.id");
    }
}

