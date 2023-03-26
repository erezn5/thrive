package com.hackeruso.automation.model.api;


import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class UserFakerDetails {

    private final Faker faker = new Faker();

    public String getEmail(String userName){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(1000);
        return userName.replace(" ", "") + randomInt + "@gmail.com";
    }

    public String generateCommonLangPassword(int count, int start, int end, boolean letter, boolean number) {
        String upperCaseLetters = "A" + RandomStringUtils.random(count-1, start, end, letter, number);
        String lowerCaseLetters = "a" + RandomStringUtils.random(count-1, start, end, letter, number);
        String numbers = RandomStringUtils.randomNumeric(count + 1);
        String totalChars = RandomStringUtils.randomAlphanumeric(3);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

}

