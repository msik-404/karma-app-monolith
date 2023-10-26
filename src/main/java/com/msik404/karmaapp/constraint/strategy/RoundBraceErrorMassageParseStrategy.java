package com.msik404.karmaapp.constraint.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.msik404.karmaapp.pair.Pair;
import com.msik404.karmaapp.strategy.Strategy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoundBraceErrorMassageParseStrategy implements Strategy<String, Pair<String, String>> {

    @Override
    public Pair<String, String> execute(@NonNull String errorMessage) {

        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(errorMessage);

        String fieldName = null;
        if (matcher.find()) {
            fieldName = matcher.group(1);
        }
        String value = null;
        if (matcher.find()) {
            value = matcher.group(1);
        }
        return new Pair<>(fieldName, value);
    }
}
