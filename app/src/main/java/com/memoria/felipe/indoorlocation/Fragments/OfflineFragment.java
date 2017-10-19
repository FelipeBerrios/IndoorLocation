package com.memoria.felipe.indoorlocation.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memoria.felipe.indoorlocation.R;
import com.memoria.felipe.indoorlocation.Utils.App;
import com.memoria.felipe.indoorlocation.Utils.CustomBeacon;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacons;
import com.memoria.felipe.indoorlocation.Utils.Model.BeaconsDao;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoSession;

import java.util.List;

import java8.util.stream.StreamSupport;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OfflineFragment.OnFragmentOfflineListener} interface
 * to handle interaction events.
 * Use the {@link OfflineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OfflineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RadioGroup mRadioGroupCoord;
    private LinearLayout mLinearPatron;
    private LinearLayout mLinearCoord;
    private TextView mTextViewCoordX;
    private TextView mTextViewCoordY;
    private EditText mEditTextPosX;
    private EditText mEditTextPosY;
    private EditText mEditTextPatron;

    //Fields Beacon Insert

    private Button mButtonInitScanBeacon;
    private ProgressBar mProgressScanBeacon;
    private RelativeLayout mRelativeInsertBeacon;
    private TextView mTextViewMac;
    private TextView mTextViewUniqueId;
    private EditText mEditXBeacon;
    private EditText mEditYBeacon;
    private Button mButtonIngresar;
    //private Spinner mSpinnerOrientation;
    private Button mButtonMakeScan;

    private Double mXCoord;
    private Double mYCoord;

    private CustomBeacon mNewBeacon;
    private DaoSession daoSession;
    private BeaconsDao beaconsDao;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentOfflineListener mListener;

    public OfflineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OfflineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OfflineFragment newInstance(String param1, String param2) {
        OfflineFragment fragment = new OfflineFragment();
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

        // get the note DAO
        daoSession = ((App) getActivity().getApplication()).getDaoSession();
        beaconsDao = daoSession.getBeaconsDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_offline, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mRadioGroupCoord = (RadioGroup)getView().findViewById(R.id.radiogroup_coord);
        mLinearCoord = (LinearLayout)getView().findViewById(R.id.linear_layout_coordenadas);
        mLinearPatron = (LinearLayout)getView().findViewById(R.id.linear_layout_patron);
        mTextViewCoordX = (TextView)getView().findViewById(R.id.tv_coordenada_x);
        mTextViewCoordY = (TextView)getView().findViewById(R.id.tv_coordenada_y);
        mEditTextPosX = (EditText)getView().findViewById(R.id.edit_text_patron_pos_x);
        mEditTextPosY = (EditText)getView().findViewById(R.id.edit_text_patron_pos_y);
        mEditTextPatron = (EditText)getView().findViewById(R.id.edit_text_patron);
        //mSpinnerOrientation = (Spinner) getView().findViewById(R.id.spinner_orientation);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.orientation_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        //mSpinnerOrientation.setAdapter(adapter);
        mButtonMakeScan = (Button)getView().findViewById(R.id.make_scan_button);

        mButtonInitScanBeacon = (Button)getView().findViewById(R.id.button_init_scan_beacon);
        mProgressScanBeacon = (ProgressBar) getView().findViewById(R.id.progress_scan);
        mRelativeInsertBeacon = (RelativeLayout)getView().findViewById(R.id.relative_beacon_data);
        mTextViewMac = (TextView)getView().findViewById(R.id.mac_adress);
        mTextViewUniqueId = (TextView)getView().findViewById(R.id.unique_id);
        mEditXBeacon = (EditText)getView().findViewById(R.id.edit_text_x_beacon);
        mEditYBeacon = (EditText)getView().findViewById(R.id.edit_text_y_beacon);
        mButtonIngresar = (Button)getView().findViewById(R.id.button_insert_new_beacon);

        mRadioGroupCoord.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId==R.id.radio_coordenadas){
                    mLinearPatron.setVisibility(View.GONE);
                    mLinearCoord.setVisibility(View.VISIBLE);
                }
                else{
                    mLinearCoord.setVisibility(View.GONE);
                    mLinearPatron.setVisibility(View.VISIBLE);
                }
            }
        });

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
                String xString = mEditTextPosX.getText().toString();
                String yString = mEditTextPosY.getText().toString();
                String patronString  = mEditTextPatron.getText().toString();
                //Integer orientation = mSpinnerOrientation.getSelectedItemPosition();
                if(mXCoord!=null && mYCoord!=null /*&&
                        orientation!= AdapterView.INVALID_POSITION*/ ){
                    mListener.onGetFingerprint(mXCoord,mYCoord);
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Debes llenar los campos corrctamente", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonInitScanBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressScanBeacon.setVisibility(View.VISIBLE);
                mRelativeInsertBeacon.setVisibility(View.GONE);
                mListener.onRequestCloseBeacon();
                mButtonInitScanBeacon.setEnabled(false);
            }
        });

        mButtonIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String xText = mEditXBeacon.getText().toString();
                String yText = mEditYBeacon.getText().toString();

                try{
                    if(xText.equals("") || yText.equals("")){
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Debes ingresar posiciones validas",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Double xBeacon = Double.parseDouble(xText);
                        Double yBeacon = Double.parseDouble(yText);
                        processInsertBeacon(xBeacon,yBeacon,mNewBeacon);
                    }
                }

                catch(NumberFormatException e){
                    //En caso de que los inputs no puedan ser transformados a numeros
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Debes ingresar posiciones validas",
                            Toast.LENGTH_SHORT).show();
                }


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

    public void getFingerprint(){

    }

    public void captureNewBeacon(CustomBeacon bc){

        if(bc.getMAC()!=null){
            mNewBeacon = new CustomBeacon(bc);
            mProgressScanBeacon.setVisibility(View.GONE);
            mRelativeInsertBeacon.setVisibility(View.VISIBLE);
            mButtonInitScanBeacon.setEnabled(true);
            mTextViewMac.setText("MAC: " + bc.getMAC());
            mTextViewUniqueId.setText("Id Unico: " + bc.getUniqueId());
        }
        else{
            mNewBeacon = null;
            mProgressScanBeacon.setVisibility(View.GONE);
            mRelativeInsertBeacon.setVisibility(View.GONE);
            mButtonInitScanBeacon.setEnabled(true);
            mTextViewMac.setText("");
            mTextViewUniqueId.setText("");
        }

    }

    public void processInsertBeacon(Double x, Double y, CustomBeacon bc){

        Beacons newBeacon = new Beacons();
        newBeacon.setName(bc.getName());
        newBeacon.setInstanceId(bc.getInstanceId());
        newBeacon.setUniqueId(bc.getUniqueId());
        newBeacon.setTxPower(bc.getTxPower());
        newBeacon.setMAC(bc.getMAC());
        newBeacon.setNameSpace(bc.getNameSpace());
        newBeacon.setRssi(bc.getRssi());
        newBeacon.setXPosition(x);
        newBeacon.setYPosition(y);
        long beaconExist = beaconsDao.queryBuilder().where(BeaconsDao.Properties.MAC.eq(bc.getMAC())).count();

        if(beaconExist==0){
            try{
                beaconsDao.insert(newBeacon);
                Toast.makeText(getActivity().getApplicationContext(),
                        "Insercion realizada con exito",
                        Toast.LENGTH_SHORT).show();
                mRelativeInsertBeacon.setVisibility(View.GONE);
                mListener.onInsertBeacon(newBeacon);

            }
            catch (Exception ex){
                ex.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Ha ocurrido un error en la insercion",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getActivity().getApplicationContext(),
                    "Esta MAC ya se encuentra registrada, prueba con otra",
                    Toast.LENGTH_SHORT).show();
            mRelativeInsertBeacon.setVisibility(View.GONE);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentOfflineListener) {
            mListener = (OnFragmentOfflineListener) context;
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
    public interface OnFragmentOfflineListener {
        // TODO: Update argument type and name
        void onRequestCloseBeacon();
        void onInsertBeacon(Beacons beacon);
        void onGetFingerprint(Double x, Double y);
    }
}
