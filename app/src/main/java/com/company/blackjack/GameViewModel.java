package com.company.blackjack;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    public int amount = -1;
    boolean hit = false;
    boolean stand = false;
    boolean cashout = true;
    boolean deal = true;
    ArrayList<Card> playerCards;
    ArrayList<Card> dealerCards;
    ArrayList<Card> deck;
}
