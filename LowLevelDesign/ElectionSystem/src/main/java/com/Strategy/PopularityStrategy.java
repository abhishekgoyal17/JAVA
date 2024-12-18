package com.Strategy;

import java.util.*;

public class PopularityStrategy implements TieBreakerStrategy{
    private final List<String> popularityList;

    public PopularityStrategy(){
        this.popularityList= Arrays.asList("Priyansh","Arpit","Raj","Abhishek");
    }

    @Override
    public String breakTie(Map<String, Integer> voteCount, int maxVotes, List<String> candidates) {
         Map<String, Integer> popularityRank=new HashMap<>();

         for(int i=0;i<popularityList.size();i++){
             popularityRank.put(popularityList.get(i),i+1);
         }

         return candidates.stream()
                 .filter(vote ->voteCount.get(vote)==maxVotes)
                 .min(Comparator.comparingInt(popularityRank::get))
                 .orElse("");

    }
}
