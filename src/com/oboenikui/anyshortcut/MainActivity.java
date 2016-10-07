package com.oboenikui.anyshortcut;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private Intent intent = new Intent();
    private static ArrayAdapter<String> adapter;
    private static List<NameSet> list = new ArrayList<MainActivity.NameSet>();
    private class NameSet{
        public NameSet(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public String name;
        public String value;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button addButton = Button.class.cast(findViewById(R.id.add));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView listView = ListView.class.cast(findViewById(R.id.listView1));
        listView.setAdapter(adapter);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText name = EditText.class.cast(findViewById(R.id.name));
                EditText value = EditText.class.cast(findViewById(R.id.value));
                String nameString = name.getText().toString();
                String valueString = value.getText().toString();
                if(nameString.length()==0){
                    return;
                }
                name.setText("");
                value.setText("");
                list.add(new NameSet(nameString, valueString));
                adapter.add("name:"+nameString+"\nvalue:"+valueString);
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", arg2);
                MyDialogFragment dialog = new MyDialogFragment();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(),"remove");
            }
        });
        Button createButton = Button.class.cast(findViewById(R.id.create));
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText name = EditText.class.cast(findViewById(R.id.shortcutname_editText));
                EditText application = EditText.class.cast(findViewById(R.id.application_editText));
                EditText activity = EditText.class.cast(findViewById(R.id.activity_editText));
                EditText action = EditText.class.cast(findViewById(R.id.action_editText));
                EditText data = EditText.class.cast(findViewById(R.id.data_editText));
                String packageName = application.getText().toString();
                String activityName = activity.getText().toString();
                intent.setClassName(packageName, activityName);
                if(action.getText().length()>0){
                    intent.setAction(action.getText().toString());
                }
                if(data.getText().length()>0){
                    intent.setData(Uri.parse(data.getText().toString()));
                }

                for(NameSet set:list.toArray(new NameSet[0])){
                    intent.putExtra(set.name, set.value);
                }
                Intent shortcutIntent = new Intent();
                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                if(name.getText().length()>0){
                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name.getText().toString());
                } else {
                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, activityName.substring(activityName.lastIndexOf(".")+1));
                }
                //Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
                //iconResource.packageName = packageName;
                //iconResource.resourceName = packageName+":drawable/ic_launcher";
                
                //BitmapDrawable.class.cast(getPackageManager().getApplicationIcon(packageName)).getBitmap();
                try {
                    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapDrawable.class.cast(getPackageManager().getApplicationIcon(packageName)).getBitmap());
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                shortcutIntent.setData(null);
                sendBroadcast(shortcutIntent);
                Toast.makeText(MainActivity.this, "Created!", Toast.LENGTH_SHORT).show();
                intent = new Intent();
            }
        });
    }

    public static class MyDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Remove this extra");
            builder.setMessage("Can't restore. OK?");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    list.remove(getArguments().getInt("position"));
                    adapter.remove(adapter.getItem(getArguments().getInt("position")));
                    adapter.notifyDataSetChanged();
                    MyDialogFragment.this.dismiss();
                }
            });
            builder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyDialogFragment.this.dismiss();
                }
            });
            return builder.create();
        }
    }
}
