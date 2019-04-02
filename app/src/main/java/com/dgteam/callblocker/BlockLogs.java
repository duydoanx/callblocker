package com.dgteam.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.blurpopupwindow.library.BlurPopupWindow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class BlockLogs extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String BLOCK_LOG = "block_logs.dat";

    private RecyclerView recyclerView;
    private FloatingActionButton fabClearAll;

    private LogContactAdapter logContactAdapter;
    protected static List<ContactItemLog> contactItemLogList = new ArrayList<ContactItemLog>();
    private Context context;

    private String mParam1;
    private String mParam2;


    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver broadcastReceiver;

    public BlockLogs() {

    }

    public static BlockLogs newInstance(String param1, String param2) {
        BlockLogs fragment = new BlockLogs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        readLogs();


        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_block_logs, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.rvLog);
        fabClearAll = (FloatingActionButton)view.findViewById(R.id.fabClearAll);

        fabClearAll.setOnClickListener(v -> {
            if (!contactItemLogList.isEmpty()) {
                BlurPopupWindow dialog = new BlurPopupWindow.Builder(context)
                        .setContentView(R.layout.dialog)
                        .setGravity(Gravity.CENTER)
                        .setScaleRatio(0.2f)
                        .setBlurRadius(15)
                        .setTintColor(0x30000000)
                        .setAnimationDuration(300)
                        .setDismissOnClickBack(false)
                        .setDismissOnTouchBackground(false)
                        .build();
                TextView message = (TextView) dialog.findViewById(R.id.tvMessage);
                Button agree = (Button) dialog.findViewById(R.id.btAgree);
                Button degree = (Button) dialog.findViewById(R.id.btDegree);

                message.setText("Bạn có chắc muốn xóa tất cả nhật kí chặn cuộc gọi?");
                agree.setText("Xóa");
                agree.setTextColor(Color.parseColor("#FF0000"));
                agree.setOnClickListener(v1 -> {
                    List<ContactItemLog> backupList = new ArrayList<ContactItemLog>();
                    backupList.clear();
                    for (ContactItemLog i: contactItemLogList){
                        backupList.add(i);
                    }
                    contactItemLogList.clear();
                    logContactAdapter.notifyDataSetChanged();
                    writeLogs();
                    //Toast.makeText(container.getContext(),"Đã xóa nhật kí chặn cuộc gọi",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Snackbar.make(view, "Đã xóa nhật kí chặn cuộc gọi", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v2 -> {
                                for (ContactItemLog i: backupList){
                                    contactItemLogList.add(i);
                                }
                                logContactAdapter.notifyDataSetChanged();
                                writeLogs();
                            }).show();
                });
                degree.setTextColor(Color.parseColor("#FF0000"));
                degree.setText("Hủy");
                degree.setOnClickListener(v1 -> dialog.dismiss());
                dialog.show();
            }else {
                //Toast.makeText(container.getContext(),"Nhật kí chặn cuộc gọi rỗng",Toast.LENGTH_SHORT).show();
                Snackbar.make(view, "Nhật kí chặn cuộc gọi rỗng", Snackbar.LENGTH_SHORT).show();
            }

        });

        showRecyclerView(container, view);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).
                        equals(TelephonyManager.EXTRA_STATE_IDLE)){
                    readLogs();
                    Log.d("aaa", "onReceive: "+contactItemLogList.size());
                    logContactAdapter.notifyDataSetChanged();
                }
            }
        };
        context.registerReceiver(broadcastReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));

        return view;
    }

    public static void addList(ContactItemLog contactItemLog){
        if (contactItemLogList.isEmpty()) {
            contactItemLogList.add(0,contactItemLog);
            contactItemLogList.add(0,new ContactItemLog(contactItemLog.getDateLog()));

        }else {
            ContactItemLog contactItemLog1 = contactItemLogList.get(0);
            if (contactItemLog1.getDateLog().equalsIgnoreCase(contactItemLog.getDateLog())){
                contactItemLogList.add(1,contactItemLog);
            }else {
                contactItemLogList.add(0,contactItemLog);
                contactItemLogList.add(0,new ContactItemLog(contactItemLog.getDateLog()));
            }
        }
    }

    public static void checkLogs(){
        if (contactItemLogList.size()<=1){
            contactItemLogList.clear();
        }else {
            if (contactItemLogList.get(contactItemLogList.size()-1).getHeader()!=null){
                Log.d("aaa", "checkLogs: Xoa cuoi");
                contactItemLogList.remove(contactItemLogList.size()-1);
            }
            for (int i=0;i<contactItemLogList.size()-1; i++ ){
                if (contactItemLogList.get(i).getHeader()!=null
                        && !contactItemLogList.get(i+1).getDateLog()
                        .equalsIgnoreCase(contactItemLogList.get(i).getDateLog())){
                    contactItemLogList.remove(i);
                }
            }
        }
        if (!contactItemLogList.isEmpty() && contactItemLogList.get(0).getHeader()==null){
            contactItemLogList.add(0,new ContactItemLog(contactItemLogList.get(0).getDateLog()));
        };
        int i=0;
        while (i<contactItemLogList.size()-1){
            if (!contactItemLogList.get(i).getDateLog()
                    .equalsIgnoreCase(contactItemLogList.get(i+1).getDateLog())
                    && contactItemLogList.get(i).getHeader()==null &&
                    contactItemLogList.get(i+1).getHeader()==null){
                contactItemLogList.add(i+1,new ContactItemLog(
                        contactItemLogList.get(i+1).getDateLog()));
            }
            if(contactItemLogList.get(i).getHeader()!=null && contactItemLogList.get(i+1)
                    .getHeader()!=null){
                contactItemLogList.remove(i);
            }
            i++;
        }
    }

    public void showRecyclerView(ViewGroup container, View view){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(container.getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(container.getContext(), 0));
        logContactAdapter = new LogContactAdapter(contactItemLogList, container.getContext(), view);

        recyclerView.setAdapter(logContactAdapter);
        logContactAdapter.notifyDataSetChanged();
    }
    public static void writeLogs(){
        try {
            FileOutputStream fileOut = (FileOutputStream) MainActivity.getContextOfApplication()
                    .openFileOutput(BLOCK_LOG,Context.MODE_PRIVATE);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOut);
            for (int i=contactItemLogList.size()-1;i>=0;i--){
                outputStream.writeObject(contactItemLogList.get(i));
            }
            outputStream.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readLogs(){
        try {
            FileInputStream fileIn = MainActivity.getContextOfApplication()
                    .openFileInput(BLOCK_LOG);
            ObjectInputStream inputStream = new ObjectInputStream(fileIn);

            ContactItemLog itemLog;
            contactItemLogList.clear();

            while ((itemLog = (ContactItemLog) inputStream.readObject())!= null){
                addList(itemLog);
                checkLogs();
            }

            fileIn.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            checkLogs();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context=context;
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

    @Override
    public void onDestroyView() {
        context.unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
