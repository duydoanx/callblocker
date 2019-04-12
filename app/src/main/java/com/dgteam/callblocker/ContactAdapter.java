package com.dgteam.callblocker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.blurpopupwindow.library.BlurPopupWindow;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<ContactItem> contactList;
    private int layout;
    private Context context;
    private View view;

    public ContactAdapter(List<ContactItem> contactList, int layout, Context context, View view) {
        this.contactList = contactList;
        this.layout = layout;
        this.context = context;
        this.view = view;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(layout,viewGroup,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tvName.setText(contactList.get(i).getName());
        viewHolder.tvNumber.setText(contactList.get(i).getNumber());
        viewHolder.imAvatar.setImageBitmap(contactList.get(i).getAvatar());

        viewHolder.imDetele.setOnClickListener(v -> {
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

            message.setText("Bạn có chắc chắn muốn xóa?");
            agree.setText("Xóa");
            agree.setTextColor(Color.parseColor("#FF0000"));
            agree.setOnClickListener(v1 -> {
                ContactItem backup = contactList.get(i);
                contactList.remove(i);
                notifyDataSetChanged();
                BlackList.writeContact();
                SmsBlackList.writeContact();
                dialog.dismiss();
                Snackbar snackbar = Snackbar.make(view, "Đã xóa", Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", v2 -> {
                                contactList.add(i,backup);
                                notifyDataSetChanged();
                                BlackList.writeContact();
                        });
                View snackBarView = snackbar.getView();
                TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            });
            degree.setTextColor(Color.parseColor("#FF0000"));
            degree.setText("Hủy");
            degree.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();

        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener, View.OnLongClickListener*/{

        private ImageView imAvatar, imDetele;
        private TextView tvName, tvNumber;

        //private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvNumber = (TextView)itemView.findViewById(R.id.tvNumber);
            imDetele = (ImageView)itemView.findViewById(R.id.imDelete);

//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//            setItemClickListener(new ItemClickListener() {
//                @Override
//                public void onClick(View view, int position, boolean isLongClick) {
//
//                }
//            });
        }

//        public void setItemClickListener(ItemClickListener itemClickListener) {
//            this.itemClickListener = itemClickListener;
//        }
//
//        @Override
//        public void onClick(View view) {
//            itemClickListener.onClick(view,getAdapterPosition(),false);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            itemClickListener.onClick(view,getAdapterPosition(),true);
//            return false;
//        }
    }
}