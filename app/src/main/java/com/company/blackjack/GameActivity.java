package com.company.blackjack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.arch.lifecycle.ViewModelProviders;
import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends AppCompatActivity {
    Button menu;
    MediaPlayer blackjack_sound;
    //boolean hit = false;
    //boolean stand = false;
    //boolean cashout = true;
    //boolean deal = true;
    private GameViewModel viewModel;
    FragmentManager fm = getSupportFragmentManager();

    //ArrayList<Card> playerCards = new ArrayList<>();
    //ArrayList<Card> dealerCards = new ArrayList<>();
    //ArrayList<Card> deck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        viewModel = ViewModelProviders.of(GameActivity.this).get(GameViewModel.class);
        Intent game_intent = getIntent();
        int buyin = game_intent.getIntExtra("buyin",100);
        if(viewModel.amount == -1)
            viewModel.amount = buyin;
        TextView textTotal = (TextView) findViewById(R.id.chiptotal);
        textTotal.setText("$" + Integer.toString(viewModel.amount));

        TextView playerScore = (TextView) findViewById(R.id.player_score);
        TextView dealerScore = (TextView) findViewById(R.id.dealer_score);
        TextView playerHand = (TextView) findViewById(R.id.player_hand);
        TextView dealerHand = (TextView) findViewById(R.id.dealer_hand);

        if(viewModel.playerCards != null) {
            playerHand.setText("");
            for(int i = 0; i < viewModel.playerCards.size(); i++){
                playerHand.setText(playerHand.getText() + viewModel.playerCards.get(i).toString()+ "   ");
            }

            dealerHand.setText("");
            for(int i = 0; i < viewModel.dealerCards.size(); i++){
                dealerHand.setText(dealerHand.getText() + viewModel.dealerCards.get(i).toString()+ "   ");
            }

            if(viewModel.dealerCards.size()==1) {
                dealerHand.setText(viewModel.dealerCards.get(0).toString() + "   {HIDDEN CARD}");
            }

            if(viewModel.dealerCards.size()>1) {
                dealerScore.setText(Integer.toString(returnTotal(viewModel.dealerCards)));
            }

            playerScore.setText(Integer.toString(returnTotal(viewModel.playerCards)));
        }


        menu = (Button) findViewById(R.id.menu_button);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(GameActivity.this);
                mBuilder.setPositiveButton("Ok", null);
                mBuilder.setNegativeButton("Cancel", null);
                View mView = getLayoutInflater().inflate(R.layout.menu_listview, null);
                mBuilder.setTitle("Menu:");
                final ListView mList = (ListView) mView.findViewById(R.id.menu_list);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GameActivity.this,android.R.layout.simple_list_item_single_choice,
                        getResources().getStringArray(R.array.menu));
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

                mList.setAdapter(adapter);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface mdialog) {
                        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                    if (mList.getCheckedItemPosition() != -1) {
                                        dialog.dismiss();
                                        if (mList.getCheckedItemPosition() == 0 && viewModel.deal && viewModel.amount > 0) {
                                            startGame();
                                        } else if (mList.getCheckedItemPosition() == 1 && viewModel.stand) {
                                            stand();
                                        } else if (mList.getCheckedItemPosition() == 2 && viewModel.hit) {
                                            hit();
                                        } else if (mList.getCheckedItemPosition() == 3 && viewModel.cashout) {
                                            Intent backIntent = new Intent();
                                            //backIntent.putExtra("amount", amount);
                                            backIntent.putExtra("amount", viewModel.amount);
                                            setResult(RESULT_OK, backIntent);
                                            finish();
                                        } else
                                            Toast.makeText(GameActivity.this, "Action unavailable.", Toast.LENGTH_LONG).show();
                                    }
                            }
                        });
                    }

                });
                dialog.show();
            }
        });
    }

    public void startGame(){
        blackjack_sound = MediaPlayer.create(GameActivity.this, R.raw.chipstack);
        blackjack_sound.start();
        TextView playerScore = (TextView) findViewById(R.id.player_score);
        TextView dealerScore = (TextView) findViewById(R.id.dealer_score);
        TextView playerHand = (TextView) findViewById(R.id.player_hand);
        TextView dealerHand = (TextView) findViewById(R.id.dealer_hand);

        playerScore.setText("");
        playerHand.setText("");
        dealerHand.setText("");
        dealerScore.setText("");

        viewModel.deck = new ArrayList<>();
        viewModel.deck = shuffleDeck();
        viewModel.playerCards = new ArrayList<>();
        viewModel.dealerCards = new ArrayList<>();
        //playerCards.clear();
        //dealerCards.clear();

        viewModel.amount -= 100;
        TextView textTotal = (TextView) findViewById(R.id.chiptotal);
        textTotal.setText("$" + Integer.toString(viewModel.amount));

        Toast.makeText(GameActivity.this, "Hand dealt, you bet $100.", Toast.LENGTH_LONG).show();
        viewModel.deal = false;
        viewModel.stand = false;
        viewModel.hit = false;
        viewModel.cashout = false;

        viewModel.playerCards.add(viewModel.deck.remove(0));
        viewModel.playerCards.add(viewModel.deck.remove(0));
        viewModel.dealerCards.add(viewModel.deck.remove(0));

        playerHand.setText(viewModel.playerCards.get(0).toString() + "   " + viewModel.playerCards.get(1));
        dealerHand.setText(viewModel.dealerCards.get(0) + "   {HIDDEN CARD}");
        playerScore.setText(Integer.toString(returnTotal(viewModel.playerCards)));
        dealerScore.setText("0");
        viewModel.stand = true;
        viewModel.hit = true;

        bust(returnTotal(viewModel.playerCards),1);
    }

    public void bust(int total, int player) {
        if(player == 1) {
            if(total > 21) {
                Toast.makeText(GameActivity.this, "You bust, you lose.", Toast.LENGTH_LONG).show();
                viewModel.hit = false;
                viewModel.stand = false;
                viewModel.cashout = true;
                viewModel.deal = true;
            }
            else if(total == 21) {
                stand();
            }
        }
        if(player == 0) {
            if (total > 21) {
                Toast.makeText(GameActivity.this, "Dealer bust, you win.", Toast.LENGTH_LONG).show();
                viewModel.amount += 200;
                viewModel.hit = false;
                viewModel.stand = false;
                viewModel.cashout = true;
                viewModel.deal = true;
            } else {
                if (total == returnTotal(viewModel.playerCards)) {
                    Toast.makeText(GameActivity.this, "Tie.", Toast.LENGTH_LONG).show();
                    viewModel.amount += 100;
                    viewModel.hit = false;
                    viewModel.stand = false;
                    viewModel.cashout = true;
                    viewModel.deal = true;
                } else {
                    if (total > returnTotal(viewModel.playerCards)) {
                        Toast.makeText(GameActivity.this, "Dealer has the higher point total, you lose.", Toast.LENGTH_LONG).show();
                        viewModel.hit = false;
                        viewModel.stand = false;
                        viewModel.cashout = true;
                        viewModel.deal = true;
                    } else {
                        Toast.makeText(GameActivity.this, "Dealer has the lower point total, you win.", Toast.LENGTH_LONG).show();
                        viewModel.amount += 200;
                        viewModel.hit = false;
                        viewModel.stand = false;
                        viewModel.cashout = true;
                        viewModel.deal = true;
                    }
                }
            }
        }
        TextView textTotal = (TextView) findViewById(R.id.chiptotal);
        textTotal.setText("$" + Integer.toString(viewModel.amount));
    }

    public void hit(){
        blackjack_sound = MediaPlayer.create(GameActivity.this, R.raw.carddeal);
        blackjack_sound.start();
        TextView playerHand = (TextView) findViewById(R.id.player_hand);
        TextView playerScore = (TextView) findViewById(R.id.player_score);
        viewModel.playerCards.add(viewModel.deck.remove(0));
        playerHand.setText("");
        for(int i = 0; i < viewModel.playerCards.size(); i++){
            playerHand.setText(playerHand.getText() + viewModel.playerCards.get(i).toString()+ "   ");
        }
        playerScore.setText(Integer.toString(returnTotal(viewModel.playerCards)));
        bust(returnTotal(viewModel.playerCards),1);
    }

    public void stand(){
        TextView dealerHand = (TextView) findViewById(R.id.dealer_hand);
        TextView dealerScore = (TextView) findViewById(R.id.dealer_score);

        blackjack_sound = MediaPlayer.create(GameActivity.this, R.raw.carddeal);
        blackjack_sound.start();

        viewModel.stand = false;
        viewModel.hit = false;
        viewModel.cashout = false;
        viewModel.deal = false;

        viewModel.dealerCards.add(viewModel.deck.remove(0));

        if(returnTotal(viewModel.dealerCards) <= returnTotal(viewModel.playerCards)){
            while(returnTotal(viewModel.dealerCards)<17)
                viewModel.dealerCards.add(viewModel.deck.remove(0));
        }
        dealerHand.setText("");
        dealerScore.setText(Integer.toString(returnTotal(viewModel.dealerCards)));
        for(int i = 0; i < viewModel.dealerCards.size(); i++){
            dealerHand.setText(dealerHand.getText() + viewModel.dealerCards.get(i).toString()+ "   ");
        }
        bust(returnTotal(viewModel.dealerCards),0);
        viewModel.cashout = true;
        viewModel.deal = true;
    }

    public ArrayList shuffleDeck(){
        ArrayList<Card> newDeck = new ArrayList<>( );

        Card.Rank[] ranks = Card.Rank.values();
        Card.Suit[] suits = Card.Suit.values();

        for (Card.Suit suit : suits) {
            for (Card.Rank rank : ranks) {
                newDeck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(newDeck);
        return newDeck;
    }

    public int returnTotal(ArrayList<Card> hand){
        int total = 0;
        boolean ace = false;

        for(int i = 0; i < hand.size(); i++){
            if(hand.get(i).rank == Card.Rank.JACK || hand.get(i).rank == Card.Rank.QUEEN ||
                    hand.get(i).rank == Card.Rank.KING) {
                total += 10;
            }
            if(hand.get(i).rank == Card.Rank.TWO)
                total += 2;
            if(hand.get(i).rank == Card.Rank.THREE)
                total += 3;
            if(hand.get(i).rank == Card.Rank.FOUR)
                total += 4;
            if(hand.get(i).rank == Card.Rank.FIVE)
                total += 5;
            if(hand.get(i).rank == Card.Rank.SIX)
                total += 6;
            if(hand.get(i).rank == Card.Rank.SEVEN)
                total += 7;
            if(hand.get(i).rank == Card.Rank.EIGHT)
                total += 8;
            if(hand.get(i).rank == Card.Rank.NINE)
                total += 9;
            if(hand.get(i).rank == Card.Rank.TEN)
                total += 10;
            if(hand.get(i).rank == Card.Rank.ACE) {
                ace = true;
                total += 1;
            }
        }
        if (ace == true && total + 10 <= 21)
            total += 10;
        return total;
    }
}
