package com.lawyer.belawyer.service;

import java.util.List;

public interface TextSummarizer {

    List<String> summarize(String text, int numSentences);
}