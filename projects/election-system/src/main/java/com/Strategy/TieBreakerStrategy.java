package com.Strategy;

import java.util.List;
import java.util.Map;

public interface TieBreakerStrategy {

    String breakTie(Map<String,Integer> voteCount, int maxVotes, List<String> votes);
}
