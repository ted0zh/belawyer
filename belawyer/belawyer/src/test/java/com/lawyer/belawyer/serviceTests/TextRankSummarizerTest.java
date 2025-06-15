package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.service.serviceImpl.TextRankSummarizer;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextRankSummarizerTest {

    private TextRankSummarizer summarizer;
    private SentenceDetectorME mockSentenceDetector;
    private TokenizerME mockTokenizer;

    @BeforeEach
    void setUp() throws Exception {
        summarizer = new TextRankSummarizer();

        mockSentenceDetector = mock(SentenceDetectorME.class);
        mockTokenizer = mock(TokenizerME.class);

        Field sentField = TextRankSummarizer.class.getDeclaredField("sentenceDetector");
        sentField.setAccessible(true);
        sentField.set(summarizer, mockSentenceDetector);

        Field tokenField = TextRankSummarizer.class.getDeclaredField("tokenizer");
        tokenField.setAccessible(true);
        tokenField.set(summarizer, mockTokenizer);
    }

    @Test
    void summarize_whenFewerSentencesThanRequested_returnsAllSentences() {
        String[] sentences = {"Sentence one", "Sentence two"};
        when(mockSentenceDetector.sentDetect(anyString())).thenReturn(sentences);

        int numSentencesRequested = 5;

        when(mockTokenizer.tokenize("Sentence one")).thenReturn(new String[]{"sentence", "one"});
        when(mockTokenizer.tokenize("Sentence two")).thenReturn(new String[]{"sentence", "two"});

        List<String> result = summarizer.summarize("Any text", numSentencesRequested);

        assertEquals(2, result.size());
        assertTrue(result.contains("Sentence one"));
        assertTrue(result.contains("Sentence two"));
        verify(mockSentenceDetector, times(1)).sentDetect("Any text");
    }
    @Test
    void summarize_whenMoreSentencesThanRequested_picksTopSentencesByConnectivity() {
        String[] sentences = {
            "alpha beta gamma",
            "beta gamma delta",
            "epsilon zeta eta",
            "beta gamma epsilon"
        };
        when(mockSentenceDetector.sentDetect(anyString()))
            .thenReturn(sentences);

        for (String sent : sentences) {
        String[] tokens = sent.toLowerCase().split(" ");
        when(mockTokenizer.tokenize(sent)).thenReturn(tokens);
        }

        int numSentencesRequested = 2;

        List<String> result = summarizer.summarize("Dummy text", numSentencesRequested);

        assertEquals(2, result.size(), "Should return exactly 2 sentences");

        assertEquals("epsilon zeta eta", result.get(0), "First chosen must be sentence index 2");
        assertEquals("beta gamma epsilon", result.get(1), "Second chosen must be sentence index 3");

        verify(mockSentenceDetector, times(1)).sentDetect("Dummy text");
        for (String sent : sentences) {
        verify(mockTokenizer, times(1)).tokenize(sent);
        }
    }


    @Test
    void pickTop_ranksByScoreAndMaintainsOriginalOrderAmongTop() throws Exception {
        String[] sentences = {"s0", "s1", "s2", "s3"};
        double[] scores = {0.1, 0.4, 0.2, 0.3};
        int k = 3;

        Method pickTopMethod = TextRankSummarizer.class
                .getDeclaredMethod("pickTop", String[].class, double[].class, int.class);
        pickTopMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> topSentences = (List<String>) pickTopMethod.invoke(
                summarizer,
                (Object) sentences,
                scores,
                k
        );

        assertEquals(3, topSentences.size());
        assertEquals("s1", topSentences.get(0));
        assertEquals("s2", topSentences.get(1));
        assertEquals("s3", topSentences.get(2));
    }

    @Test
    void buildTfIdfVectors_createsCorrectVectorsForSimpleSentences() {
        // Arrange
        String[] sentences = {"a b a", "b c"};
        when(mockSentenceDetector.sentDetect(anyString())).thenReturn(sentences);

        when(mockTokenizer.tokenize("a b a")).thenReturn(new String[]{"a","b","a"});
        when(mockTokenizer.tokenize("b c")).thenReturn(new String[]{"b","c"});

        List<String> result = summarizer.summarize("irrelevant", 1);

        assertEquals(1, result.size());
        assertTrue(
                result.get(0).equals("a b a") || result.get(0).equals("b c"),
                "Expected top-1 to be either 'a b a' or 'b c', but got: " + result.get(0)
        );

        verify(mockSentenceDetector, times(1)).sentDetect("irrelevant");
        verify(mockTokenizer, times(1)).tokenize("a b a");
        verify(mockTokenizer, times(1)).tokenize("b c");
    }

}
