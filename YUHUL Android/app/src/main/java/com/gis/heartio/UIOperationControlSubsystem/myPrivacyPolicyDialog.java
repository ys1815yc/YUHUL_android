package com.gis.heartio.UIOperationControlSubsystem;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gis.heartio.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class myPrivacyPolicyDialog {
    AppCompatActivity context;
    private int backgroundColor;
    private int titleTextColor;
    private int subtitleTextColor;
    private int linkTextColor;
    private int termsOfServiceTextColor;
    private int acceptButtonColor;
    private int acceptTextColor;

    private String acceptText;

    // Cancel
    private String cancelText;
    private int cancelTextColor;

    private String title;
    private String termsOfServiceSubtitle;

    private String termsOfServiceUrl, privacyPolicyUrl;

    private ArrayList<String> lines;

    public int getBackgroundColor(){
        return this.backgroundColor;
    }

    public void setBackgroundColor(int inputColor){
        this.backgroundColor = inputColor;
    }

    public int getTitleTextColor(){
        return this.titleTextColor;
    }

    public void setTitleTextColor(int inputColor){
        this.titleTextColor = inputColor;
    }

    public int getSubtitleTextColor(){
        return this.subtitleTextColor;
    }

    public void setSubtitleTextColor(int inputColor){
        this.subtitleTextColor = inputColor;
    }


    myPrivacyPolicyDialog(AppCompatActivity context,
                          String termsOfServiceUrl,
                          String privacyPolicyUrl){

        this.context = context;
        this.termsOfServiceUrl = termsOfServiceUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;

        this.backgroundColor = Color.parseColor("#ffffff");
        this.titleTextColor = Color.parseColor("#222222");
        this.subtitleTextColor = Color.parseColor("#757575");
        this.linkTextColor = Color.parseColor("#000000");
        this.termsOfServiceTextColor = Color.parseColor("#757575");

        // Accept Button

        this.acceptButtonColor = Color.parseColor("#222222");
        this.acceptTextColor = Color.parseColor("#ffffff");

        cancelTextColor = Color.parseColor("#757575");

        this.acceptText = context.getString(R.string.agree);
        this.cancelText = context.getString(R.string.cancel);
        this.title = context.getString(R.string.term_of_serivce);
        this.termsOfServiceSubtitle = this.context.getString(R.string.terms_of_service_subtitle);

        this.lines = new ArrayList<>();

    }

    public interface OnClickListener {
        void onAccept(Boolean isFirstTime);
        void onCancel();
    }

    private myPrivacyPolicyDialog.OnClickListener onClickListener = null;
    private AlertDialog dialog = null;
    private myPrivacyPoliciesAdapter adapter;

    public void setAcceptText(String acceptText) {
        this.acceptText = acceptText;
    }

    public void setCancelText(String cancelText){
        this.cancelText = cancelText;
    }

    public void setTitle(String titleText){
        this.title = titleText;
    }

    public void addPoliceLine(String line){
        lines.add(line);
    }

    public int getAcceptButtonColor(){
        return this.acceptButtonColor;
    }

    public void setAcceptButtonColor(int inputColor){
        this.acceptButtonColor = inputColor;
    }

    public String getTermsOfServiceSubtitle(){
        return this.termsOfServiceSubtitle;
    }

    public void setTermsOfServiceSubtitle(String subtitle){
        this.termsOfServiceSubtitle = subtitle;
    }

    private View loadLayout(){

        View layout = context.getLayoutInflater().inflate(R.layout.dialog_privacy_policies, null);
        RelativeLayout mContainer = layout.findViewById(R.id.container);
        mContainer.setBackgroundColor(backgroundColor);

        TextView mTermsOfServiceTitle = layout.findViewById(R.id.termsOfServiceTitle);
        mTermsOfServiceTitle.setText(title);
        mTermsOfServiceTitle.setTextColor(titleTextColor);

        TextView termsOfServiceSubtitleTextView = layout.findViewById(R.id.termsOfServiceSubtitleTextView);
        termsOfServiceSubtitleTextView.setText(this.toHtml(this.termsOfServiceSubtitle));
        termsOfServiceSubtitleTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsOfServiceSubtitleTextView.setLinkTextColor(linkTextColor);
        termsOfServiceSubtitleTextView.setTextColor(subtitleTextColor);

        RelativeLayout acceptButton = layout.findViewById(R.id.acceptButton);
        setBackgroundColor(acceptButton,acceptButtonColor);

        TextView acceptTextView = layout.findViewById(R.id.acceptTextView);
        acceptTextView.setTextColor(acceptTextColor);
        acceptTextView.setText(acceptText);

        TextView cancelTextView = layout.findViewById(R.id.cancelTextView);
        cancelTextView.setTextColor(cancelTextColor);
        cancelTextView.setText(cancelText);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onClickListener.onAccept(true);
            }
        });

        RelativeLayout cancelButton = layout.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onClickListener.onCancel();

            }
        });


        RecyclerView mPoliciesRecyclerView = layout.findViewById(R.id.policiesRecyclerView);
        mPoliciesRecyclerView.setLayoutManager(new LinearLayoutManager(this.context));
        this.adapter = new myPrivacyPoliciesAdapter(this.termsOfServiceTextColor);
        mPoliciesRecyclerView.setAdapter(this.adapter);
        if (this.adapter!=null){
            Log.d("ppdialog","we have "+this.lines.size()+" lines");
            this.adapter.updateDataSet(this.lines);
        }
        return layout;
    }

    public void setOnClickListener(myPrivacyPolicyDialog.OnClickListener listener){
        this.onClickListener = listener;
    }

    private void dismiss(){
        if (!context.isFinishing()&& dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(loadLayout());
        builder.setCancelable(false);
        dialog = builder.show();
    }

    private Spanned toHtml(int res){
        String body = this.context.getString(res);
        return this.toHtml(body);
    }

    private Spanned toHtml(String body){
        String outputBody = body
                .replace("{accept}",this.context.getString(R.string.agree))
                .replace("{privacy}","<a href=\""+this.privacyPolicyUrl+"\">")
                .replace("{/privacy}","</a>")
                .replace("{terms}","<a href=\""+this.termsOfServiceUrl+"\">")
                .replace("{/terms}","</a>")
                .replace("{","<")
                .replace("}",">");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return Html.fromHtml(outputBody,Html.FROM_HTML_MODE_LEGACY);
        }else {
            return Html.fromHtml(outputBody);
        }
    }

    private void setBackgroundColor(View view, int resColor){
        if (view == null || view.getBackground() == null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (view.getBackground() instanceof RippleDrawable)){
            RippleDrawable rippleDrawable = (RippleDrawable) view.getBackground();
            rippleDrawable.setColorFilter(resColor, PorterDuff.Mode.MULTIPLY);
        } else {
            if (view.getBackground() instanceof ColorDrawable){
                // Non ripple
                ColorDrawable drawable = (ColorDrawable)view.getBackground();
                drawable.setColor(resColor);
            } else if (view.getBackground() instanceof GradientDrawable){
                GradientDrawable drawable = (GradientDrawable)view.getBackground();
                drawable.setColor(resColor);
            }
        }
    }

    public class myPrivacyPoliciesAdapter extends RecyclerView.Adapter{
        private int textColor;
        private ArrayList<String> items;

        public myPrivacyPoliciesAdapter(int textColor){
            this.textColor = textColor;
            this.items = new ArrayList<>();
        }

        public class myPrivacyItemViewHolder extends RecyclerView.ViewHolder{

            public myPrivacyItemViewHolder(View itemView) {
                super(itemView);
            }
            public void bind(int textColor,int number,String body){
                //this.numberTextView.text
                View inputView = this.itemView;
                TextView numberTextView = inputView.findViewById(R.id.numberTextView);
                numberTextView.setText(""+number+'.');
                TextView bodyTextView = inputView.findViewById(R.id.bodyTextView);
                bodyTextView.setText(body);
                numberTextView.setTextColor(textColor);
                bodyTextView.setTextColor(textColor);

            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.privacy_policy_item, parent, false);
            return new myPrivacyItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof myPrivacyPoliciesAdapter.myPrivacyItemViewHolder){
                myPrivacyPoliciesAdapter.myPrivacyItemViewHolder myHolder =
                        (myPrivacyPoliciesAdapter.myPrivacyItemViewHolder)holder;
                myHolder.bind(this.textColor,position+1,this.items.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void updateDataSet(List<String> items){
            this.items.clear();
            this.items.addAll(items);
        }
    }
}
