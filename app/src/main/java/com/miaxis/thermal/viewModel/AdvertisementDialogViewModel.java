package com.miaxis.thermal.viewModel;

import android.graphics.drawable.Drawable;

import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;

import java.util.ArrayList;
import java.util.List;

public class AdvertisementDialogViewModel extends BaseViewModel {

    public AdvertisementDialogViewModel() {
    }

    public List<?> getAdvertisementList() {
        List<Drawable> imageList = new ArrayList<>();
        imageList.add(App.getInstance().getResources().getDrawable(R.drawable.background_miaxis, null));
        return imageList;
    }

}
