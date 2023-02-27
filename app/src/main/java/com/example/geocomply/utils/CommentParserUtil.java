package com.example.geocomply.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class CommentParserUtil {

    public static List<String> extractByPattern(String text, Pattern pattern){
        List<String> containedUrls = new ArrayList<>();
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        return containedUrls.stream().distinct().collect(Collectors.toList());
    }

}
