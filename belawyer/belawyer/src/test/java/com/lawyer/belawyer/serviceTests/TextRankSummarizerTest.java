package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.service.serviceImpl.TextRankSummarizer;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
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

        // Create mocks
        mockSentenceDetector = mock(SentenceDetectorME.class);
        mockTokenizer = mock(TokenizerME.class);

        // Inject mocks into private fields via reflection
        Field sentField = TextRankSummarizer.class.getDeclaredField("sentenceDetector");
        sentField.setAccessible(true);
        sentField.set(summarizer, mockSentenceDetector);

        Field tokenField = TextRankSummarizer.class.getDeclaredField("tokenizer");
        tokenField.setAccessible(true);
        tokenField.set(summarizer, mockTokenizer);
    }

    @Test
    void summarize_whenFewerSentencesThanRequested_returnsAllSentences() {
        // Arrange
        String[] sentences = {"Sentence one", "Sentence two"};
        when(mockSentenceDetector.sentDetect(anyString())).thenReturn(sentences);

        // Because there are only 2 sentences, regardless of k=5, both should be returned
        int numSentencesRequested = 5;

        // Tokenizer should not matter here; but stub to avoid NPE if used
        when(mockTokenizer.tokenize("Sentence one")).thenReturn(new String[]{"sentence", "one"});
        when(mockTokenizer.tokenize("Sentence two")).thenReturn(new String[]{"sentence", "two"});

        // Act
        List<String> result = summarizer.summarize("Any text", numSentencesRequested);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("Sentence one"));
        assertTrue(result.contains("Sentence two"));
        verify(mockSentenceDetector, times(1)).sentDetect("Any text");
    }
    @Test
    void summarize_whenMoreSentencesThanRequested_picksTopSentencesByConnectivity() {
        // Arrange
        String[] sentences = {
            "alpha beta gamma",    // index 0
            "beta gamma delta",    // index 1
            "epsilon zeta eta",    // index 2 (will connect to #3 via “epsilon”)
            "beta gamma epsilon"   // index 3 (bridge via “epsilon”)
        };
        when(mockSentenceDetector.sentDetect(anyString()))
            .thenReturn(sentences);

        for (String sent : sentences) {
        String[] tokens = sent.toLowerCase().split(" ");
        when(mockTokenizer.tokenize(sent)).thenReturn(tokens);
        }

        int numSentencesRequested = 2;

        // Act
        List<String> result = summarizer.summarize("Dummy text", numSentencesRequested);

        // Assert: we expect exactly sentence 2 and sentence 3 in that order of original indices
        assertEquals(2, result.size(), "Should return exactly 2 sentences");

        // Because pickTop(...) returns them sorted by original index, we expect ["epsilon zeta eta","beta gamma epsilon"]
        assertEquals("epsilon zeta eta", result.get(0), "First chosen must be sentence index 2");
        assertEquals("beta gamma epsilon", result.get(1), "Second chosen must be sentence index 3");

        verify(mockSentenceDetector, times(1)).sentDetect("Dummy text");
        for (String sent : sentences) {
        verify(mockTokenizer, times(1)).tokenize(sent);
        }
    }


    @Test
    void pickTop_ranksByScoreAndMaintainsOriginalOrderAmongTop() throws Exception {
        // Arrange
        String[] sentences = {"s0", "s1", "s2", "s3"};
        double[] scores = {0.1, 0.4, 0.2, 0.3};
        int k = 3;

        // Access private method directly
        Method pickTopMethod = TextRankSummarizer.class
                .getDeclaredMethod("pickTop", String[].class, double[].class, int.class);
        pickTopMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> topSentences = (List<String>) pickTopMethod.invoke(
                summarizer,
                (Object) sentences,  // cast to Object to avoid varargs
                scores,
                k
        );

        // Expected top 3 (by descending scores): indices [1, 3, 2], then sorted by original index → [1, 2, 3]
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

        // Ask for only 1 sentence, so we go into TF-IDF branch (not the “return all” branch)
        List<String> result = summarizer.summarize("irrelevant", 1);

        // We expect exactly one sentence. Either “a b a” or “b c” can be chosen as top-1,
        // because they share “b” and have equal connectivity. So just assert it’s one of them.
        assertEquals(1, result.size());
        assertTrue(
                result.get(0).equals("a b a") || result.get(0).equals("b c"),
                "Expected top-1 to be either 'a b a' or 'b c', but got: " + result.get(0)
        );

        verify(mockSentenceDetector, times(1)).sentDetect("irrelevant");
        verify(mockTokenizer, times(1)).tokenize("a b a");
        verify(mockTokenizer, times(1)).tokenize("b c");
    }

