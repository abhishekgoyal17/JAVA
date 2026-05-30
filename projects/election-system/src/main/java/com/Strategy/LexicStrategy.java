package com.Strategy;

import java.util.List;
import java.util.Map;

public class LexicStrategy implements TieBreakerStrategy{
    @Override
    public String breakTie(Map<String, Integer> voteCount, int maxVotes, List<String> candidates) {
//        String winner="";
//        for(Map.Entry<String,Integer> entry: voteCount.entrySet()){
//            String candidate= entry.getKey();
//            int count=entry.getValue();
//
//            if(count>maxVotes ||( count==maxVotes && candidate.compareTo(winner)<0)){
//                maxVotes=count;
//                winner=candidate;
//            }
//        }

        return candidates.stream().filter(candidate->voteCount.get(candidate)==maxVotes)
                .min(String::compareTo)
                .orElse("");}
}
