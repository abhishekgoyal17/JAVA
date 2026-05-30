package com.abhi;

import com.Strategy.TieBreakerStrategy;

import java.util.*;

public class ElectionWinner {

    public ElectionWinner() {
    }

    public static String findWinner(String[] votes, TieBreakerStrategy strategy){

        HashMap<String, Integer> voteCount= new HashMap<>();
        for(String vote: votes){
            voteCount.put(vote, voteCount.getOrDefault(vote,0)+1);
        }

        int maxVotes= Collections.max(voteCount.values());
        List<String> candidates= new ArrayList<>(voteCount.keySet());

        return strategy.breakTie(voteCount,maxVotes,candidates);

    }
}
