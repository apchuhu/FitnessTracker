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
import java.util.Locale;
import java.util.SimpleTimeZone;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        return view;
    }

    // Used for testing
    public void setDate(int year, int month, int dayOfMonth){
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

    private void showAddItem(LayoutInflater inflater){
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
                        }
                        else {
                            String entry = editText.getText().toString().trim();
                            if (!entry.isEmpty()){
                                entryList.add(entry);
                                entryAdapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), "Entry Added", Toast.LENGTH_SHORT).show();
                                Log.d("EntryList", "Updated List: " + entryList.toString());
                                dialog.dismiss();
                            }
                            else {
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