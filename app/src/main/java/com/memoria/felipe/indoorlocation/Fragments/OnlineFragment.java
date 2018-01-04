package com.memoria.felipe.indoorlocation.Fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memoria.felipe.indoorlocation.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnlineFragment.OnFragmentOnlineListener} interface
 * to handle interaction events.
 * Use the {@link OnlineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnlineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView mTextViewCoordX;
    private TextView mTextViewCoordY;
    private EditText mEditTextPosX;
    private EditText mEditTextPosY;
    private EditText mEditTextPatron;
    private Button mButtonMakeScan;
    private boolean isButtonScanning = false;
    private CheckBox mCheckBoxDinamic;
    private Button mButtonScanBeacon;
    private EditText mEditTextBeaconName;
    private boolean isButtonBeaconScanning = false;

    private Double mXCoord;
    private Double mYCoord;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentOnlineListener mListener;
    private Spinner mSpinnerAlgorithms;

    public OnlineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnlineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OnlineFragment newInstance(String param1, String param2) {
        OnlineFragment fragment = new OnlineFragment();
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
        return inflater.inflate(R.layout.fragment_online, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mTextViewCoordX = (TextView)getView().findViewById(R.id.tv_coordenada_x);
        mTextViewCoordY = (TextView)getView().findViewById(R.id.tv_coordenada_y);
        mEditTextPosX = (EditText)getView().findViewById(R.id.edit_text_patron_pos_x);
        mEditTextPosY = (EditText)getView().findViewById(R.id.edit_text_patron_pos_y);
        mEditTextPatron = (EditText)getView().findViewById(R.id.edit_text_patron);
        mCheckBoxDinamic = (CheckBox)getView().findViewById(R.id.checkbox_dinamic_mode);
        mEditTextBeaconName = (EditText)getView().findViewById(R.id.edit_text_name_beacon_scan);
        mButtonScanBeacon = (Button)getView().findViewById(R.id.make_scan_button_beacon);

        mSpinnerAlgorithms = (Spinner) getView().findViewById(R.id.spinner_algorithms);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.algorithms_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerAlgorithms.setAdapter(adapter);
        mButtonMakeScan = (Button)getView().findViewById(R.id.make_scan_button);

        mEditTextPosX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateCoordTextView();
            }
        });
        mEditTextPosY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateCoordTextView();
            }
        });
        mEditTextPatron.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateCoordTextView();
            }
        });

        mButtonMakeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isButtonScanning){
                    mButtonMakeScan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    mButtonMakeScan.setText("Iniciar");
                    isButtonScanning = false;
                    mListener.stopStaticPositionEstimation();
                    return;
                }
                Boolean isDinamic = mCheckBoxDinamic.isChecked();
                String xString = mEditTextPosX.getText().toString();
                String yString = mEditTextPosY.getText().toString();
                String patronString  = mEditTextPatron.getText().toString();
                int positionAlgorithm = mSpinnerAlgorithms.getSelectedItemPosition();
                int positionMode = 0;
                boolean pca =  false;
                if(positionAlgorithm ==0){
                    positionMode =0;
                    pca = false;
                }
                else if(positionAlgorithm ==1){
                    positionMode =0;
                    pca = true;
                }
                if(positionAlgorithm ==2){
                    positionMode =1;
                    pca = false;
                }
                else if(positionAlgorithm ==3){
                    positionMode =1;
                    pca = true;
                }
                if(positionAlgorithm ==4){
                    positionMode =2;
                    pca = false;
                }
                else if(positionAlgorithm ==5){
                    positionMode =2;
                    pca = true;
                }

                if(mXCoord!=null && mYCoord!=null  ){
                    isButtonScanning = true;
                    mButtonMakeScan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_material)));
                    mButtonMakeScan.setText("Detener");
                    mListener.getStaticPositionEstimation(positionMode, pca, mXCoord,mYCoord, isDinamic);
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Debes llenar los campos correctamente", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonScanBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isButtonBeaconScanning){
                    mButtonScanBeacon.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    mButtonScanBeacon.setText("Scan Beacon");
                    isButtonBeaconScanning = false;
                    mListener.stopScanBeaconByName();
                    return;
                }

                String nameBeacon = mEditTextBeaconName.getText().toString();
                isButtonBeaconScanning = true;
                mButtonScanBeacon.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_material)));
                mButtonScanBeacon.setText("Detener");
                mListener.scanBeaconByName(nameBeacon);

            }
        });
    }

    public void UpdateCoordTextView(){

        String xText = mEditTextPosX.getText().toString();
        String yText = mEditTextPosY.getText().toString();
        String patronText = mEditTextPatron.getText().toString();

        try{
            if(!xText.equals("") && !yText.equals("") && !patronText.equals("")){
                Double x  = Double.parseDouble(xText);
                Double y  = Double.parseDouble(yText);
                Double patron  = Double.parseDouble(patronText);

                if(patron !=0.0 ){
                    mXCoord = patron*x - patron/2;
                    mYCoord = patron*y - patron/2;
                    mTextViewCoordX.setText("Coord X: " + String.valueOf(mXCoord));
                    mTextViewCoordY.setText("Coord Y: " + String.valueOf(mYCoord));
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext()
                            ,"Debes Ingresar un Patron distinto de cero", Toast.LENGTH_SHORT).show();
                }
            }else{
                mXCoord = null;
                mYCoord = null;
            }
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            mXCoord = null;
            mYCoord = null;
            mTextViewCoordX.setText("Coord X: ");
            mTextViewCoordY.setText("Coord Y: ");
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentOnlineListener) {
            mListener = (OnFragmentOnlineListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentOnlineListener {
        // TODO: Update argument type and name
        // mode: 0 knn, 1: svm, 2 nn
        void getStaticPositionEstimation(int mode, boolean pca, Double xCoord, Double yCoord, Boolean dinamic);
        void stopStaticPositionEstimation();
        void scanBeaconByName(String name);
        void stopScanBeaconByName();
    }
}
