package com.msik404.karmaappmonolith.exception.constraint.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.msik404.karmaappmonolith.exception.constraint.dto.ParseResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoundBraceErrorMassageParseStrategy implements Strategy<String, ParseResult> {

    @NonNull
    @Override
    public ParseResult execute(@NonNull String errorMessage) {

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

        assert fieldName != null && value != null;

        return new ParseResult(fieldName, value);
    }
}
