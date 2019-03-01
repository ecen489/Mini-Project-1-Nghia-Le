package com.company.blackjack;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SplashScreenFragment extends Fragment {
    Activity context;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context=getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    public void onStart() {
        super.onStart();
        Button play = (Button) context.findViewById(R.id.play_button);
        Button instruction = (Button) context.findViewById(R.id.instruction_button);

        play.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder sBuilder = new AlertDialog.Builder(getActivity());
                View sView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                sBuilder.setTitle("Amount of Buy-in:");
                final Spinner sSpinner = (Spinner) sView.findViewById(R.id.spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.bet_amount));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sSpinner.setAdapter(adapter);

                sBuilder.setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int buyin = Integer.parseInt(sSpinner.getSelectedItem().toString());
                        dialog.dismiss();
                        Intent game_intent=new Intent(getActivity(), GameActivity.class);
                        game_intent.putExtra("buyin", buyin);
                        startActivityForResult(game_intent, 6900);
                    }
                });

                sBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                sBuilder.setView(sView);
                AlertDialog dialog = sBuilder.create();
                dialog.show();
            }
        });

        instruction.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent instruct_intent=new Intent(getActivity(), InstructionActivity.class);
                startActivity(instruct_intent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==6900){
            int answer = intent.getIntExtra("amount", 0);
            Toast toast = Toast.makeText(getActivity(), "You cashed out with $" + answer + ".", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
