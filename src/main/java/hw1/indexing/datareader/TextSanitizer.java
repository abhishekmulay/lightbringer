package hw1.indexing.datareader;


import hw1.main.ConfigurationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/1/17.
 */


//https://stackoverflow.com/questions/18830813/how-can-i-remove-punctuation-from-input-text-in-java

public class TextSanitizer {

    private static final String STOP_WORDS_FILE_PATH = ConfigurationManager.getConfigurationValue("stop.words.file.path");

    // remove special characters like , etc. split into words and remove whitespace
    public static String[] tokenize(String text) {
        String lowercaseText = text.toLowerCase();
        String removedColon = lowercaseText.replaceAll(":", "");
        String removedQuote = removedColon.replaceAll("'", "");
        String removeComma = removedQuote.replaceAll(",", "");
        String removeTick = removeComma.replaceAll("`", "");
        String replaceNewLineWithSpace = removeTick.replaceAll("\n", " ");
        String replaceForwardSlash = replaceNewLineWithSpace.replaceAll("\\/","");
        String replaceEndingFullStop = replaceForwardSlash.replaceAll("\\.(?!\\w)", "");
        String removeBrackets = replaceEndingFullStop.replaceAll("[()]", "");
        String removeQuestionMark = removeBrackets.replaceAll("\\?", "");

        String[] tokens = removeQuestionMark.split(" ");

        ArrayList<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
        //remove all elements that are null or equals to ""
        tokenList.removeAll(Arrays.asList(null,""));

        for (String s : tokenList)
            s.trim().replaceAll("\\s+","");

        return tokenList.toArray(new String[0]);
    }


    public static String[] removeStopWords (String text) {
        List<String> stopWords = Collections.unmodifiableList(getStopWords());
        String[] tokens = tokenize(text);
        List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
        tokenList.removeAll(stopWords);

        StringBuffer buffer = new StringBuffer();
        for (String token : tokenList)
            buffer.append(token).append(" ");

        return buffer.toString().split(" ");
    }


    private static List<String> getStopWords() {
         List<String> stopWords = new ArrayList<String>();
        try {
            InputStream content = new FileInputStream(STOP_WORDS_FILE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(content));
            while (br.readLine() != null) {
                stopWords.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }


    public static void main(String[] args) {
        final String text = "Liberace's ex-lover testified Tuesday that a\n" +
                "convicted drug dealer spoke of a ``bloody mess'' after a 1981\n" +
                "quadruple murder and said, ``The whole thing got out of hand.''\n" +
                "   Scott Thorson said defendant Eddie Nash also told him he was\n" +
                "going to teach a lesson to a group of people who had robbed him,\n" +
                "saying, ``I'll have these people on their knees.''\n" +
                "   Nash, 59, whose real name is Adel Nasrallah, and his bodyguard\n" +
                "Gregory Diles, 40, are charged with the Laurel Canyon slayings in\n" +
                "which sex-film star John Holmes once was tried and acquitted.\n" +
                "   Witnesses at the current preliminary hearing said Nash was robbed\n" +
                "of cash, drugs and jewelry by two subsequent murder victims. Witness\n" +
                "David Lind, who participated in the robbery, said Nash fell to his\n" +
                "knees and asked for time to pray, assuming he would be killed.\n" +
                "   Scott Thorson, 29, who was Liberace's companion from 1977 to\n" +
                "1982, said he became close friends with Nash after he bought cocaine\n" +
                "from him in early 1981. He said he wound up living at Nash's house\n" +
                "when Liberace evicted him in 1982.\n" +
                "   At the end of June 1981, Thorson recalled, he went to Nash's\n" +
                "house to buy cocaine. Soon, he said, Diles appeared with Holmes in\n" +
                "tow. Diles took Holmes into a bedroom, Thorson said, adding he was\n" +
                "told to leave but listened outside the door.\n" +
                "   ``Eddie (Nash) was screaming at the top of his lungs,'' Thorson\n" +
                "said. ``... He threatened that he would kill John Holmes' family and\n" +
                "he would kill him if he didn't take him to the home of the people\n" +
                "who robbed him.''\n" +
                "   Diles and Holmes left and Nash said that he had sent Diles ``to\n" +
                "get his property.'' Thorson said.\n" +
                "   Later, Thorson said, Nash sank into a depression and went on a\n" +
                "drug binge. During one of those sessions, Thorson said Nash confided\n" +
                "in him.\n" +
                "   ``He discussed ... Wonderland (the street on which the murders\n" +
                "occurred) and he had gone a little too far,'' said Thorson.\n" +
                "   ``He used the term, `a bloody mess,''' said Thorson. ``He said\n" +
                "the whole thing got out of hand.''\n" +
                "   ``Did he say he had the murders done?'' asked Deputy District\n" +
                "Attorney Dale Davidson.\n" +
                "   ``He didn't come right out and say it,'' said Thorson. ``... He\n" +
                "said they were pinning the murders on him.''\n" +
                "   The victims, Ronald Launius, 37, Roy DeVerell, 42, Barbara\n" +
                "Richardson, 22, and Joy Audrey Miller, 46, were bludgeoned to death\n" +
                "on July 1, 1981, about two days after Lind said Nash was robbed.\n" +
                "   Launius' wife, Susan, survived the attack but was never able to\n" +
                "identify her attacker. Holmes died of acquired immune deficiency\n" +
                "syndrome last March.\n" +
                "   Thorson made headlines when he sued Liberace for $12 million\n" +
                "claiming the entertainer reneged on a promise to support him for\n" +
                "life. Liberace died of AIDS in 1987.";


            String[] tokenList = removeStopWords(text);
            for (String term : tokenList)
                System.out.println("["+term+"]");
    }


}
