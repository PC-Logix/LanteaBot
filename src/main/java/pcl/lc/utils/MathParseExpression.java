package pcl.lc.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Action {
    MULTIPLY,
    DIVIDE,
    ADD,
    SUB
}

public class MathParseExpression {
    public String inputString;
    public ArrayList<String> steps;
    public String result;

    public MathParseExpression(String inputString) {
        this(inputString, 100);
    }

    public MathParseExpression(String inputString, int maxIteration) {
        this.inputString = inputString;
        steps = new ArrayList<>();
        int i = 0;

        Pattern groupPattern = Pattern.compile("\\(([a-zA-Z0-9+*\\-\\d]+)\\)");

        while (i < maxIteration) {
            Matcher matcher = groupPattern.matcher(inputString);

            if (!matcher.find()) {
                break;
            }
            String group = parseMathGroup(matcher.group(1));
            inputString = Helper.replaceSubstring(inputString, group, matcher.start(), matcher.end());
            steps.add(inputString);
            i++;
        }
        inputString = parseMathGroup(inputString);
        result = inputString;
    }

    private static String doPatternSearch(String input, Pattern pattern, Action action) {
        return doPatternSearch(input, pattern, action, 10);
    }

    private static String doPatternSearch(String input, Pattern pattern, Action action, int maxIteration) {
        int i = 0;
        while (i < maxIteration) {
            Matcher matcher = pattern.matcher(input);

            if (!matcher.find())
                break;
            int math = 0;
            if (action.equals(Action.MULTIPLY))
                math = Integer.parseInt(matcher.group(1)) * Integer.parseInt(matcher.group(2));
            else if (action.equals(Action.DIVIDE))
                math = Integer.parseInt(matcher.group(1)) / Integer.parseInt(matcher.group(2));
            else if (action.equals(Action.ADD))
                math = Integer.parseInt(matcher.group(1)) + Integer.parseInt(matcher.group(2));
            else if (action.equals(Action.SUB))
                math = Integer.parseInt(matcher.group(1)) - Integer.parseInt(matcher.group(2));
            input = Helper.replaceSubstring(input, String.valueOf(math), matcher.start(), matcher.end());
            i++;
        }
        return input;
    }

    public static String parseMathGroup(String mathString) {
        if (mathString.equals(""))
            return "0";
        int maxIteration = 10;
        int i = 0;

        Pattern findDiceGroups = Pattern.compile("\\[([\\d,]+)]");
        Pattern findMultiplication = Pattern.compile("(\\d+)[x*]+(\\d+)");
        Pattern findDivision = Pattern.compile("(\\d+)[/÷]+(\\d+)");
        Pattern findSubtraction = Pattern.compile("(\\d+)[-]+(\\d+)");
        Pattern findAddition = Pattern.compile("(\\d+)[+]+(\\d+)");

        while (i < maxIteration) {
            Matcher matcher = findDiceGroups.matcher(mathString);

            if (!matcher.find())
                break;
            String[] strings = matcher.group(1).split(",");
            int math = 0;
            for (String str : strings)
                math += Integer.parseInt(str);
            mathString = Helper.replaceSubstring(mathString, String.valueOf(math), matcher.start(), matcher.end());
            i++;
        }
        mathString = doPatternSearch(mathString, findMultiplication, Action.MULTIPLY);
        mathString = doPatternSearch(mathString, findDivision, Action.DIVIDE);
        mathString = doPatternSearch(mathString, findSubtraction, Action.SUB);
        mathString = doPatternSearch(mathString, findAddition, Action.ADD);
        return mathString;
    }
}
