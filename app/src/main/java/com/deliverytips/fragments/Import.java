package com.deliverytips.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.deliverytips.DeliveryEvent;
import com.deliverytips.MyDatabaseHelper;
import com.deliverytips.Person;
import com.deliverytips.R;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class Import extends Fragment {

    Map<Integer, List<String>> imported_data = null;
    TextView textViewOut;

    public Import() {
        // Required empty public constructor
    }


    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_import, container, false);
        final Button getCSVButton = (Button)rootView.findViewById(R.id.buttonGetCSV);
        final Button importDatasetButton = (Button)rootView.findViewById(R.id.buttonImportDataset);
        textViewOut = (TextView) rootView.findViewById(R.id.textViewOutput);
        String dl_folder;

        //on click for get csv, opens PWR CSV download in browser
        getCSVButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                //get store_id from shared preferences
                String store_id = "1953";
                String url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;

                Toast.makeText(getActivity(), "Opening " + url + " in browser.", Toast.LENGTH_LONG).show();

                Uri uri = Uri.parse(url);
                Intent intent= new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });

        Button processCSVButton = (Button)rootView.findViewById(R.id.buttonProcessCSV);

        //on click for get csv, opens PWR CSV download in browser
        processCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String path = Environment.getExternalStorageDirectory().toString();
                String dl_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                File directory = new File(dl_folder);

                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if( permissionCheck == 0){
                    textViewOut.setText(textViewOut.getText() + "\nPermission: GOOD");
                    Log.d("Files", "Path: " + directory.getPath());
                    textViewOut.setText(textViewOut.getText() + "\n" + directory.getPath());
                    showFileChooser();
                    importDatasetButton.setVisibility(View.VISIBLE);

                } else {
                    textViewOut.setText(textViewOut.getText() + "\nPermission: BAD");
                }
            }
        });

        //show data and option to submit dataset
            importDatasetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getContext(), "Starting Import", Toast.LENGTH_SHORT).show();

                    MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
                    SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

                    //add each item
                    for (int i=1; i<imported_data.size(); i++) {


                        //how to access data
                        //imported_data.get(i).get(0);//store_id
                        //imported_data.get(i).get(1);//ticket_id

                        //create person if not exists
                        Person person = new Person();
                        person._address = imported_data.get(i).get(4);
                        person._phone_number =  imported_data.get(i).get(3);
                        person._id = db.insert(person.TABLE_NAME, null, person.getContentValues());

                        //create delivery event if not exists
                        DeliveryEvent deliveryEvent = new DeliveryEvent();

                        //parse delivery number
//                        String order_cell = imported_data.get(i).get(1);
//                        String[] arr = order_cell.split("#");
//                        Long order_number;
//                        if( arr.length > 0 ){
//                            order_number = Long.parseLong( arr[1] );
//                            deliveryEvent.setOrderNumber(order_number);
//                        }
//
//                        Double price = 0.0;
//                        if( imported_data.get(i).get(4) != null && Double.parseDouble(imported_data.get(i).get(4) ) > 0 ){
//                            price = Double.parseDouble(imported_data.get(i).get(4));
//                        }
//
//                        deliveryEvent.setPrice(price);//Double.parseDouble(editTextPrice.getText().toString())
//                        deliveryEvent.setTimestampNow();
//                        deliveryEvent.setPerson(person);
//                        db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());

                        Toast.makeText(getContext(), imported_data.get(i).toString(), Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getContext(), "Import Data Complete", Toast.LENGTH_SHORT).show();
                }
            });

        // Inflate the layout for this fragment
        return rootView;
    }

    private static final int FILE_SELECT_CODE = 0;
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select PWR XLS Import File"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getActivity(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                Uri uri = data.getData();
                String filename = getFileName(uri);
                String dl_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                File directory = new File(dl_folder);
                textViewOut.setText(textViewOut.getText() + "\nFile: " + filename);
                imported_data = readExcelFile(getContext(), directory.getPath() + "/" + filename );

                textViewOut.setText(textViewOut.getText() + "\n" + imported_data.toString());
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //not using this approach to parse xls
    public List<String> read(String key) throws IOException  {
        List<String> resultSet = new ArrayList<String>();

        File inputWorkbook = new File(key);
        if(inputWorkbook.exists()){
            Workbook w;
            try {
                w = Workbook.getWorkbook(inputWorkbook);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over column and lines
                for (int j = 0; j < sheet.getRows(); j++) {
                    Cell cell = sheet.getCell(0, j);
                    if(cell.getContents().equalsIgnoreCase(key)){
                        for (int i = 0; i < sheet.getColumns(); i++) {
                            Cell cel = sheet.getCell(i, j);
                            resultSet.add(cel.getContents());
                        }
                    }
                    continue;
                }
            } catch (BiffException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            resultSet.add("\nFile not found..!");
        }
        if(resultSet.size()==0){
            resultSet.add("\nData not found..!");
        }
        return resultSet;
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {

            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    private static Map<Integer, List<String>> readExcelFile(Context context, String filename) {

        Toast.makeText(context, "Reading file: " + filename, Toast.LENGTH_SHORT).show();

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
            return new HashMap<Integer, List<String>>();
        }

        try{
            // Creating Input Stream
            File file = new File(filename);

            if( !file.exists() ){
                Toast.makeText(context, "Does not Exists", Toast.LENGTH_SHORT).show();
                return new HashMap<Integer, List<String>>();
            } else {
                Toast.makeText(context, "Exists", Toast.LENGTH_SHORT).show();
            }

            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

            while(rowIter.hasNext()){

                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                List<String> row = new ArrayList<String>();

                //Toast.makeText(context, "cell Value: " + myRow.toString(), Toast.LENGTH_SHORT).show();

                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();

                    Log.d(TAG, "Cell Value: " +  myCell.toString());
                    row.add(myCell.toString());

                }

                map.put(myRow.getRowNum(),row);
            }

            return map;
        }catch (Exception e){e.printStackTrace(); }

        return new HashMap<Integer, List<String>>();
    }
}
