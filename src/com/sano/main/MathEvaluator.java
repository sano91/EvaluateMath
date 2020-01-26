package com.sano.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class MathEvaluator {

    public double calculate(String expression) {
        System.out.println(expression);
        System.out.println(expression);
        String simpleCase = justANumber(expression);
        if (simpleCase != null) {
            return Double.parseDouble(simpleCase);
        }
        String trimmedExpression = expression.replaceAll("\\s+", "");
        trimmedExpression = trimmedExpression.replaceAll("\n", "");
        List<String> expressionArray = generateArrayfromString(trimmedExpression);
        return calculating(expressionArray);
    }

    private double calculating(List<String> expressionArray) {
        while (expressionArray.size() > 1) {
            System.out.println("calculating: " + expressionArray);
            findAndCalculateTheActualScope(expressionArray); // 24 + 3 * 2 -> 24 + 6
        }
        return Double.parseDouble(expressionArray.get(0));
    }

    private List<String> findAndCalculateTheActualScope(List<String> expressionArray) {
        ActualScope actualScope = findScope(expressionArray);
        String resultOfTheActualScope = doMath(actualScope);
        if (expressionArray.get(actualScope.startIndex).equals("(")) {
            expressionArray.set(actualScope.startIndex, resultOfTheActualScope);
            expressionArray.subList(actualScope.startIndex + 1, actualScope.startIndex + 3).clear();
            return expressionArray;
        }
        return expressionArray;
    }

    private String doMath(ActualScope actualScope) {
        List<String> task = actualScope.sequence;
        while (task.size() > 1) {
            task = calc(task);
        }
        return task.get(0);
    }


    private List<String> calc(List<String> task)  {
        if (task.size() == 3 && task.get(0).equals("(")) {
            return task.subList(1, 2);
        }
        if (task.get(0).equals("(")) {
            task = task.subList(1, task.size() - 1);
        }
        if ("+-".contains(task.get(0))) {
            prepareTheFirstNumber(task);
        }
        int indexOfMultiplication = task.indexOf("*");
        int indexOfDivision = task.indexOf("/");
        int indexOfAddison = task.indexOf("+");
        int indexOfSubtraction = task.indexOf("-");
        if (indexOfMultiplication + indexOfDivision > -1) {
            int winnerOperation = getWinnerOperationPosition(indexOfMultiplication, indexOfDivision);
            String opSign = winnerOperation == indexOfMultiplication ? "*" : "/";
            return doMagic(opSign, task, winnerOperation);
        }  else if (indexOfAddison > -1) {
            return doMagic("+", task, indexOfAddison);
        } else if (indexOfSubtraction > -1) {
            return doMagic("-", task, indexOfSubtraction);
        } else {
            return task;
        }
    }

    private int getWinnerOperationPosition(int indexOfMultiplication, int indexOfDivision) {
        if(indexOfDivision == -1){
            return indexOfMultiplication;
        }
        else if(indexOfMultiplication == -1){
            return indexOfDivision;
        }
        return indexOfMultiplication < indexOfDivision ? indexOfMultiplication : indexOfDivision;
    }

    private List<String> prepareTheFirstNumber(List<String> task) {
        ActualScope firstNumberScope = new ActualScope();
        firstNumberScope.startIndex = 0;
        int lastIndex = 0;
        while (checkMinusOrPlus(task.get(lastIndex))) {
            lastIndex++;
        }
        //case like this: -23
        if (firstNumberScope.startIndex + 1 == lastIndex) {
            task.set(1, task.get(0) + task.get(1));
            task.remove(0);
            return task;
        }
        firstNumberScope.lastIndex = lastIndex - 1;
        firstNumberScope.sequence = task.subList(0, lastIndex);
        String firstTwoSignResult = evaluateMinusAndPlus(firstNumberScope);
        if (firstNumberScope.sequence.size() == 2) {
            task.set(2, firstTwoSignResult + task.get(2));
            task.removeAll(task.subList(0, 2));
            return task;
        } else {
            String ftsr = evaluateAllMinusAndPlus(firstNumberScope, firstTwoSignResult);
            task.set(lastIndex, ftsr + task.get(lastIndex));
            task.removeAll(task.subList(0, lastIndex));
            return task;
        }
    }

    private boolean checkMinusOrPlus(String minusOrPlus) {
        return "+-".contains(minusOrPlus);
    }

    private List<String> doMagic(String operationSign, List<String> task, int indexOfOperation) {
        ActualScope actualScope = createSimpleActualScope(task, indexOfOperation); //good
        String afterOperationMinusOrPlus = getMinusOrPlus(actualScope, operationSign);
        actualScope = dropMinusAndPlusAfterTheOperation(actualScope, afterOperationMinusOrPlus); // -34.54 - --+- 12.34 -> -34.54 + 12.34
        String resultOfTheSequence = calculateThePrepared(operationSign, actualScope);
        task.subList(actualScope.startIndex, actualScope.lastIndex+1).clear();
        task.add(actualScope.startIndex, resultOfTheSequence);
        return task;
    }

    private ActualScope dropMinusAndPlusAfterTheOperation(ActualScope actualScope, String afterMultiplicationMinusOrPlus) {
        if (afterMultiplicationMinusOrPlus != null) {
            actualScope.sequence.subList(2, actualScope.sequence.size() - 1).clear();
            if (actualScope.sequence.get(actualScope.sequence.size() - 1).startsWith("-") || actualScope.sequence.get(actualScope.sequence.size() - 1).startsWith("+")) {
                String firstLetter = "" + actualScope.sequence.get(actualScope.sequence.size() - 1).charAt(0);
                String resultBeforeNumber = firstLetter.equals(afterMultiplicationMinusOrPlus) ? "" : "-";
                String number = actualScope.sequence.get(actualScope.sequence.size() - 1).substring(1);
                actualScope.sequence.set(actualScope.sequence.size() - 1, resultBeforeNumber + number);
                actualScope.lastIndex = actualScope.startIndex + 2;
                return actualScope;
            }
            actualScope.sequence.set(actualScope.sequence.size() - 1, afterMultiplicationMinusOrPlus + actualScope.sequence.get(actualScope.sequence.size() - 1));
            actualScope.lastIndex = actualScope.startIndex + 2;
            return actualScope;
        }
        return actualScope;
    }

    private String calculateThePrepared(String s, ActualScope actualScope) {
        double firstNum = numberCutter(actualScope.sequence.get(0));
        double otherNum = numberCutter(actualScope.sequence.get(actualScope.sequence.size() - 1));
        double result = 0;
        switch (s) {
            case "+":
                result = firstNum + otherNum;
                break;
            case "-":
                result = firstNum - otherNum;
                break;
            case "*":
                result = firstNum * otherNum;
                break;
            case "/":
                result = firstNum / otherNum;
                break;
        }
        System.out.println(firstNum + " " + s + " " + otherNum + " = " + result);
        return "" + result;
    }

    private double numberCutter(String number){
        //simple case
        if(!(number.startsWith("+")) && !(number.startsWith("-")) || !("+-".contains(""+number.charAt(1)))){
            return Double.parseDouble(number);
        }
        int lastPositionOfMinus = number.lastIndexOf("-");
        int lastIndexOfPlus = number.lastIndexOf("+");
        int endPositionOfMinusOrPlus = lastIndexOfPlus > lastPositionOfMinus ? lastIndexOfPlus : lastPositionOfMinus;
        String pureNumber = number.substring(endPositionOfMinusOrPlus+1);
        ActualScope actualScope = new ActualScope();
        actualScope.sequence = Arrays.asList(number.substring(0, endPositionOfMinusOrPlus+1).split(""));
        String evaluatedFirstTwo = evaluateMinusAndPlus(actualScope);
        String allEvaluated = evaluateAllMinusAndPlus(actualScope, evaluatedFirstTwo);
        return allEvaluated != null ? Double.parseDouble(allEvaluated+pureNumber) : Double.parseDouble(pureNumber);
    }

    private String getMinusOrPlus(ActualScope actualScope, String operationSign) {
        ActualScope actualMinusOrPlusSequence = getMinusOrPlusSequence(operationSign, actualScope.sequence);
        if (actualMinusOrPlusSequence.sequence.size() == 1) {
            actualMinusOrPlusSequence.plusMinusResult = actualMinusOrPlusSequence.sequence.get(0);
            return actualMinusOrPlusSequence.plusMinusResult;
        } else if (actualMinusOrPlusSequence.sequence.size() == 0) {
            return null;
        }
        String evaulatedTheFirstTwo = evaluateMinusAndPlus(actualMinusOrPlusSequence);
        return evaluateAllMinusAndPlus(actualMinusOrPlusSequence, evaulatedTheFirstTwo);
    }

    private String evaluateAllMinusAndPlus(ActualScope actualMinusOrPlusSequence, String evaluatedTheFirstTwo) {
        for (int i = 2; i < actualMinusOrPlusSequence.sequence.size(); i++) {
            if (evaluatedTheFirstTwo.equals(actualMinusOrPlusSequence.sequence.get(i))) {
                actualMinusOrPlusSequence.plusMinusResult = "+";
            } else {
                actualMinusOrPlusSequence.plusMinusResult = "-";
            }
        }
        return actualMinusOrPlusSequence.plusMinusResult;
    }



    private String evaluateMinusAndPlus(ActualScope actualMinusOrPlusSequence) {
        if (actualMinusOrPlusSequence.sequence.get(0).equals(actualMinusOrPlusSequence.sequence.get(1))) {
            return "+";
        }
        return "-";
    }

    private ActualScope getMinusOrPlusSequence(String operationSign, List<String> sequence) {
        ActualScope actualScope = new ActualScope();
        if (operationSign.equals("*")) {
            return searchingMinusOrPlusSequence(actualScope, "*", sequence);
        }
        if (operationSign.equals("/")) {
            return searchingMinusOrPlusSequence(actualScope, "/", sequence);
        }
        if (operationSign.equals("+")) {
            return searchingMinusOrPlusSequence(actualScope, "+", sequence);
        } else {
            return searchingMinusOrPlusSequence(actualScope, "-", sequence);
        }
    }

    private ActualScope searchingMinusOrPlusSequence(ActualScope actualScope, String op, List<String> seq) {
        int opPos = seq.indexOf(op);
        int mightyLastIndex = seq.lastIndexOf("-");
        int otherMightyIndex = seq.lastIndexOf("+");
        if (mightyLastIndex > opPos || otherMightyIndex > opPos) {
            actualScope.startIndex = opPos + 1;
            actualScope.lastIndex = mightyLastIndex > otherMightyIndex ? mightyLastIndex : otherMightyIndex;
            actualScope.sequence = seq.subList(actualScope.startIndex, actualScope.lastIndex + 1);
            return actualScope;
        }
        return actualScope;
    }

    private ActualScope createSimpleActualScope(List<String> task, int operationPosition) {
        ActualScope actualScope = new ActualScope();
        actualScope.startIndex = findNumber(true, task, operationPosition); // if true, it search the first number
        actualScope.lastIndex = findNumber(false, task, operationPosition);
        actualScope.sequence = task.subList(actualScope.startIndex, actualScope.lastIndex + 1);
        return actualScope;
    }

    //  3*--2
    private int findNumber(boolean b, List<String> list, int opPos) {
        if (b) {
            return opPos - 1;
        }
        if (!("+-".contains(list.get(opPos + 1)))) {
            return opPos + 1;
        }
        int pos = opPos + 2;
        while ("+-".contains(list.get(pos))) {
            pos++;
        }
        return pos;
    }

    private ActualScope findScope(List<String> expressionArray) {
        ActualScope actualScope = new ActualScope();
        // case when found bracket
        if (expressionArray.contains("(")) {
            actualScope.startIndex = expressionArray.lastIndexOf("(");
            actualScope.lastIndex = findClosingBracketPositionForTheOpeningOne(expressionArray, actualScope.startIndex);
            actualScope.sequence = expressionArray.subList(actualScope.startIndex, actualScope.lastIndex + 1);
            return actualScope;
        }
        int indexOfMultiplication = expressionArray.indexOf("*");
        int indexOfDivision = expressionArray.indexOf("/");
        // case found strong operation
        if (indexOfDivision + indexOfMultiplication > -1) {
            return getActualScopeTheSmallest(expressionArray, indexOfMultiplication, indexOfDivision);
        }
        int indexOfAddison = expressionArray.indexOf("+");
        int indexOfSubtraction = expressionArray.indexOf("-");
        // case only weak operation found
        return getActualScopeTheSmallest(expressionArray, indexOfSubtraction, indexOfAddison);
    }

    private int findClosingBracketPositionForTheOpeningOne(List<String> expressionArray, int startIndex) {
        String[] convertedOriginalListToArray = expressionArray.toArray(new String[expressionArray.size()]);
        List<String> fromBracketList = Arrays.asList( Arrays.copyOfRange(convertedOriginalListToArray, startIndex, convertedOriginalListToArray.length));
        return fromBracketList.indexOf(")") + startIndex;
    }

    // checking
    private ActualScope getActualScopeTheSmallest(List<String> expressionArray, int indexOfOpOne, int indexOfOpTwo) {
        ActualScope actualScope;
        int strongOperationPosition;
        if (indexOfOpOne > 0) {
            if (indexOfOpTwo > 0 && indexOfOpOne < indexOfOpTwo) {
                strongOperationPosition = indexOfOpOne;
            }
            else if(indexOfOpTwo < 1){
                strongOperationPosition = indexOfOpOne;
            }
            else{
                strongOperationPosition = indexOfOpTwo;
            }
        }
        else {
            strongOperationPosition = indexOfOpTwo;
        }
        actualScope = createSimpleActualScope(expressionArray, strongOperationPosition);
        return actualScope;
    }

    private List<String> generateArrayfromString(String trimmedExpression) {
        List<String> resultList = new ArrayList<>();
        char[] ch = trimmedExpression.toCharArray();
        StringBuilder member = new StringBuilder();
        for (char c : ch) {
            if ("0123456789.".contains("" + c)) {
                member.append(c);
            } else {
                if (member.length() > 0) {
                    resultList.add(member.toString());
                    member.delete(0, member.length());
                }
                resultList.add("" + c);
            }
        }
        if (member.length() > 0) {
            resultList.add(member.toString());
        }
        return resultList;
    }

    private String justANumber(String expression) {
        String trimmedString =
                expression.replaceAll("\\(", "")
                        .replaceAll("\\)", "")
                        .replaceAll("\\s+", "");
        int plus = trimmedString.length() - trimmedString.replaceAll("\\+", "").length();
        int minus = trimmedString.length() - trimmedString.replaceAll("-", "").length();
        if (plus + minus <= 1 && !trimmedString.contains("*") && !trimmedString.contains("/") && (plus == 1 && trimmedString.charAt(0) == '+' || minus == 1 && trimmedString.charAt(0) == '-' || minus + plus == 0)) {
            return trimmedString;
        }
        return null;
    }

    public static void main(String[] args) {
        MathEvaluator mathEvaluator = new MathEvaluator();
        System.out.println(mathEvaluator.calculate("(123.45*(678.90 / (-2.5+ 11.5)-(((80 -(19))) *33.25)) / 20) - (123.45*(678.90 / (-2.5+ 11.5)-(((80 -(19))) *33.25)) / 20) + (13 - 2)/ -(-11)"));
    }


    private class ActualScope {
        int startIndex;
        int lastIndex;
        String plusMinusResult;
        List<String> sequence = new ArrayList<>();

        @Override
        public String toString() {
            return "ActualScope{" +
                    "startIndex=" + startIndex +
                    ", lastIndex=" + lastIndex +
                    ", plusMinusResult='" + plusMinusResult + '\'' +
                    ", sequence=" + sequence +
                    '}';
        }
    }
}