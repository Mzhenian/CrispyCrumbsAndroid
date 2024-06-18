package com.example.crispycrumbs.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;

public class CustomMediaController extends MediaController {

    public CustomMediaController(Context context) {
        super(context);
    }

//    @Override
    public void setAnchorView(View view, int bottomMargin) {
        super.setAnchorView(view);

        // Customize the position and size of the MediaController
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // Set margins (left, top, right, bottom) to adjust the position
        params.setMargins(0, 0, 0, bottomMargin);// - getHeight()); //todo pin bottom margin to video bottom
        this.setLayoutParams(params);
    }
}