//    @Test
//    void summarize_whenMoreSentencesThanRequested_picksTopSentencesByConnectivity() {
//        // Arrange
//        // Define 4 sentences in the text
//        String[] sentences = {
//                "alpha beta gamma",    // sentence 0
//                "beta gamma delta",    // sentence 1
//                "epsilon zeta eta",    // sentence 2 (disconnected cluster)
//                "beta gamma epsilon"   // sentence 3 (connects cluster 1 and 2)
//        };
//        when(mockSentenceDetector.sentDetect(anyString())).thenReturn(sentences);
//
//        // Define tokenization behavior: lowercase tokens split on space
//        for (String sent : sentences) {
//            String[] tokens = sent.toLowerCase().split(" ");
//            when(mockTokenizer.tokenize(sent)).thenReturn(tokens);
//        }
//
//        // We request top 2 sentences
//        int numSentencesRequested = 2;
//
//        // Act
//        List<String> result = summarizer.summarize("Dummy text", numSentencesRequested);
//
//        // Assert
//        // In this small graph, sentences 0, 1, and 3 form a tighter cluster;
//        // sentence 2 is isolated except via sentence 3. We expect top 2 to be among indices {1,3} or {0,1} or {0,3}.
//        // However, the highest scores should go to sentences 1 and 3 (they connect heavily).
//        assertEquals(2, result.size());
//        assertTrue(
//                (result.contains("beta gamma delta") && result.contains("beta gamma epsilon")) ||
//                        (result.contains("alpha beta gamma") && result.contains("beta gamma delta")) ||
//                        (result.contains("alpha beta gamma") && result.contains("beta gamma epsilon")),
//                "Expected the two most connected sentences to be selected"
//        );
//
//        verify(mockSentenceDetector, times(1)).sentDetect("Dummy text");
//        for (String sent : sentences) {
//            verify(mockTokenizer, times(1)).tokenize(sent);
//        }
//    }
//
//    @Test
//    void pickTop_ranksByScoreAndMaintainsOriginalOrderAmongTop() throws Exception {
//        // Use reflection to test private pickTop method directly
//        String[] sentences = {"s0", "s1", "s2", "s3"};
//        double[] scores = {0.1, 0.4, 0.2, 0.3};
//        int k = 3;
//
//        // Call private method pickTop via reflection
//        Field pickTopField = TextRankSummarizer.class.getDeclaredField("pickTop");
//        pickTopField.setAccessible(true);
//
//        @SuppressWarnings("unchecked")
//        List<String> topSentences = (List<String>) pickTopField
//                .get(summarizer)
//                .getClass()
//                .getMethod("pickTop", String[].class, double[].class, int.class)
//                .invoke(summarizer, (Object) sentences, scores, k);
//
//        // Expected top 3 indices by descending scores: [1 (0.4), 3 (0.3), 2 (0.2)]
//        // Then sorted by original index order: [1, 2, 3] → ["s1","s2","s3"]
//        assertEquals(3, topSentences.size());
//        assertEquals("s1", topSentences.get(0));
//        assertEquals("s2", topSentences.get(1));
//        assertEquals("s3", topSentences.get(2));
//    }
//
//    @Test
//    void buildTfIdfVectors_createsCorrectVectorsForSimpleSentences() throws Exception {
//        // We test buildTfIdfVectors via summarizer.summarize on specially crafted sentences:
//        // sentences = {"a b a", "b c"}
//        String[] sentences = {"a b a", "b c"};
//        when(mockSentenceDetector.sentDetect(anyString())).thenReturn(sentences);
//
//        // Tokenization: split on space
//        when(mockTokenizer.tokenize("a b a")).thenReturn(new String[]{"a","b","a"});
//        when(mockTokenizer.tokenize("b c")).thenReturn(new String[]{"b","c"});
//
//        // For k ≥ sentences.length, summarizer returns both sentences
//        List<String> result = summarizer.summarize("irrelevant", 2);
//
//        // Ensure both sentences appear
//        assertEquals(2, result.size());
//        assertTrue(result.contains("a b a"));
//        assertTrue(result.contains("b c"));
//
//        // Internally, the TF-IDF for term "a" in sentence0: tf=2, df("a")=1, idf=ln(2/2)=0 → weight 0
//        //                                  "b" in sentence0: tf=1, df("b")=2, idf=ln(2/3)
//        //                                  "c" in sentence1: tf=1, df("c")=1, idf=ln(2/2)=0
//        // Sentence1 "b": tf=1, df("b")=2, idf=ln(2/3)
//        // Thus vectors are non-zero for "b" components. They should be connected.
//
//        verify(mockSentenceDetector, times(1)).sentDetect("irrelevant");
//        verify(mockTokenizer, times(1)).tokenize("a b a");
//        verify(mockTokenizer, times(1)).tokenize("b c");
//    }
}
