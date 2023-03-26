package com.hackeruso.automation.model.infra_components;

public enum SkillItems {
    WEB_SCRAPING_IN_PYTHON("Web-scraping in Python", "1"),
    WEB_SECURITY("Web Security", "2"),
    APK("APK", "9"),
    MOBILE("Mobile", "4"),
    SMTP("SMTP", "5"),
    TELNET("Telnet", "6"),
    SSH_AUTHENTICATION("SSH Authentication", "7"),
    DIRECTORY_BRUTE_FORCE("Directory Brute-force", "8"),
    PYTHON_SCRIPTING("Python Scripting", "9"),
    NETWORK_COMMUNICATION("Network Communication", "16"),
    FUNDAMENTALS("Fundamentals", "11");

    String value;
    String skillId;

    SkillItems(String value, String skillId){
        this.value = value;
        this.skillId = skillId;
    }

    public String getSkillId(){
        return skillId;
    }

    public String getValue() {
        return value;
    }
}
