package com.example.fitnesstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
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
import java.util.Objects;


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
    private String today_date;
    CalendarView calendarView;
    Calendar calendar;
    private FloatingActionButton floatingActionButton;
    private ListView entryListView;
    private ArrayList<String> entryList;
    private ArrayAdapter<String> entryAdapter;
    private DatabaseReference mDatabaseRefEntries;
    private DatabaseReference mDatabaseRefDate;
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

//            mDatabaseRefDate = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());

        } else {
            Log.e("Firebase", "User is not logged in");
        }
        //Adds the calendar
        calendarView = view.findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();
        //setDate(2024, 3, 17);
        today_date = getDate();
        mDatabaseRefEntries = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child(today_date);


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
        //mDatabaseRefEntries.child("entry").child("general_task").addListenerForSingleValueEvent(new ValueEventListener() {
        mDatabaseRefEntries.child("entry").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entryList.clear(); // Clear the existing list
                // Iterate through the snapshot to retrieve entries
                DataSnapshot generalTaskSnapshot = snapshot.child("general_task");
                for (DataSnapshot entrySnapshot : generalTaskSnapshot.getChildren()) {
                    String entry = entrySnapshot.getValue(String.class);
                    if (entry != null) {
                        entryList.add(entry);
                    }
                }
                DataSnapshot exerciseTaskSnapshot = snapshot.child("exercise_task");
                for (DataSnapshot exerciseSnapshot : exerciseTaskSnapshot.getChildren()) {
                    String exerciseName = exerciseSnapshot.getKey();
                    StringBuilder exerciseEntry = new StringBuilder(exerciseName + ": ");

                    // Iterate through exercise details (weight, sets, reps)
                    for (DataSnapshot detailSnapshot : exerciseSnapshot.getChildren()) {
                        String detailKey = detailSnapshot.getKey();
                        String detailValue = detailSnapshot.getValue(String.class);
                        exerciseEntry.append(detailKey).append(": ").append(detailValue).append(", ");
                    }
                    exerciseEntry.delete(exerciseEntry.length() - 2, exerciseEntry.length());

                    // Add the exercise entry to the list
                    entryList.add(exerciseEntry.toString());
                }
                // Update the adapter with the new data
                entryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error retrieving entries: " + error.getMessage());
            }
        });

        entryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view1, int position, long id) {
                String heldEntry = entryList.get(position);
                showEditOrDeletePopup(heldEntry, position);
                return true;
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

    public String getDate() {
        long date = calendarView.getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy", Locale.getDefault());
        calendar.setTimeInMillis(date);
        String selectDate = simpleDateFormat.format(calendar.getTime());
        Toast.makeText(requireContext(), selectDate, Toast.LENGTH_SHORT).show();

        return selectDate;
    }


private void showAddItem(LayoutInflater inflater) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    LayoutInflater popup = requireActivity().getLayoutInflater();
    View popupView = inflater.inflate(R.layout.add_item, null);
    final Spinner taskType = popupView.findViewById(R.id.spinnerTask);
    final EditText editText = popupView.findViewById(R.id.editEntry);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.taskTypes,
            android.R.layout.simple_spinner_dropdown_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    taskType.setAdapter(adapter);

    builder.setView(popupView).setTitle("Add Entry").
            setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String selectedTaskType = taskType.getSelectedItem().toString();

                    if (selectedTaskType.equals("Exercise")){
                        dialog.dismiss();
                        showExerciseAddItem(inflater);
                    }else{
                        String entry = editText.getText().toString().trim();
                        if (!entry.isEmpty()) {
                            // Retrieve the existing entry list from the database
                            mDatabaseRefEntries.child("entry").child("general_task").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> entryList = new ArrayList<>();
                                    if (snapshot.exists()) {
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
                                    mDatabaseRefEntries.child(today_date);
                                    mDatabaseRefEntries.child("entry").child("general_task").setValue(entryList);
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

//    private void updateDatabase(ArrayList<String> entryList) {
//        mDatabaseRefEntries.child("entry").setValue(entryList)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("Firebase", "Entry list updated successfully");
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("Firebase", "Error updating entry list: " + e.getMessage());
//                    }
//                });
//
//    }

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

                        HashMap<String, String> exerciseMap = new HashMap<>();
                        exerciseMap.put("Weight(lbs)", weight);
                        exerciseMap.put("Sets", sets);
                        exerciseMap.put("Reps", reps);

                        mDatabaseRefEntries.child(today_date);
                        mDatabaseRefEntries.child("entry").child("exercise_task").child(selectedExercise).setValue(exerciseMap);


                        if (!weight.isEmpty() && !sets.isEmpty() && !reps.isEmpty()){
                            String exerciseEntry = selectedExercise + ": Weight(lbs): " + weight + ", Sets: "
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

    private void showEditOrDeletePopup(String heldEntry, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit or Delete").setItems(new CharSequence[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch (which){
                    case 0:
                        if (heldEntry.contains("Weight(lbs)")){
                            String exerciseName = heldEntry.split(":")[0].trim();
                            showExerciseEditPopup(exerciseName, position);
                        }
                        else {
                            showEditPopup(heldEntry, position);
                        }
                        break;
                    case 1:
                        showDeletePopup(position);
                        break;
                }
            }

        }).setNegativeButton("Cancel", null).create().show();
    }

    private void showEditPopup(String heldEntry, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Entry");

        final EditText editText = new EditText(requireContext());
        editText.setText(heldEntry);
        builder.setView(editText);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updateEntry = editText.getText().toString();
                entryList.set(position, updateEntry);
                entryAdapter.notifyDataSetChanged();
                String entryID = String.valueOf(position);
                if (entryID != null){
                    mDatabaseRefEntries.child("entry").child("general_task").child(entryID).setValue(updateEntry)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(requireContext(), "Entry Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showExerciseEditPopup(String exerciseName, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupview = inflater.inflate(R.layout.add_exercise, null);

        EditText newWeightText = popupview.findViewById(R.id.editWeight);
        EditText newSetsText = popupview.findViewById(R.id.editSets);
        EditText newRepText = popupview.findViewById(R.id.editReps);

        newWeightText.setText("");
        newSetsText.setText("");
        newRepText.setText("");

        builder.setView(popupview).setTitle("Edit Exercise").setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newWeights = newWeightText.getText().toString().trim();
                String newSets = newSetsText.getText().toString().trim();
                String newReps = newRepText.getText().toString().trim();

                String updatedExercise = exerciseName + ": Weight(lbs): " + newWeights + ", Sets: " + newSets + ", Reps: " + newReps;
                entryList.set(position, updatedExercise);
                entryAdapter.notifyDataSetChanged();

                HashMap<String, Object> updatedExercises = new HashMap<>();
                updatedExercises.put("Weight(lbs)", newWeights);
                updatedExercises.put("Sets", newSets);
                updatedExercises.put("Reps", newReps);

                mDatabaseRefEntries.child("entry").child("exercise_task").child(exerciseName).setValue(updatedExercises)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(requireContext(), "Exercise Task Updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }).setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeletePopup(int position) {
        String entryRemove = entryList.get(position);
        entryList.remove(position);
        entryAdapter.notifyDataSetChanged();

        boolean exerciseEntry = entryRemove.contains("Weight");
        if (exerciseEntry){
            String exerciseName = entryRemove.split(":")[0].trim();

            mDatabaseRefEntries.child("entry").child("exercise_task").child(exerciseName).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(requireContext(), "Entry Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Error Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            mDatabaseRefEntries.child("entry").child("general_task").orderByValue().equalTo(entryRemove)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(requireContext(), "Entry Deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), "Error Deleting", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}