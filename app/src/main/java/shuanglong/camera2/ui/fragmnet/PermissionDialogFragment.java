package shuanglong.camera2.ui.fragmnet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import shuanglong.camera2.R;
import shuanglong.camera2.interfaces.IClickDialogYesBtuuon;


/**
 * Created by shuanglong on 2018/4/20.
 */

public class PermissionDialogFragment extends DialogFragment implements View.OnClickListener {

    private static IClickDialogYesBtuuon iClickDialogYesBtuuons = null;
    private static String contexts = "";

    public static PermissionDialogFragment getInstance(IClickDialogYesBtuuon iClickDialogYesBtuuon, String context) {
        iClickDialogYesBtuuons = iClickDialogYesBtuuon;
        contexts = context;
        return new PermissionDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission_dialog, container, false);
        TextView context = view.findViewById(R.id.tv_context);
        context.setText(contexts);
        Button no = view.findViewById(R.id.btn_no);
        Button yes = view.findViewById(R.id.btn_yes);
        no.setOnClickListener(this);
        yes.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_no:
                this.dismiss();
                break;
            case R.id.btn_yes:
                if (iClickDialogYesBtuuons != null)
                    iClickDialogYesBtuuons.clickDialogYesBtuuon();
                this.dismiss();
                break;
        }
    }
}
