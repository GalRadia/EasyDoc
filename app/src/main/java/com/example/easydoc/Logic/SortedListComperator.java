package com.example.easydoc.Logic;

import androidx.recyclerview.widget.SortedList;

import com.example.easydoc.Model.Appointment;

public class SortedListComperator extends SortedList.Callback<Appointment> {
    public SortedListComperator() {
    }

    @Override
    public int compare(Appointment o1, Appointment o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void onChanged(int position, int count) {

    }

    @Override
    public boolean areContentsTheSame(Appointment oldItem, Appointment newItem) {
        return false;
    }

    @Override
    public boolean areItemsTheSame(Appointment item1, Appointment item2) {
        return false;
    }

    @Override
    public void onInserted(int position, int count) {

    }

    @Override
    public void onRemoved(int position, int count) {

    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {

    }
}
