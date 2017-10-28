package com.memoria.felipe.indoorlocation.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.memoria.felipe.indoorlocation.R;
import com.memoria.felipe.indoorlocation.Screens.MainActivity;
import com.memoria.felipe.indoorlocation.Utils.App;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacon_RSSI;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacon_RSSIDao;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacons;
import com.memoria.felipe.indoorlocation.Utils.Model.BeaconsDao;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoSession;
import com.memoria.felipe.indoorlocation.Utils.Model.Fingerprint;
import com.memoria.felipe.indoorlocation.Utils.Model.FingerprintDao;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button mButtonExport;
    private Button mButtonCsv;
    private DaoSession daoSession;
    private FingerprintDao fingerprintDao;
    private Beacon_RSSIDao beacon_rssiDao;
    private BeaconsDao beaconsDao;
    private Button mButtonDelete;

    private EditText mEditTextNumberMeditions;
    private EditText mEditTextIntervalMeditions;
    private TextView mTextViewTotalTime;
    private Button mButtonSaveMeditions;


    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        daoSession = ((App) getActivity().getApplication()).getDaoSession();
        fingerprintDao = daoSession.getFingerprintDao();
        beacon_rssiDao = daoSession.getBeacon_RSSIDao();
        beaconsDao = daoSession.getBeaconsDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mButtonExport = (Button)getView().findViewById(R.id.button_export);
        mButtonCsv = (Button)getView().findViewById(R.id.button__generate_csv);

        mEditTextNumberMeditions = (EditText)getView().findViewById(R.id.edit_text_number_of_meditions);
        mEditTextIntervalMeditions = (EditText)getView().findViewById(R.id.edit_text_interval_meditions);
        mTextViewTotalTime = (TextView)getView().findViewById(R.id.textview_calculo_tiempo_mediciones);

        mButtonSaveMeditions = (Button)getView().findViewById(R.id.button_save_meditions_settings);
        mButtonDelete = (Button)getView().findViewById(R.id.button_delete_db);

        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDeleteBD();
            }
        });

        mButtonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDatabse("indoor-db");
            }
        });

        mButtonCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCSVFile();
            }
        });

        loadInitialData();

        mEditTextIntervalMeditions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateMeditionsTime();
            }
        });

        mEditTextNumberMeditions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateMeditionsTime();
            }
        });

        mButtonSaveMeditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Integer numberOfMedition = Integer.valueOf(mEditTextNumberMeditions.getText().toString());
                    Integer intervalMeditions = Integer.valueOf(mEditTextIntervalMeditions.getText().toString());

                    if(numberOfMedition!=null && intervalMeditions!=null){

                        mListener.onSetMeditionsData(numberOfMedition, intervalMeditions);

                    }
                    else{
                        Toast.makeText(getActivity(), "Debes introducir campos validos", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Debes introducir campos validos", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void UpdateMeditionsTime(){
        try{
            Integer numberOfMedition = Integer.valueOf(mEditTextNumberMeditions.getText().toString());
            Integer intervalMeditions = Integer.valueOf(mEditTextIntervalMeditions.getText().toString());

            if(numberOfMedition!=null && intervalMeditions!=null){
                Integer totalTime = numberOfMedition*intervalMeditions;
                Double segundos = totalTime/1000.0;

                mTextViewTotalTime.setText("Tiempo Total: " + String.valueOf(segundos) + " s");

            }
            else{
                Toast.makeText(getActivity(), "Debes introducir campos validos", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Debes introducir campos validos", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadInitialData(){
        mListener.onRequestMeditionsData();
    }

    public void catchDataResults(Integer numberMeditions, Integer interval){
        mEditTextNumberMeditions.setText(numberMeditions.toString());
        mEditTextIntervalMeditions.setText(interval.toString());
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
        }
    }

    public void showDialogDeleteBD(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("Eliminar Base de Datos")
                .setMessage("¿Seguro que deseas eliminar los registros de la base de datos?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        mListener.onDeleteDB();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onRequestMeditionsData();
        void onSetMeditionsData(Integer meditions, Integer interval);
        void onDeleteDB();
    }

    public void exportDatabse(String databaseName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                //String currentDBPath = "//data//"+getPackageName()+"//databases//"+databaseName+"";
                String currentDBPath = getActivity().getApplicationContext().getDatabasePath(databaseName).getAbsolutePath();
                String folderS = sd + File.separator+"Indoor";
                String backupDBPath = databaseName + "-backup.db";
                File currentDB = new File(currentDBPath);
                File folder = new File(folderS);
                File backupDB;
                if(!folder.exists()){
                    folder.mkdirs();
                    backupDB = new File(folderS, backupDBPath);
                }
                else{
                    backupDB = new File(folderS, backupDBPath);
                }

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    MediaScannerConnection.scanFile(getActivity().getApplicationContext(),
                            new String[] {backupDB.toString()}, null, null);
                }
            }
        } catch (Exception e) {

        }
    }

    public void createCSVFile(){

        Handler handler = new Handler();  //Optional. Define as a variable in your activity.

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try{
                    String fileName= "IndoorFingerprint.csv";
                    List<Beacons> beaconses = beaconsDao.loadAll();
                    String external = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    String folderS = external + File.separator +"Indoor";
                    String csv = folderS + File.separator + fileName;
                    CSVWriter writer;
                    File folder = new File(folderS);

                    if(!folder.exists()){
                        folder.mkdirs();
                    }

                    File f = new File(csv);
                    // File exist
                    if(f.exists() && !f.isDirectory()){
                        FileWriter mFileWriter = new FileWriter(csv, true);
                        writer = new CSVWriter(mFileWriter);
                    }
                    else {
                        writer = new CSVWriter(new FileWriter(csv));
                    }
                    List<String> header = new ArrayList<String>();
                    List<String> beaconsNames = StreamSupport.stream(beaconses)
                            .map(x->x.getUniqueId()).collect(Collectors.toList());
                    header.add("X");
                    header.add("Y");
                    header.addAll(beaconsNames);
                    String[] row = header.toArray(new String[0]);
                    writer.writeNext(row);

                    List<Fingerprint> fingerprints = fingerprintDao.loadAll();
                    List<Beacon_RSSI> actual = new ArrayList<Beacon_RSSI>();
                    List<String> rowList = new ArrayList<String>();
                    for(int i = 0; i<fingerprints.size(); i++){
                        actual = beacon_rssiDao.queryBuilder()
                                .where(Beacon_RSSIDao.Properties.FingerprintId.eq(fingerprints.get(i).getId()))
                                .orderAsc(Beacon_RSSIDao.Properties.BeaconId)
                                .list();
                        List<String> rssi = StreamSupport.stream(actual)
                                .map(x->x.getRssi().toString()).collect(Collectors.toList());

                        rowList.add(fingerprints.get(i).getXPosition().toString());
                        rowList.add(fingerprints.get(i).getYPosition().toString());
                        rowList.addAll(rssi);
                        row = rowList.toArray(new String[0]);
                        writer.writeNext(row);
                        rowList.clear();

                    }
                    writer.close();

                    MediaScannerConnection.scanFile(getActivity().getApplicationContext(),
                            new String[] {f.toString()}, null, null);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run()
                    {
                        Toast.makeText(getActivity(), "CSV creado con exito", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        Thread t = new Thread(r);
        t.start();

    }
}
