package edu.northeastern.rhythmlounge;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewEventSpace extends RecyclerView.ItemDecoration {
    private final int space;

    public RecyclerViewEventSpace(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = -space;
    }
}
