package com.abhi;

import com.Strategy.LexicStrategy;
import com.Strategy.PopularityStrategy;
import com.Strategy.TieBreakerStrategy;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        String[] votes= {"Abhishek","Raj","Arpit","Priyansh","Arpit","Abhishek","Priyansh"};
        HashMap<String, Integer> voteCount= new HashMap<>();
        for(String vote: votes){
            voteCount.put(vote, voteCount.getOrDefault(vote,0)+1);
        }
        TieBreakerStrategy lexicoStrategy= new LexicStrategy();
        String LexicWinner= ElectionWinner.findWinner(votes,lexicoStrategy);
        System.out.println("the lexico winner is "+ LexicWinner);
        TieBreakerStrategy popularityStrategy= new PopularityStrategy();
        String PopularityWinner= ElectionWinner.findWinner(votes, popularityStrategy);
        System.out.println("the Popularity winner is "+ PopularityWinner);




    }
}