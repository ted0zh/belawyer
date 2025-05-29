package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.service.TextSummarizer;
import opennlp.tools.sentdetect.SentenceDetectorME;
import jakarta.annotation.PostConstruct;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextRankSummarizer implements TextSummarizer {

    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;

    @PostConstruct
    public void init() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        try (InputStream sModel = cl.getResourceAsStream("models/opennlp-bg-ud-btb-sentence-1.2-2.5.0.bin");
             InputStream tModel = cl.getResourceAsStream("models/opennlp-bg-ud-btb-tokens-1.2-2.5.0.bin")) {

            if (sModel == null || tModel == null) {
                throw new IllegalStateException(
                        "Не намерих моделите в classpath:/models/: " +
                                "opennlp-bg-ud-btb-sentence-1.2-2.5.0.bin или " +
                                "opennlp-bg-ud-btb-tokens-1.2-2.5.0.bin");
            }

            sentenceDetector = new SentenceDetectorME(new SentenceModel(sModel));
            tokenizer       = new TokenizerME(new TokenizerModel(tModel));
        }
    }



    @Override
    public List<String> summarize(String text, int numSentences) {
        String[] sentences = sentenceDetector.sentDetect(text);
        if (sentences.length <= numSentences) {
            return Arrays.asList(sentences);
        }

        List<RealVector> vectors = buildTfIdfVectors(sentences);

        double[][] simMatrix = buildSimilarityMatrix(vectors);

        double[] scores = pageRank(simMatrix, 0.85, 20);

        return pickTop(sentences, scores, numSentences);
    }

    private List<RealVector> buildTfIdfVectors(String[] sentences) {
        int n = sentences.length;
        List<String[]> tokenized = new ArrayList<>(n);
        Map<String, Integer> df = new HashMap<>();

        for (String sent : sentences) {
            String[] tokens = tokenizer.tokenize(sent.toLowerCase());
            Set<String> seen = new HashSet<>();
            tokenized.add(tokens);
            for (String t : tokens) {
                if (seen.add(t)) {
                    df.put(t, df.getOrDefault(t, 0) + 1);
                }
            }
        }

        List<String> vocab = new ArrayList<>(df.keySet());
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < vocab.size(); i++) {
            idx.put(vocab.get(i), i);
        }

        List<RealVector> vectors = new ArrayList<>(n);
        for (String[] tokens : tokenized) {
            double[] vec = new double[vocab.size()];
            Map<String, Integer> tf = new HashMap<>();
            for (String t : tokens) {
                tf.put(t, tf.getOrDefault(t, 0) + 1);
            }
            for (Map.Entry<String, Integer> e : tf.entrySet()) {
                String term = e.getKey();
                int termFreq = e.getValue();
                double idf = Math.log((double)n / (df.get(term) + 1));
                vec[idx.get(term)] = termFreq * idf;
            }
            vectors.add(new ArrayRealVector(vec));
        }
        return vectors;
    }

    private double[][] buildSimilarityMatrix(List<RealVector> vectors) {
        int n = vectors.size();
        double[][] sim = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                double score = (i == j) ? 0 : vectors.get(i).cosine(vectors.get(j));
                sim[i][j] = score;
                sim[j][i] = score;
            }
        }
        return sim;
    }

    private double[] pageRank(double[][] graph, double damping, int iterations) {
        int n = graph.length;
        double[] rank = new double[n];
        Arrays.fill(rank, 1.0 / n);
        for (int it = 0; it < iterations; it++) {
            double[] next = new double[n];
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    double outSum = Arrays.stream(graph[j]).sum();
                    if (outSum != 0) {
                        sum += graph[j][i] * (rank[j] / outSum);
                    }
                }
                next[i] = (1 - damping) / n + damping * sum;
            }
            rank = next;
        }
        return rank;
    }

    private List<String> pickTop(String[] sentences, double[] scores, int k) {
        return Arrays.stream(
                java.util.stream.IntStream.range(0, scores.length)
                        .boxed()
                        .sorted((i, j) -> Double.compare(scores[j], scores[i]))
                        .limit(k)
                        .sorted()
                        .map(i -> sentences[i])
                        .toArray(String[]::new)
        ).collect(Collectors.toList());
    }
}
