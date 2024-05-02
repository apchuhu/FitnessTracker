package com.example.fitnesstracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserInputFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EntryInputListener listener;
    private EditText editText;

    public UserInputFragment() {
        // Required empty public constructor
    }

    public interface EntryInputListener {
        void addEntry(String entryText);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserInputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserInputFragment newInstance(String param1, String param2) {
        UserInputFragment fragment = new UserInputFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        try {
            listener = (EntryInputListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement EntryInputListener");
        }
    }

    @SuppressLint("MissingInflatedId")
    @NonNull
    public AlertDialog onCreateView(@Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_userinput, null);
        editText = view.findViewById(R.id.entry_edit);
        build.setView(view)
                .setTitle("Enter Entry")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String entryText = editText.getText().toString();
                        listener.addEntry(entryText);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return build.create();
    }
}