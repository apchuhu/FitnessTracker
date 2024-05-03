package com.example.fitnesstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.DialogPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlanFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    CalendarView calendarView;
    Calendar calendar;
    private FloatingActionButton floatingActionButton;
    private ListView entryListView;
    private ArrayList<String> entryList;
    private ArrayAdapter<String> entryAdapter;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mCurrentUser;

    public PlanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlanFragment newInstance(String param1, String param2) {
        PlanFragment fragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plan, container, false);
        //database getting user and entries
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {

            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        } else {
            Log.e("Firebase", "User is not logged in");
        }
        //Adds the calendar
        calendarView = view.findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();
        //setDate(2024, 3, 17);
        getDate();


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Toast.makeText(requireContext(), month + 1 + "/" + dayOfMonth + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });

        //Button to create a popup
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItem(inflater);
            }
        });

        //List view
        entryList = new ArrayList<>();
        entryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, entryList);
        entryListView = view.findViewById(R.id.entryList);
        entryListView.setAdapter(entryAdapter);
        mDatabaseRef.child("entry").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entryList.clear(); // Clear the existing list
                // Iterate through the snapshot to retrieve entries
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String entry = entrySnapshot.getValue(String.class);
                    if (entry != null) {
                        entryList.add(entry);
                    }
                }
                // Update the adapter with the new data
                entryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error retrieving entries: " + error.getMessage());
            }
        });

        return view;
    }

    // Used for testing
    public void setDate(int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        long millSec = calendar.getTimeInMillis();
        calendarView.setDate(millSec);
    }

    public void getDate() {
        long date = calendarView.getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        calendar.setTimeInMillis(date);
        String selectDate = simpleDateFormat.format(calendar.getTime());
        Toast.makeText(requireContext(), selectDate, Toast.LENGTH_SHORT).show();
    }


//    private void showAddItem(LayoutInflater inflater) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        LayoutInflater popup = requireActivity().getLayoutInflater();
//        View popupView = inflater.inflate(R.layout.add_item, null);
//        final EditText editText = popupView.findViewById(R.id.editEntry);
//        if (Register.userIdMap == null) {
//            Register.userIdMap = new HashMap<>();
//        }
//
//        builder.setView(popupView).setTitle("Add Entry").
//                    setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                    //Register register = new Register();
//                    //HashMap<String, Object> entryMap = register.getUserIdMap();
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String entry = editText.getText().toString().trim();
//                        if (!entry.isEmpty()) {
//                            // Add the entry to the userIdMap directly
//
//                            Register.userIdMap.put("Entry", entry);
//                            // Add the entry to the entry list and notify the adapter
//                            entryList.add(entry);
//                            entryAdapter.notifyDataSetChanged();
//                            // Update the database with the new entry
//                            mDatabaseRef.setValue(Register.userIdMap);
//                            updateDatabase(Register.userIdMap);
//                            Toast.makeText(requireContext(), "Entry Added", Toast.LENGTH_SHORT).show();
//                            Log.d("EntryList", "Updated List: " + entryList.toString());
//                            dialog.dismiss();
//                        } else {
//                            Toast.makeText(requireContext(), "Entry is empty try again.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
private void showAddItem(LayoutInflater inflater) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    LayoutInflater popup = requireActivity().getLayoutInflater();
    View popupView = inflater.inflate(R.layout.add_item, null);
    final EditText editText = popupView.findViewById(R.id.editEntry);

    builder.setView(popupView).setTitle("Add Entry").
            setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String entry = editText.getText().toString().trim();
                    if (!entry.isEmpty()) {
                        // Retrieve the existing entry list from the database
                        mDatabaseRef.child("entry").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> entryList = new ArrayList<>();
                                if (snapshot.exists()) {
                                    // If entry data exists, retrieve it
                                    for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                                        String existingEntry = entrySnapshot.getValue(String.class);
                                        if (existingEntry != null) {
                                            entryList.add(existingEntry);
                                        }
                                    }
                                }
                                // Add the new entry to the list
                                entryList.add(entry);
                                // Update the entry list in the database
                                updateDatabase(entryList);
                                // Update the ListView adapter with the new data
                                entryAdapter.clear();
                                entryAdapter.addAll(entryList);
                                entryAdapter.notifyDataSetChanged();
                                // Notify the user
                                Toast.makeText(requireContext(), "Entry Added", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Firebase", "Error retrieving entry data: " + error.getMessage());
                            }
                        });
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Entry is empty try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
    AlertDialog dialog = builder.create();
    dialog.show();
}

    private void updateDatabase(ArrayList<String> entryList) {
        mDatabaseRef.child("entry").setValue(entryList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Entry list updated successfully");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error updating entry list: " + e.getMessage());
                    }
                });

    }

    private void showExerciseAddItem(LayoutInflater inflater){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View popupView = inflater.inflate(R.layout.add_exercise, null);
        final Spinner exerciseTypes = popupView.findViewById(R.id.spinnerExerciseTask);
        final EditText editWeight = popupView.findViewById(R.id.editWeight);
        final EditText editSets = popupView.findViewById(R.id.editSets);
        final EditText editReps = popupView.findViewById(R.id.editReps);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.exerciseList,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseTypes.setAdapter(adapter);

        builder.setView(popupView).setTitle("Select Exercise").setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedExercise = exerciseTypes.getSelectedItem().toString();
                        String weight = editWeight.getText().toString().trim();
                        String sets = editSets.getText().toString().trim();
                        String reps = editReps.getText().toString().trim();

                        if (!weight.isEmpty() && !sets.isEmpty() && !reps.isEmpty()){
                            String exerciseEntry = selectedExercise + ": Weight: " + weight + ", Sets: "
                                    + sets + ", Reps: " + reps;
                            entryList.add(exerciseEntry);
                            entryAdapter.notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Entry Added", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(requireContext(), "Entry is empty try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}